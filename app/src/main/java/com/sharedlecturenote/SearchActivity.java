package com.sharedlecturenote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends Activity {
    // AsyncHttpClient
    private AsyncHttpClient mHttpClient;
    // Search str
    private  String str = null;
    // str Code
    private int resultCode = -1;
    //str Intent
    private final Intent intent = new Intent();
    // Network Check 위한 Connectivity Manger 변수
    private ConnectivityManager cm;
    // 현재 활성화된 network 정보를 얻기 위한 NetworkInfo 변수
    private NetworkInfo activeNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 창크기를 width에 꽉 차게한다.
        // 첫번째 parameter Width, 두번째 parameter Height
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final EditText searchId = (EditText) findViewById(R.id.searchIdInput);
        Button searchBtn = (Button) findViewById(R.id.subSearch);
        Button cancelBtn = (Button) findViewById(R.id.subCancel);

        // network 정보를 가져오기 위한 cm 선언 및 System Service 할당
        cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        final Intent intent = getIntent();
        final String url = intent.getStringExtra("url") + "/search";

        mHttpClient = new AsyncHttpClient();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditText String 얻음
                String searchIdString = searchId.getText().toString();

                // 검색하기 위한 master ID RequestParameter에 입력
                RequestParams searchParam = new RequestParams();
                searchParam.put("searchId", searchIdString);

                // 현재 활성화된 Network 확인
                activeNetwork = cm.getActiveNetworkInfo();

                // network check
                if(activeNetwork == null) {
                    // 네트워크 연결이 없는 경우
                    Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
                } else if(activeNetwork.getType() != ConnectivityManager.TYPE_WIFI && activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE) {
                    // 네트워크 연결이 없는 경우
                    Toast.makeText(getApplicationContext(), "데이터 네트워크 또는 Wifi에 연결해야 합니다.", Toast.LENGTH_SHORT).show();
                } else if(searchIdString.isEmpty()) {
                    // 검색할 ID를 입력하지 않은 경우
                    Toast.makeText(getApplicationContext(), "검색할 ID를 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    // 검색 실시
                    mHttpClient.post(getApplicationContext(), url, searchParam, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            //결과 값을 받은 경우
                            str = new String(responseBody);
                            // startActivityForReuslt로 data를 전달하기 위해 Intent에 입력
                            intent.putExtra("jsonData", str);
                            // startActivityForResult의 결과값
                            resultCode = 1;
                            setResult(resultCode, intent);
                            finish();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            // startActivityForResult의 결과값
                            resultCode = 0;
                            setResult(resultCode);
                            finish();
                        }
                    });
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultCode = -1;
                setResult(resultCode);
                finish();
            }
        });
    }
}
