package com.sharedlecturenote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class RoomActivity extends Activity {
    // DrawView
    private DrawView drawView;

    // 사용자 id, 방장 Id
    private String userId, masterId;
    // 방번호
    private int roomNum;
    // url
    private String url;

    // socket IO 소켓
    private Socket ioSocket;
    // socket에 쓸 options 변수
    IO.Options opts = new IO.Options();
    // evet를 처리할 socket.io Listener
    private Emitter.Listener onNewDraw, onNewUser, onExitUser, makeResult;

    // json객체 성공적으로 받았는지 여부
    int isSuccess;
    // 전송 및 수신에 사용할 JSON객체
    JSONObject jsonObject;
    JSONArray jsonArray;

    // 전송할때 사용할 변수
    private float x, y, penSize = 1.0f, strSize = 10.0f;
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

    // Menu 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //add( Group Id, Item Id, Order, Title);
        menu.add(0, 1, 0, R.string.exit);

        return super.onCreateOptionsMenu(menu);
    }

    // Activity가 종료되는 시점에 exitRoom 실행
    @Override
    protected void onStop() {
        super.onStop();

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
        // 연결 종료 후 Event Listener도 종료
        ioSocket.off("enterResult", onNewUser).off("exitResult", onExitUser).off("serverToClient", onNewDraw).off("makeResult", makeResult);
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
                        drawView.setPenColor(pA, pR, pG, pB);
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
                        drawView.setPenColorSize(pA, pR, pG, pB, penSize);
                        break;
                    // 형광펜 선택
                    case 2 :
                        penSize = data.getFloatExtra("penSize", 1.0f);
                        penType = data.getIntExtra("penType", 1);
                        pA = 80;
                        drawView.setPenColorSize(pA, pR, pG, pB, penSize);
                        break;
                    // 지우개 선택
                    case 3 :
                        penSize = data.getFloatExtra("penSize", 1.0f);
                        penType = data.getIntExtra("penType", 1);
                        pA = 255;
                        pR = 255;
                        pG = 255;
                        pB = 255;
                        drawView.setPenColorSize(pA, pR, pG, pB, penSize);
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
                        // drawView 글자 설정 및 글자 입력 상태로 전환
                        drawView.setStr(sR, sG, sB, strType, strSize, typeface, str, isText);
                        break;
                }
                Toast.makeText(getApplicationContext(), "화면을 터치하면 텍스트가 입력됩니다.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // Menu 선택
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case 1:
                // Exit 누른 경우
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 윈도우 자동꺼짐 해제
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_room);

        // 그림그리는 부분
        drawView = (DrawView) findViewById(R.id.canvas);

        // 초기화
        paint = drawView.getPaintData();

        // 버튼으로 사용할 ImageView 3개
        ImageView colorPick = (ImageView) findViewById(R.id.colorPick);
        ImageView penSelect = (ImageView) findViewById(R.id.pen);
        ImageView textInsert = (ImageView) findViewById(R.id.textInsert);

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

        // drawView에 터치입력이 들어온 경우
        drawView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // 그림그리기 작업인지 string 입력인지 확인을 위한 bool 변수 얻음
                isText = drawView.getIsText();

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
                    default:
                        break;
                }

                // JSON객체로 변환
                jsonObject = json.make(userId, roomNum,  x,  y, penSize, penType, str, pA, pR, pG, pB, sR, sG, sB, strSize, strType, typeface, isDraw, isText);

                // 객체 전송
                ioSocket.emit("clientToServer", jsonObject);

                if(isText == 1) {
                    isText = 0;
                    str="";
                }

                return false;
            }
        });

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

                                    drawData d = new drawData(x2, y2, sR2, sG2, sB2, penType2, typeface2, str2, strSize2, strType2, paint, isDraw2, isText2);
                                    // drawView로 data 전달
                                    drawView.sendDrawData(d);
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
                                if(drawView.getDrawDataSize() == 0 && !userId.equals(masterId)) {
                                    JSONArray drawArray = jsonObject.getJSONArray("result");
                                    // 그림정보가 있는 경우 추가 작업 실시
                                    for(int i = 0; i < drawArray.length(); i++) {
                                        jsonObject = drawArray.getJSONObject(i);

                                        Log.d("json", ""+jsonObject);

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

                                        drawData d = new drawData(x2, y2, sR2, sG2, sB2, penType2, typeface2, str2, strSize2, strType2, paint, isDraw2, isText2);
                                        // drawView로 data 전달
                                        drawView.sendDrawDataWithoutInvalidate(d);
                                    }
                                    // drawView에 데이터 넣는 작업이 완료되면 view 갱신
                                    drawView.invalidate();
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

        // opts setting
        opts.forceNew = true;
        opts.reconnection = false;

        // socekt 객체에 소켓생성
        try{
            // port 3000 setting
            ioSocket = IO.socket(url+":3000", opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // socket에서 Handle할 Listener 등록
        ioSocket.on("enterResult", onNewUser).on("exitResult", onExitUser).on("serverToClient", onNewDraw).on("makeResult", makeResult);

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
                // 방생성 정보를 전송
                ioSocket.emit("makeRoom", jsonObject);
                // ListView에 masterID 입력
                userListAdapter.add(userId);
                userListAdapter.notifyDataSetChanged();
                break;
            case "enterRoom" :
                // 방번호르 얻어옴
                roomNum = intent.getIntExtra("roomNum", 0);
                try {
                    jsonObject.put("roomNum", roomNum);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // 입장 정보를 전송
                ioSocket.emit("enterRoom", jsonObject);
                break;
            default:
                break;
        }
    }
}
