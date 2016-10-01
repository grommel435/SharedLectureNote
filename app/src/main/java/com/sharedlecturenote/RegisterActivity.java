package com.sharedlecturenote;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class RegisterActivity extends AppCompatActivity {
    // AsyncHtpClient 객체
    private AsyncHttpClient mHttpClient;
    // Network Check 위한 Connectivity Manger 변수
    private ConnectivityManager cm;
    // 현재 활성화된 network 정보를 얻기 위한 NetworkInfo 변수
    private NetworkInfo activeNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regster);

        // url 저장
        Intent intent = getIntent();
        final String url = intent.getStringExtra("url") + "/register";

        // 버튼 및 위젯 등록
        Button accButton = (Button)findViewById(R.id.acceptButtom);
        Button cancelButton = (Button)findViewById(R.id.cancelButtom);
        final EditText id = (EditText)findViewById(R.id.idEdit);
        final EditText pw = (EditText)findViewById(R.id.pwEdit);

        // login Activity로 이동위한 intent
        final Intent loginIntent = new Intent(this, LoginActivity.class);

        // network 정보를 가져오기 위한 cm 선언 및 System Service 할당
        cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (accButton != null) {
            accButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String strId = id.getText().toString();
                    String strPw = pw.getText().toString();

                    // POST방식으로 서버 전송
                    mHttpClient = new AsyncHttpClient();
                    RequestParams loginData = new RequestParams();

                    // 현재 활성화된 Network 확인
                    activeNetwork = cm.getActiveNetworkInfo();

                    if(strId.isEmpty()) {
                        // ID 확인
                        Toast.makeText(getApplicationContext(), "아이디를 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                    } else if(strPw.isEmpty()) {
                        // pw 확인
                        Toast.makeText(getApplicationContext(), "비밀번호를 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                    } else if(activeNetwork.getType() != ConnectivityManager.TYPE_WIFI && activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE) {
                        // network 연결이 안된 경우
                        Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
                    } else if(activeNetwork == null) {
                        // network 연결이 안된 경우
                        Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // id와 pw가 모두 있는 경우
                        loginData.add("ID", strId);
                        loginData.add("PW", strPw);

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
                                        Toast.makeText(getApplicationContext(), "회원가입 성공", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        // 그 외의 경우 실패
                                        Toast.makeText(getApplicationContext(), "회원가입을 실패하였습니다.\n다시 시도해주세요.", Toast.LENGTH_SHORT).show();
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

        if (cancelButton != null) {
            cancelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public  void onClick(View view) {
                    // cancel Button은 이전 화면으로 이동
                    finish();
                }
            });
        }
    }
}
