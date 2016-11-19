package com.sharedlecturenote;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.loopj.android.http.AsyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class RoomActivity extends Activity implements OnLoadCompleteListener {
    // DrawView
    private DrawView myDrawView, otherDrawView;
    // pdfView
    private PDFView pdfView;

    // master 인지 판별
    boolean isMaster = false;

    // pdf uri
    Uri pdfUri;
    // PDF 파일 이름 저장
    String fileName;
    // PDF 파일 경로
    String pdfFilePath;
    // 파일저장 경로 설정
    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SharedLectureNote";
    // 처음 여는 강의록인지 확인
    boolean firstOpen = false;
    // pdf pageNumber
    int pageNumber = 0;
    // PDF 총 페이지 수
    int pdfPageCnt = -1;
    // PDF fie
    File pdfFile = null;

    TextView pageText;
    ImageView nextBtn;
    ImageView prevBtn;

    // 사용자 id, 방장 Id
    private String userId, masterId;
    // 방번호
    private int roomNum;
    // url
    private String url;

    // socket IO 소켓
    private Socket ioSocket;
    // file 에 사용할 소켓
    private Socket fileSocket;
    // 음성에 사용할 소켓
    private Socket voiceSocket;
    // socket에 쓸 options 변수
    IO.Options opts = new IO.Options();
    // evet를 처리할 socket.io Listener
    private Emitter.Listener onNewDraw, onNewUser, onExitUser, makeResult, onFile, onPageChange, onSound,  onDelete;

    // Android AsyncHttpClient
    private AsyncHttpClient mHttpClient = new AsyncHttpClient();

    // json객체 성공적으로 받았는지 여부
    int isSuccess;
    // 전송 및 수신에 사용할 JSON객체
    JSONObject jsonObject;
    JSONArray jsonArray;

    // 음성 녹음 및 재생을 하는 스레드와 ruunable 객체
    Thread voicePlayThread, voiceRecordThread;
    Runnable voicePlayTask, voiceRecordTask;
    // 음성을 기록할 바이트 배열
    byte [] voiceRecordByte, voicePlayByte;

    // 전송할때 사용할 변수
    private float x, y, penSize = 1.0f, strSize = 40.0f;
    private String str = "";
    private int pA = 255, pR = 0, pG = 0, pB = 0, sR = 0, sG = 0, sB = 0, penType = 1, typeface = 1, strType = 1, isDraw, isText;
    private Paint paint;

    // 송신할때 사용할 변수
    private float x2, y2, penSize2, strSize2;
    private String str2;
    private int pA2, pR2, pG2, pB2, sR2, sG2, sB2, penType2, typeface2, strType2, isDraw2, isText2;

    // 통신을 위한 JSON객체를 만드는 객체
    private makeDrawJSON json = new makeDrawJSON();

    private class activityTypeClass {
        static final int COLOR_PICK = 0;
        static final int PEN_SELECT = 1;
        static final int TEXT_INSERT = 2;
        static final int PDF_CHOOSE = 3;
    }

    // 픽셀을 DP 로 변환하는 메소드.
    private float pxToDp(Context context, float px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dp = Math.round(px / (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    // DP 를 픽셀로 변환하는 메소드.
    private float dpToPx(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float px = Math.round(dp * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    // 뒤로 가기 버튼 누르는 경우 Activity 완전 종료
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    // Activity가 종료되는 시점에 exitRoom 실행
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 유저가 퇴장하는 것을 JSON으로 전송하기 위해 객체 전송
        jsonObject = new JSONObject();
        try {
            jsonObject.put("ID", userId);
            jsonObject.put("roomNum", roomNum);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 방 퇴장 정보를 전송
        ioSocket.emit("exitRoom", jsonObject);

        // ioSocket 종료
        ioSocket.disconnect();
        fileSocket.disconnect();

        // Event Listener 해제
        ioSocket.off("enterResult", onNewUser).off("exitResult", onExitUser).off("serverToClient", onNewDraw).off("makeResult", makeResult).off("onDelete", onDelete);
        fileSocket.off("pdfDownload", onFile).off("pageChange", onPageChange).off("enterRoomPdf", onFile);

//        voicePlayThread.interrupt();
//        voiceRecordThread.interrupt();
    }

    // Activity가 다시 보이게 됨.
    @Override
    protected void onRestart() {
        super.onRestart();

        // 소켓 재연결
        ioSocket.connect();
        fileSocket.connect();
        voiceSocket.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 스레드 작동
//        voicePlayThread.start();
//        voiceRecordThread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            // colorPick
            case activityTypeClass.COLOR_PICK :
                switch (resultCode) {
                    case 1 :
                        // 넘어오는 데이터 받음
                        pR = data.getIntExtra("r", 0);
                        pG = data.getIntExtra("g", 0);
                        pB = data.getIntExtra("b", 0);
                        // 색변화 적용
                        myDrawView.setPenColor(pA, pR, pG, pB);
                        break;
                }
                break;
            // PenSelect
            case activityTypeClass.PEN_SELECT :
                switch (resultCode) {
                    // 연필 선택, 기본 펜
                    case 1 :
                        // 색상은 불투명하게 굵기는 넘어오는 데이터로 변경
                        penSize = data.getFloatExtra("penSize", 1.0f);
                        penType = data.getIntExtra("penType", 1);
                        pA = 255;
                        myDrawView.setPenColorSize(pA, pR, pG, pB, penSize);
                        break;
                    // 형광펜 선택
                    case 2 :
                        penSize = data.getFloatExtra("penSize", 1.0f);
                        penType = data.getIntExtra("penType", 1);
                        pA = 80;
                        myDrawView.setPenColorSize(pA, pR, pG, pB, penSize);
                        break;
                    // 지우개 선택
                    case 3 :
                        penSize = data.getFloatExtra("penSize", 1.0f);
                        penType = data.getIntExtra("penType", 1);
                        pA = 255;
                        pR = 255;
                        pG = 255;
                        pB = 255;
                        myDrawView.setPenColorSize(pA, pR, pG, pB, penSize);
                        break;
                }
                break;
            // TextInsert
            case activityTypeClass.TEXT_INSERT :
                switch(resultCode) {
                    case 1 :
                        // 색상
                        sR = data.getIntExtra("r", 0);
                        sG = data.getIntExtra("g", 0);
                        sB = data.getIntExtra("b", 0);
                        // 글자크기
                        strSize = data.getFloatExtra("strSize", 1.0f);
                        // 글씨체 종류
                        strType = data.getIntExtra("strType", 1);
                        // 글자 속성
                        typeface = data.getIntExtra("typeface", 1);
                        // 입력된 글자 가져오기
                        str = data.getStringExtra("string");
                        isText = 1;
                        // myDrawView 글자 설정 및 글자 입력 상태로 전환
                        myDrawView.setStr(sR, sG, sB, strType, strSize, typeface, str, isText);
                        Toast.makeText(getApplicationContext(), "화면을 터치하면 텍스트가 입력됩니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            // Open File
            case activityTypeClass.PDF_CHOOSE:
                if(data == null)
                {
                    Toast.makeText(getApplicationContext(), "해당 파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    pdfUri = data.getData();
                    // content URI 를 File URI로 바꾸어 경로를 생성
                    pdfFilePath = convertUri.getPath(getApplicationContext(), pdfUri);

                    if(pdfFilePath.isEmpty())
                    {
                        Toast.makeText(getApplicationContext(), "파일을 선택하지 않았거나 잘못 선택되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        pdfFile = new File(pdfFilePath);

                        // 처음 파일을 여는 경우
                        // 전체 페이지를 얻어옴
                        if (firstOpen) {
                            pdfPageCnt = getPageCount(pdfFile);
                        }

                        // 화면에 출력
                        displayFromUri(pdfUri);
                        // 파일 위치를 /에 따라 잘라서 얻음
                        List<String> tmp = pdfUri.getPathSegments();
                        // List의 마지막은 primary:파일 이름
                        fileName = tmp.get(tmp.size() - 1);

                        // :의 위치 추출
                        int index_t = fileName.indexOf(':');
                        // :위치이후부터 끝까지 추출하면 파일의 이름과 폴더가 추출
                        fileName = fileName.substring(index_t+1);

                        while(fileName.indexOf('/') > 0)
                        {
                            index_t = fileName.indexOf('/');
                            fileName = fileName.substring(index_t+1);
                        }

                        // PDF to Byte Array
                        byte[] pdfByte = convertFile.convertToByteArray(pdfFile);

                        if(pdfByte != null)
                        {
                            jsonObject = new JSONObject();

                            try {
                                jsonObject.put("num", roomNum);
                                jsonObject.put("fileName", fileName);
                                jsonObject.put("pageNum", pageNumber);
                                jsonObject.put("fileData", pdfByte);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            fileSocket.emit("pdfUpload", jsonObject);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "PDF파일의 전송에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    // Permission Check
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getApplicationContext(), "메모리 접근 권한을 주었습니다.", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
            case 1:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getApplicationContext(), "메모리 접근 권한을 주었습니다.", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle("메뉴");

        menu.add(0, 1, 0, "강의록 열기");
        menu.add(0, 2, 0, "방 나가기");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case 1:
                if(isMaster)
                {
                    if(Build.VERSION.SDK_INT >= 19)
                    {
                        // Open 누를때 새로운 강의록을 여므로 페이지 번호 초기화
                        pageNumber = 0;
                        // 처음 여는 것으로 간주
                        firstOpen = true;

                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");

                        // OnActivitiyReusult로 처리
                        startActivityForResult(intent, activityTypeClass.PDF_CHOOSE);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "안드로이드 킷캣 4.4 이상에서만 사용가능합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "방장만이 PDF파일을 열 수 있습니다.", Toast.LENGTH_SHORT).show();
                }
                return true;
            case 2:
                // Exit 누른 경우
                onBackPressed();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 윈도우 자동꺼짐 해제
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_room);

        // 폴더 생성
        File folder = new File(path);
        if(!folder.exists())
        {
            folder.mkdirs();
        }

        // 그림그리는 view
        myDrawView = (DrawView) findViewById(R.id.canvas);
        myDrawView.setBackgroundColor(Color.TRANSPARENT);

        // 그림 받는 View
        otherDrawView = (DrawView) findViewById(R.id.otherCanvas);

        // PDF view
        pdfView = (PDFView) findViewById(R.id.pdfView);

        // 초기화
        paint = myDrawView.getPaintData();

        // 오른쪽에 그리기 속성 버튼으로 사용할 ImageView 3개
        ImageView colorPick = (ImageView) findViewById(R.id.colorPick);
        ImageView penSelect = (ImageView) findViewById(R.id.pen);
        ImageView textInsert = (ImageView) findViewById(R.id.textInsert);

        // 왼쪽에 PDF속성 버튼으로 사용할 ImageView
        nextBtn = (ImageView) findViewById(R.id.nextButton);
        prevBtn = (ImageView) findViewById(R.id.prevButton);

        pageText = (TextView) findViewById(R.id.pageView);

        // 취소 버튼
        ImageView cancelBtn = (ImageView) findViewById(R.id.backButton);
        // 마이크 버튼
        ImageView micBtn = (ImageView) findViewById(R.id.micOnOff);
        // 메뉴 버튼
        final Button menuBtn = (Button) findViewById(R.id.menuButton);

        // 참여자 목록 ListView
        final ListView userList = (ListView) findViewById(R.id.userlist);
        // Adapter
        final ArrayAdapter<String> userListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        // Adapter 연결
        userList.setAdapter(userListAdapter);

        // network 정보를 가져오기 위한 cm 선언 및 System Service 할당
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 현재 활성화된 Network 확인
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        // Intent는 onCreate이후
        Intent intent = getIntent();

        // ID와 url 얻음
        userId = intent.getStringExtra("ID");
        url = intent.getStringExtra("url");
        masterId = intent.getStringExtra("masterId");

        // 어떤 버튼으로 넘어온 것인지 판별을 위한 변수
        String work = intent.getStringExtra("work");

        // Activity 실행을 위한 Intent
        final Intent colorIntent = new Intent(RoomActivity.this, colorPickActivity.class);
        final Intent penSelIntent = new Intent(RoomActivity.this, penSelectActivity.class);
        final Intent textInsIntent = new Intent(RoomActivity.this, textInsertActivity.class);

        // menuBtn에 ContextMenu 등록
        registerForContextMenu(menuBtn);

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuBtn.showContextMenu();
            }
        });

        // 권한 설정
        if(Build.VERSION.SDK_INT >= 23)
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        // colorPick 클릭
        colorPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorIntent.putExtra("work", "penColor");
                colorIntent.putExtra("r", pR);
                colorIntent.putExtra("g", pG);
                colorIntent.putExtra("b", pB);
                startActivityForResult(colorIntent, activityTypeClass.COLOR_PICK);
            }
        });

        // penSelect 클릭
        penSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                penSelIntent.putExtra("penSize", penSize);
                penSelIntent.putExtra("penType", penType);
                startActivityForResult(penSelIntent, activityTypeClass.PEN_SELECT);
            }
        });

        // textInsert 클릭
        textInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textInsIntent.putExtra("r", sR);
                textInsIntent.putExtra("g", sG);
                textInsIntent.putExtra("b", sB);
                textInsIntent.putExtra("strSize", strSize);
                textInsIntent.putExtra("strType", strType);
                textInsIntent.putExtra("typeface", typeface);
                startActivityForResult(textInsIntent, activityTypeClass.TEXT_INSERT);
            }
        });

        // prevBtn 클릭
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber--;
                // pageNumber가 음수로 가는 경우는 없음
                if(pageNumber < 0)
                {
                    pageNumber = 0;
                    Toast.makeText(getApplicationContext(), "첫 페이지 입니다." , Toast.LENGTH_SHORT).show();
                }
                else
                {
                    displayFromUri(pdfUri);
                    pageText.setText(""+pageNumber + " / " + "" + pdfPageCnt);
                    jsonObject = new JSONObject();

                    try {
                        jsonObject.put("ID", userId);
                        jsonObject.put("roomNum", roomNum);
                        jsonObject.put("pageNum", pageNumber);

                        fileSocket.emit("pageChange", jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                myDrawView.clearCanvas();
                otherDrawView.clearCanvas();
            }
        });

        // nextBtn 클릭
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber++;
                // pageNumber 0이 첫페이지
                if(pageNumber >= pdfPageCnt)
                {
                    pageNumber--;
                    Toast.makeText(getApplicationContext(), "마지막 페이지입니다.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    displayFromUri(pdfUri);
                    pageText.setText(""+pageNumber + " / " + "" + pdfPageCnt);
                    jsonObject = new JSONObject();

                    try {
                        jsonObject.put("ID", userId);
                        jsonObject.put("roomNum", roomNum);
                        jsonObject.put("pageNum", pageNumber);

                        fileSocket.emit("pageChange", jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                myDrawView.clearCanvas();
                otherDrawView.clearCanvas();
            }
        });

        // 되돌리기 버튼 누른 경우
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = myDrawView.getDrawObjectCnt();

                if(size > 0)
                {
                    // 현재 클라이언트의 마지막 그리기 정보의 index값
                    // 즉, 내가 그린 그림의 index값을 구하고자 함
                    int idx = myDrawView.getIndex()-1;

                    // drawView에 지우고자하는 index와 userId값을 전달하여 작업수행
                    // delete작업을 받는 리스너에서도 같은 메서드를 사용
                    myDrawView.deleteDrawObject(idx, userId);

                    jsonObject = new JSONObject();

                    try {
                        jsonObject.put("ID", userId);
                        jsonObject.put("index", idx);
                        jsonObject.put("roomNum", roomNum);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // 서버로 전송하여 서버도 삭제하고 이를 브로드캐스트
                    ioSocket.emit("onDelete", jsonObject);
                }
            }
        });

        // 터치를 받는 이벤트
        View.OnTouchListener getTouch = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // 그림그리기 작업인지 string 입력인지 확인을 위한 bool 변수 얻음
                isText = myDrawView.getIsText();

                // 좌표값을 얻음
                float pxX = motionEvent.getX();
                float pxY = motionEvent.getY();

                // 좌표를 dp값으로 변환
                x = pxToDp(getApplicationContext(), pxX);
                y = pxToDp(getApplicationContext(), pxY);

                // 입력에 따른 분류
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isDraw = 1;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        isDraw = 0;
                        break;
                }

                int idx = myDrawView.getIndex();

                // JSON객체로 변환
                jsonObject = json.make(userId, idx, x, y, penSize, penType, str, pA, pR, pG, pB, sR, sG, sB, strSize, strType, typeface, isDraw, isText, roomNum);

                // 객체 전송
                ioSocket.emit("clientToServer", jsonObject);

                if(isText == 1) {
                    isText = 0;
                    str="";
                }

                return false;
            }
        };

        // 터치를 안 받는 이벤트
        View.OnTouchListener notTouch = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        };

        // 새로운 그리기 정보 받는 경우
        onNewDraw = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(args[0] != null) {
                    jsonObject = (JSONObject) args[0];
                    try {
                        isSuccess = jsonObject.getInt("success");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // UI작업은 Handler에서
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 성공한 경우
                        if(isSuccess == 1) {
                            try {
                                // 결과 얻음
                                jsonArray = jsonObject.getJSONArray("result");

                                // JSONArray를 통해 JSONObject를 얻고 drawView로 해당정보 전송
                                for(int i=0; i<jsonArray.length(); i++) {
                                    jsonObject = jsonArray.getJSONObject(i);

                                    String id = jsonObject.getString("userId");
                                    int index = jsonObject.getInt("index");

                                    float pxX = (float) jsonObject.getDouble("x");
                                    float pyY = (float) jsonObject.getDouble("y");
                                    penSize2 = (float) jsonObject.getDouble("penSize");
                                    penType2 = jsonObject.getInt("penType");
                                    str2 = jsonObject.getString("string");
                                    pA2 = jsonObject.getInt("pA");
                                    pR2 = jsonObject.getInt("pR");
                                    pG2 = jsonObject.getInt("pG");
                                    pB2 = jsonObject.getInt("pB");
                                    sR2 = jsonObject.getInt("sR");
                                    sG2 = jsonObject.getInt("sG");
                                    sB2 = jsonObject.getInt("sB");
                                    typeface2 = jsonObject.getInt("typeface");
                                    strSize2 = (float) jsonObject.getDouble("strSize");
                                    strType2 = jsonObject.getInt("strType");
                                    isText2 = jsonObject.getInt("isText");
                                    isDraw2 = jsonObject.getInt("isDraw");

                                    // paint 객체 생성
                                    paint = new Paint();
                                    paint.setColor(Color.argb(pA2, pR2, pG2, pB2));
                                    paint.setStrokeWidth(penSize2);
                                    paint.setAntiAlias(true);

                                    // dp값 px로 변환
                                    x2 = dpToPx(getApplicationContext(), pxX);
                                    y2 = dpToPx(getApplicationContext(), pyY);

                                    drawData d = new drawData(id, x2, y2, sR2, sG2, sB2, penType2, typeface2, str2, strSize2, strType2, paint, isDraw2, isText2, index);
                                    // drawView로 data 전달
                                    otherDrawView.sendDrawData(d);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // success 0
                            Toast.makeText(getApplicationContext(), "서버로부터 정보를 받지 못하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };

        // 유저가 들어온 경우
        onNewUser = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(args[0] != null) {
                    jsonObject = (JSONObject) args[0];
                    try {
                        isSuccess = jsonObject.getInt("success");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // UI작업은 Handler에서
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // user정보 받아오는데 성공하면
                        if(isSuccess == 1) {
                            userListAdapter.clear();
                            try {
                                // 사용자 정보 Array 얻음
                                JSONArray userArray = jsonObject.getJSONArray("userList");
                                for(int i = 0; i < userArray.length(); i++) {
                                    String user = userArray.getString(i);
                                    // listView 데이터 추가
                                    userListAdapter.add(user);
                                }
                                // 리스트뷰 갱신
                                userListAdapter.notifyDataSetChanged();

                                // 유저가 막 들어온 경우
                                if(myDrawView.getDrawObjectCnt() == 0 && !userId.equals(masterId)) {
                                    JSONArray drawArray = jsonObject.getJSONArray("result");
                                    // 그림정보가 있는 경우 추가 작업 실시
                                    for(int i = 0; i < drawArray.length(); i++) {
                                        jsonObject = drawArray.getJSONObject(i);

                                        Log.d("json", ""+jsonObject);

                                        String id = jsonObject.getString("userId");
                                        int index = jsonObject.getInt("index");
                                        float pxX = (float) jsonObject.getDouble("x");
                                        float pyY = (float) jsonObject.getDouble("y");
                                        penSize2 = (float) jsonObject.getDouble("penSize");
                                        penType2 = jsonObject.getInt("penType");
                                        str2 = jsonObject.getString("string");
                                        if(str2 == null) {
                                            str2 = "";
                                        }
                                        pA2 = jsonObject.getInt("pA");
                                        pR2 = jsonObject.getInt("pR");
                                        pG2 = jsonObject.getInt("pG");
                                        pB2 = jsonObject.getInt("pB");
                                        sR2 = jsonObject.getInt("sR");
                                        sG2 = jsonObject.getInt("sG");
                                        sB2 = jsonObject.getInt("sB");
                                        typeface2 = jsonObject.getInt("typeface");
                                        strSize2 = (float) jsonObject.getDouble("strSize");
                                        strType2 = jsonObject.getInt("strType");
                                        isText2 = jsonObject.getInt("isText");
                                        isDraw2 = jsonObject.getInt("isDraw");

                                        // paint 객체 생성
                                        paint = new Paint();
                                        paint.setColor(Color.argb(pA2, pR2, pG2, pB2));
                                        paint.setStrokeWidth(penSize2);
                                        paint.setAntiAlias(true);

                                        // dp값 px로 변환
                                        x2 = dpToPx(getApplicationContext(), pxX);
                                        y2 = dpToPx(getApplicationContext(), pyY);

                                        drawData d = new drawData(id, x2, y2, sR2, sG2, sB2, penType2, typeface2, str2, strSize2, strType2, paint, isDraw2, isText2, index);
                                        // drawView로 data 전달
                                        otherDrawView.sendDrawDataWithoutInvalidate(d);
                                    }
                                    // drawView에 데이터 넣는 작업이 완료되면 view 갱신
                                    otherDrawView.invalidate();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        };

        // User가 나가는 경우
        onExitUser = new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                if(args[0] != null) {
                    jsonObject = (JSONObject) args[0];

                    try {
                        isSuccess = jsonObject.getInt("success");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // UI작업은 Handler에서
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(isSuccess == 1) {
                            try {
                                // 나간 user의 ID를 얻음
                                String user = jsonObject.getString("result");

                                if(user.equals(masterId)) {
                                    // 방장이 방을 나간 경우
                                    // Activity 종료
                                    finish();
                                } else {
                                    // 방장이 아닌 경우
                                    // 해당 ID를 목록에서 삭제
                                    userListAdapter.remove(user);
                                    userListAdapter.notifyDataSetChanged();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // success 0
                            Toast.makeText(getApplicationContext(), "서버로부터 정보를 받지 못하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };

        // 방생성 후 방번호를 받기 위한 Listener
        makeResult = new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                if(args[0] != null) {
                    jsonObject = (JSONObject) args[0];
                    try {
                        isSuccess = jsonObject.getInt("success");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(isSuccess == 0) {
                            Toast.makeText(getApplicationContext(), "방을 생성하는데에 실패하였습니다.\n다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // 성공시 방번호 얻어옴
                            try {
                                roomNum = jsonObject.getInt("result");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        };

        // PDF 파일 받는 리스너
        onFile = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(args[0] != null)
                {
                    jsonObject = (JSONObject) args[0];
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // ByteArray To PDF file
                        if(jsonObject != null)
                        {
                            int success;
                            byte [] pdfByte;

                            try {
                                success = jsonObject.getInt("success");
                                if(success == 1)
                                {
                                    fileName = jsonObject.getString("fileName");
                                    pageNumber = jsonObject.getInt("pageNum");
                                    // Object로 Data를 가져온 뒤 byte 배열로 cast
                                    pdfByte = (byte []) jsonObject.get("fileData");
                                    // convert byte array to PDF
                                    boolean complete = convertFile.convertToPdf(pdfByte, path, fileName);

                                    // 변환 후 화면에 띄우기
                                    if(complete)
                                    {
                                        pdfFile = new File(path+"/"+fileName);
                                        pdfPageCnt = getPageCount(path+"/"+fileName);

                                        // File을 통해 화면에 표시
                                        displayFromFile(pdfFile);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "파일 다운로드를 실패하였습니다.\n다시 입장해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        };

        // page이동을 받는 리스너
        onPageChange = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(args[0] != null)
                {
                    jsonObject = (JSONObject )args[0];
                }

                // UI 작업은 메인스레드에서 되도록 핸들러
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int success = jsonObject.getInt("success");

                            if(success == 1)
                            {
                                pageNumber = jsonObject.getInt("pageNum");
                                displayFromFile(pdfFile);
                                myDrawView.clearCanvas();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "페이지 이동정보를 받지 못하였습니다.", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        // 삭제작업
        onDelete = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(args[0] != null)
                {
                    jsonObject = (JSONObject) args[0];
                }

                // UI작업은 메인스레드에서 작업
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int success = jsonObject.getInt("success");
                            if(success == 1)
                            {
                                int idx = jsonObject.getInt("index");
                                String id = jsonObject.getString("ID");

                                otherDrawView.deleteDrawObject(idx, id);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        };

        // opts setting
        opts.forceNew = true;
        opts.reconnection = false;

        // socekt 객체에 소켓생성
        try{
            // port 3000 setting
            ioSocket = IO.socket(url+":3000", opts);
            // PDF file Only
            fileSocket = IO.socket(url + ":3001", opts);
            // Sound Only
            voiceSocket = IO.socket(url + ":3002", opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // socket에서 Handle할 Listener 등록
        ioSocket.on("enterResult", onNewUser).on("exitResult", onExitUser).on("serverToClient", onNewDraw).on("makeResult", makeResult).on("onDelete", onDelete);
        // 파일 관련 작업은 fileSocket에서 처리
        fileSocket.on("pdfDownload", onFile).on("pageChange", onPageChange).on("enterRoomPdf", onFile);
        // 음성관련 작업은 voiceSocket에서 처리

        // network check
        if(activeNetwork == null) {
            // 네트워크 연결이 없는 경우
            Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
        } else if(activeNetwork.getType() != ConnectivityManager.TYPE_WIFI && activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE) {
            // 네트워크 연결이 없는 경우
            Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
        } else {
            // Socket 연결
            ioSocket.connect();
            fileSocket.connect();
            voiceSocket.connect();
        }

        // 전송에 사용할 JSON객체 초기화
        jsonObject = new JSONObject();

        try {
            jsonObject.put("ID", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 방생성인지 입장인지 판별
        switch (work) {
            case "makeRoom" :
                // Master
                isMaster = true;
                // 방생성 정보를 전송
                ioSocket.emit("makeRoom", jsonObject);
                fileSocket.emit("makeRoom", jsonObject);
                voiceSocket.emit("makeRoom", jsonObject);

                // ListView에 masterID 입력
                userListAdapter.add(userId);
                userListAdapter.notifyDataSetChanged();
                break;
            case "enterRoom" :
                // not Master
                isMaster = false;
                // 방번호 얻어옴
                roomNum = intent.getIntExtra("roomNum", 0);
                try {
                    jsonObject.put("roomNum", roomNum);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 입장 정보를 전송
                ioSocket.emit("enterRoom", jsonObject);
                fileSocket.emit("enterRoom", jsonObject);
                voiceSocket.emit("enterRoom", jsonObject);

                break;
            default:
                break;
        }

        myDrawView.setUserId(userId);

        otherDrawView.setOnTouchListener(notTouch);

        // 방장인지 아닌지에 따라 버튼을 보이고 안 보이고
        if(isMaster)
        {
            // 안내
            Toast.makeText(getApplicationContext(), "왼쪽 하단의 메뉴버튼을 눌러 PDF 강의록을 열 수 있습니다.", Toast.LENGTH_SHORT).show();

            // myDrawView 터치가능한 TouchListenner등록
            myDrawView.setOnTouchListener(getTouch);
            cancelBtn.setVisibility(View.VISIBLE);
        }
        else
        {
            // myDrawView 터치못하도록 TouchListenner 등록
//            myDrawView.setOnTouchListener(notTouch);
            // Master 가 아닌경우 PDF관련 버튼 안보임
//            penSelect.setVisibility(View.GONE);
//            colorPick.setVisibility(View.GONE);
//            textInsert.setVisibility(View.GONE);

            myDrawView.setOnTouchListener(getTouch);
            cancelBtn.setVisibility(View.VISIBLE);
        }

        // 녹음 하는 스레드

        // 음성은 스레드를 통해 작업
        final int min = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        // Main 스레드에 작업실시
        final Handler voiceRecordHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
        voiceRecordTask = new Runnable() {
            @Override
            public void run() {
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, min, AudioTrack.MODE_STREAM);
                voiceRecordByte  = new byte[min];

                // 오디오 싱크에 pcm데이터 기록
                audioTrack.write(voiceRecordByte, 0, voiceRecordByte.length);
                // 남아있는 버퍼 초기화
                audioTrack.flush();
                // 재생
                audioTrack.play();
                // 해제
                audioTrack.release();
                // 초기화
                audioTrack = null;

                Message msg = new Message();
                voiceRecordHandler.sendMessage(msg);
            }
        };
        voiceRecordThread = new Thread(voiceRecordTask);

        // 재생하는 스레드
        final Handler voicePlayHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);


            }
        };
        voicePlayTask = new Runnable() {
            @Override
            public void run() {
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, min, AudioTrack.MODE_STREAM);
                voicePlayByte  = new byte[min];

                // buffer에 json객체로부터 받은 버퍼를 입력

                // 오디오 싱크에 pcm데이터 기록
                audioTrack.write(voicePlayByte, 0, voicePlayByte.length);
                // 남아있는 버퍼 초기화
                audioTrack.flush();
                // 재생
                audioTrack.play();
                // 해제
                audioTrack.release();
                // 초기화
                audioTrack = null;

                Message msg = new Message();
                voicePlayHandler.sendMessage(msg);
            }
        };
        voicePlayThread = new Thread(voicePlayTask);
    }

    // PDF의 총 페이지 수를 받음
    private int getPageCount(String path)
    {
        File f = new File(path);
        pdfView.fromFile(f)
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        // nothing to do
                        Log.d("PAGE", ""+pdfView.getPageCount());
                    }
                })
                .load();

        return pdfView.getPageCount();
    }

    private int getPageCount(File f)
    {
        pdfView.fromFile(f)
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        // nothing to do
                        Log.d("PAGE", ""+pdfView.getPageCount());
                    }
                })
                .load();

        return pdfView.getPageCount();
    }

    // URI로 PDF 화면에 출력
    private void displayFromUri(Uri uri)
    {
        // RGB_565에서 ARGB_8888로 강제변경
        // 화질 좋아짐
        // 메모리 소비 커짐
        pdfView.useBestQuality(true);

        pdfView.fromUri(uri)
                .enableSwipe(false)
                .swipeHorizontal(false)
                .enableDoubletap(false)
                .scrollHandle(null)
                .pages(pageNumber)
                .onLoad(this)
                .load();
    }

    // URI로 PDF 화면에 출력
    private void displayFromFile(File file)
    {
        // RGB_565에서 ARGB_8888로 강제변경
        // 화질 좋아짐
        // 메모리 소비 커짐
        pdfView.useBestQuality(true);

        pdfView.fromFile(file)
                .enableSwipe(false)
                .swipeHorizontal(false)
                .enableDoubletap(false)
                .scrollHandle(null)
                .pages(pageNumber)
                .onLoad(this)
                .load();
    }

    @Override
    public void loadComplete(int nbPages) {
        // pdf불러오기가 완료되면 하는 작업
        if(firstOpen)
        {
            Toast.makeText(getApplicationContext(), "강의록을 열었습니다.", Toast.LENGTH_SHORT).show();
            firstOpen = false;

            // 강의록 불러오기 전에 한 그리기 작업 모두 삭제
            myDrawView.clearCanvas();
            otherDrawView.clearCanvas();
        }

        // nbPages 는 총 페이지수와 같다
        pdfPageCnt = nbPages;

        if(isMaster)
        {
            nextBtn.setVisibility(View.VISIBLE);
            prevBtn.setVisibility(View.VISIBLE);
        }
        // pageView setting
        pageText.setText(""+ (pageNumber+1) + " / " + "" + pdfPageCnt);
        pageText.setVisibility(View.VISIBLE);
    }
}
