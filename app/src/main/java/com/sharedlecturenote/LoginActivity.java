package com.sharedlecturenote;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {
    // server url
    String url = "http://52.79.167.107";
    // AsyncHtpClient 객체
    private AsyncHttpClient mHttpClient;
    // Network Check 위한 Connectivity Manger 변수
    private ConnectivityManager cm;
    // 현재 활성화된 network 정보를 얻기 위한 NetworkInfo 변수
    private NetworkInfo activeNetwork;
    // id, pw
    private String strId = "", strPw="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Register Activity 이동을 위한 intent
        final Intent regIntent = new Intent(this, RegisterActivity.class);
        final Intent listIntent = new Intent(this, RoomListActivity.class);

        // Intent 로 url전달
        regIntent.putExtra("url", url);
        listIntent.putExtra("url", url);

        // 로그인 url
        url = url + "/login";

        // login, exit 버튼 위젯
        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        Button registerBtn = (Button) findViewById(R.id.registerBtn);

        // id, pw 입력 위젯
        final EditText idText = (EditText) findViewById(R.id.idEdit);
        final EditText pwText = (EditText) findViewById(R.id.pwEdit);

        // network 정보를 가져오기 위한 cm 선언 및 System Service 할당
        cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (loginBtn != null) {
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // login 버튼 클릭시
                    // id와 pw를 문자열로 반환
                    if(idText != null && pwText != null) {
                        strId = idText.getText().toString();
                        strPw = pwText.getText().toString();
                    }

                    // POST방식으로 서버 전송
                    mHttpClient = new AsyncHttpClient();
                    RequestParams loginData = new RequestParams();

                    // 현재 활성화된 Network 확인
                    activeNetwork = cm.getActiveNetworkInfo();

                    if(strId.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "아이디를 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                    } else if(strPw.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "비밀번호를 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                    } else if(activeNetwork == null) {
                        // 네트워크 연결이 없는 경우
                        Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
                    } else if(activeNetwork.getType() != ConnectivityManager.TYPE_WIFI && activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE) {
                        // 네트워크 연결이 없는 경우
                        Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // ID 확인
                        loginData.put("ID", strId);
                        // pw 확인
                        loginData.put("PW", strPw);

                        mHttpClient.post(getApplicationContext(), url, loginData, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                // 2진 데이터값을 String으로 변환
                                String str = new String(responseBody);
                                // String 값을 이용해 JSONObject 생성
                                try {
                                    JSONObject jsonObject = new JSONObject(str);
                                    // JSONObject 에서 success 값 가져옴
                                    int success = jsonObject.getInt("success");
                                    // success가 1이면 성공
                                    if(success == 1) {
                                        Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();

                                        // ID를 activity로 전달
                                        listIntent.putExtra("ID", strId);

                                        // id, pw란 지우기
                                        idText.setText("");
                                        pwText.setText("");

                                        startActivity(listIntent);
                                    } else {
                                        // 그 외의 경우 실패
                                        String error = jsonObject.getString("work");
                                        // 서버로부터 받은 error 메시지를 출력
                                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(getApplicationContext(), "서버와의 통신을 실패하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            });
        }

        if (registerBtn != null) {
            registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // id, pw란 지우기
                    if (idText != null) {
                        idText.setText("");
                    }
                    if (pwText != null) {
                        pwText.setText("");
                    }

                    // Register Activity로 이동
                    startActivity(regIntent);
                }
            });
        }
    }
}
