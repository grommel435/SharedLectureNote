package com.sharedlecturenote;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class SplashActivity extends AppCompatActivity {
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    // Splash Image를 Activity 전체에 설정하여 이미지를 띄우는 Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        final Intent intent = new Intent(this, LoginActivity.class);

        // Hnalder 정의
        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                // 바로 종료하는 Handler
                finish();

                startActivity(intent);
            }
        };

        // 해당 handler를 1500ms 실행
        handler.sendEmptyMessageDelayed(0, 1500);
    }
}
