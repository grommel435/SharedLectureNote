package com.sharedlecturenote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class RoomListActivity extends Activity {
    // adapter 생성
    roomListAdapter adapter;
    // AsyncHtpClient 객체
    private AsyncHttpClient mHttpClient = new AsyncHttpClient();;
    // listVIew item
    private roomData selectedItem;
    // 방번호
    int roomNum = 0;
    // 현재 활성화된 network 정보를 얻기 위한 NetworkInfo 변수
    private NetworkInfo activeNetwork;

    // URL
    String listUrl;

    // search 를 통해 값을 받는 것에 대한 작업
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(resultCode)
        {
            // 얻어오는 것을 성공한 경우
            case 1:
                // Intent에서 값을 가져옴
                String str = data.getStringExtra("jsonData");

                // String 객체를 JSONArray 객체로 변환
                try {
                    JSONObject jsonObject = new JSONObject(str);

                    // success 여부
                    if(jsonObject.getInt("success") == 1) {
                        JSONArray jsonArray = jsonObject.getJSONArray("result");

                        // adapter 초기화
                        adapter.clear();
                        // jsonArray에서 JSONObject 객체 가져옴
                        for(int i = 0; i < jsonArray.length(); i++) {
                            JSONObject tempObject = jsonArray.getJSONObject(i);
                            // 방번호와 master ID를 가져옴
                            String masterId = tempObject.getString("masterId");
                            int roomNum = tempObject.getInt("roomNum");

                            roomData item = new roomData(roomNum, masterId);
                            adapter.additem(item);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getApplicationContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case 0:
                // 실패한 경우
                Toast.makeText(getApplicationContext(), "검색에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                break;

            case -1:
                // 사용자 요청으로 종료된 경우
                break;

            default:
                Toast.makeText(getApplicationContext(), "Unknown Error", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // RoomActivity에서 나오는 경우 자동으로 목록을 최신화 하기 위해 onStart에서 구현
    @Override
    protected void onStart() {

        // network 정보를 가져오기 위한 cm 선언 및 System Service 할당
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 현재 활성화된 Network 확인
        activeNetwork = cm.getActiveNetworkInfo();

        // network check
        if(activeNetwork == null) {
            // 네트워크 연결이 없는 경우
            Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
        } else if(activeNetwork.getType() != ConnectivityManager.TYPE_WIFI && activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE) {
            // 네트워크 연결이 없는 경우
            Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
        } else {
            // 목록을 한 번 비움
            adapter.clear();
            // 서버로부터 방목록을 받음
            mHttpClient.get(getApplicationContext(), listUrl, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    // get으로 받은 데이타를 String 형태로 변환
                    String str = new String(responseBody);
                    try {
                        // JSONObject 형태로 변환
                        JSONObject jsonObject = new JSONObject(str);
                        // 성공여부 확인
                        int success = jsonObject.getInt("success");
                        // 성공한 경우
                        if(success == 1) {
                            // JSON object에서 JSON array를 가져옴
                            JSONArray jsonArray = jsonObject.getJSONArray("result");
                            if(jsonArray.isNull(0)) {
                                Toast.makeText(getApplicationContext(), "참여 가능한 방목록이 없습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                // 배열을 순회하며 data를 가져온다.
                                for(int i=0; i < jsonArray.length(); i++) {
                                    JSONObject item = jsonArray.getJSONObject(i);
                                    int roomNum = item.getInt("roomNum");
                                    String masterId = item.getString("masterId");

                                    // 얻은 방번호와 masterId로 새로운 roomData 변수를 생성
                                    roomData d = new roomData(roomNum, masterId);
                                    // Adapter에 추가
                                    adapter.additem(d);
                                }
                                // adapter에 data새로고침 전달
                                adapter.notifyDataSetChanged();
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "방 목록을 가져오는데 실패하였습니다.", Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getApplicationContext(), "서버로부터 목록을 가져오는데 실패하였습니다.", Toast.LENGTH_LONG).show();
                }
            });
        }

        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roomlist);

        // url 받음
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        // Login ID를 가져옴.
        String userId = intent.getStringExtra("ID");

        // RequestParam 에 ID정보를 입력
        RequestParams idData = new RequestParams();
        idData.put("ID", userId);

        // 방생성 URL
        final String makeUrl = url + "/makeroom";
        // 방입장 URL
        final String enterUrl = url + "/enter";
        // 방목록 URL
        listUrl = url + "/roomlist";

        // loginActivity Intent
        final Intent loginIntent = new Intent(RoomListActivity.this, LoginActivity.class);

        // SearchActivity Intent
        final Intent searchIntent = new Intent(RoomListActivity.this, SearchActivity.class);
        searchIntent.putExtra("url", url);

        // roomActivity Intent
        final Intent roomIntent = new Intent(RoomListActivity.this, RoomActivity.class);
        roomIntent.putExtra("ID", userId);
        roomIntent.putExtra("url", url);

        final ListView list = (ListView)findViewById(R.id.roomList);
        final ImageButton refresh = (ImageButton)findViewById(R.id.refresh);
        final ImageButton search = (ImageButton)findViewById(R.id.search);

        Button makeRoomBtn = (Button) findViewById(R.id.makeRoomButton);
        Button enterRoomBtn = (Button) findViewById(R.id.enterButton);
        Button logoutBtn = (Button) findViewById(R.id.logoutButton);

        // 단일 선택모드로 설정
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // adapter 생성
        adapter = new roomListAdapter();

        // listView에 adapter 전달
        list.setAdapter(adapter);

        // listView Item 클릭
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // roomData 정보를 얻는다
                selectedItem = (roomData) list.getItemAtPosition(i);
                // adapter에 선택된 배열정보 전달
                adapter.setChecked(i);
                // adapter 정보 변경 알림
                adapter.notifyDataSetChanged();
            }
        });

        // 새로고침버튼
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // network check
                if(activeNetwork == null) {
                    // 네트워크 연결이 없는 경우
                    Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
                } else if(activeNetwork.getType() != ConnectivityManager.TYPE_WIFI && activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE) {
                    // 네트워크 연결이 없는 경우
                    Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "목록을 새로 고칩니다.", Toast.LENGTH_SHORT).show();
                    // adapter clear
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    // 서버로부터 방목록을 받음
                    mHttpClient.get(getApplicationContext(), listUrl, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            // get으로 받은 데이타를 String 형태로 변환
                            String str = new String(responseBody);
                            try {
                                // JSONObject 형태로 변환
                                JSONObject jsonObject = new JSONObject(str);
                                // 성공여부 확인
                                int success = jsonObject.getInt("success");
                                // 성공한 경우
                                if(success == 1) {
                                    // JSON object에서 JSON array를 가져옴
                                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                                    if(jsonArray.length() == 0) {
                                        Toast.makeText(getApplicationContext(), "참여 가능한 방목록이 없습니다.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // 배열을 순회하며 data를 가져온다.
                                        for(int i=0; i < jsonArray.length(); i++) {
                                            JSONObject item = jsonArray.getJSONObject(i);
                                            int roomNum = item.getInt("roomNum");
                                            String masterId = item.getString("masterId");

                                            // 얻은 방번호와 masterId로 새로운 roomData 변수를 생성
                                            roomData d = new roomData(roomNum, masterId);
                                            // Adapter에 추가
                                            adapter.additem(d);
                                        }
                                        // adapter에 data새로고침 전달
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "방 목록을 가져오는데 실패하였습니다.", Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getApplicationContext(), "서버로부터 목록을 가져오는데 실패하였습니다.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        // 검색 버튼
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(searchIntent, 1);
            }
        });

        // 방생성 버튼
        makeRoomBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // 방생성과 입장을 구분
                roomIntent.putExtra("work", "makeRoom");
                // network check
                if(activeNetwork == null) {
                    // 네트워크 연결이 없는 경우
                    Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
                } else if(activeNetwork.getType() != ConnectivityManager.TYPE_WIFI && activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE) {
                    // 네트워크 연결이 없는 경우
                    Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(roomIntent);
                }
            }
        });

        // 방입장 버튼
        enterRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedItem != null) {
                    // 선택된 item에서 msterId 와 방번호 얻음
                    String masterId = selectedItem.getId();
                    roomNum = selectedItem.getNum();

                    // RequestParams에 선택된 item의 masterId와 방번호를 넣는다.
                    RequestParams enterParam = new RequestParams();
                    enterParam.put("masterId" , masterId);
                    enterParam.put("roomNum", roomNum);

                    // Intent에 방번호 추가
                    roomIntent.putExtra("roomNum", roomNum);
                    // Intent에 masterID 추가
                    roomIntent.putExtra("masterId", masterId);
                    // 방생성과 입장을 구분
                    roomIntent.putExtra("work", "enterRoom");

                    // network check
                    if(activeNetwork == null) {
                        // 네트워크 연결이 없는 경우
                        Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
                    } else if(activeNetwork.getType() != ConnectivityManager.TYPE_WIFI && activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE) {
                        // 네트워크 연결이 없는 경우
                        Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(roomIntent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "입장할 방을 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Log Out 버튼
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(loginIntent);
                finish();
            }
        });
    }
}

