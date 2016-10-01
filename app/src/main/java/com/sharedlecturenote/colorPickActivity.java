package com.sharedlecturenote;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

public class colorPickActivity extends Activity {

    int resultCode = 0;

    // 색상값
    int r, g, b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_pick);

        // 색상 보여주는 View
        final ImageView colorView = (ImageView) findViewById(R.id.colorView);

        //EditText
        final EditText editR = (EditText) findViewById(R.id.editR);
        final EditText editB = (EditText) findViewById(R.id.editB);
        final EditText editG = (EditText) findViewById(R.id.editG);

        // SeekBar
        final SeekBar Rseek = (SeekBar) findViewById(R.id.seekBarR);
        final SeekBar Gseek = (SeekBar) findViewById(R.id.seekBarG);
        final SeekBar Bseek = (SeekBar) findViewById(R.id.seekBarB);

        // 색상 선택 ImageView
        final ImageView redSelect = (ImageView) findViewById(R.id.redSelect);
        final ImageView bluedSelect = (ImageView) findViewById(R.id.blueSelect);
        final ImageView greenSelect = (ImageView) findViewById(R.id.greenSelect);
        final ImageView yellowSelect = (ImageView) findViewById(R.id.yellowSelect);
        final ImageView blackSelect = (ImageView) findViewById(R.id.blackSelct);

        // Button
        Button accBtn = (Button) findViewById(R.id.colorAcc);
        Button cancelBtn = (Button) findViewById(R.id.colorCancel);

        // 넘어온 intent 얻음
        Intent intent = getIntent();
        r = intent.getIntExtra("r", 0);
        g = intent.getIntExtra("g", 0);
        b = intent.getIntExtra("b", 0);

        // 현재 색상을 보여준다
        colorView.setBackgroundColor(Color.rgb(r, g, b));

        // 현재 값으로 seekBar 처리
        Rseek.setProgress(r);
        Gseek.setProgress(g);
        Bseek.setProgress(b);

        // EditText처리
        editR.setText(String.valueOf(r));
        editG.setText(String.valueOf(g));
        editB.setText(String.valueOf(b));

        // red SeekBar 이동 처리
        Rseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // R값 설정
                r = i;

                // EditText처리
                editR.setText(String.valueOf(r));

                // 현재 색상을 보여준다
                colorView.setBackgroundColor(Color.rgb(r, g, colorPickActivity.this.b));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // green SeekBar 이동 처리
        Gseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // G값설정
                g = i;

                // EditText처리
                editG.setText(String.valueOf(g));

                // 현재 색상을 보여준다
                colorView.setBackgroundColor(Color.rgb(r, g, colorPickActivity.this.b));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // blue SeekBar 이동처리
        Bseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // B값설정
                colorPickActivity.this.b = i;

                // EditText처리
                editB.setText(String.valueOf(colorPickActivity.this.b));

                // 현재 색상을 보여준다
                colorView.setBackgroundColor(Color.rgb(r, g, colorPickActivity.this.b));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // 빨강 선택시
        redSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // RGB 색상값 설정
                r = 255;
                g = 0;
                b = 0;

                // SeekBar 설정
                Rseek.setProgress(r);
                Gseek.setProgress(g);
                Bseek.setProgress(b);

                // EditText설정
                editR.setText(String.valueOf(r));
                editG.setText(String.valueOf(g));
                editB.setText(String.valueOf(b));

                // colorView 설정
                colorView.setBackgroundColor(Color.rgb(r, g, b));
            }
        });

        // 녹색 선택시
        greenSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // RGB 색상값 설정
                r = 0;
                g = 255;
                b = 0;

                // SeekBar 설정
                Rseek.setProgress(r);
                Gseek.setProgress(g);
                Bseek.setProgress(b);

                // EditText설정
                editR.setText(String.valueOf(r));
                editG.setText(String.valueOf(g));
                editB.setText(String.valueOf(b));

                // colorView 설정
                colorView.setBackgroundColor(Color.rgb(r, g, b));
            }
        });

        // 파랑 선택시
        bluedSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // RGB 색상값 설정
                r = 0;
                g = 0;
                b = 255;

                // SeekBar 설정
                Rseek.setProgress(r);
                Gseek.setProgress(g);
                Bseek.setProgress(b);

                // EditText설정
                editR.setText(String.valueOf(r));
                editG.setText(String.valueOf(g));
                editB.setText(String.valueOf(b));

                // colorView 설정
                colorView.setBackgroundColor(Color.rgb(r, g, b));
            }
        });

        // 노랑 선택시
        yellowSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // RGB 색상값 설정
                r = 255;
                g = 255;
                b = 0;

                // SeekBar 설정
                Rseek.setProgress(r);
                Gseek.setProgress(g);
                Bseek.setProgress(b);

                // EditText설정
                editR.setText(String.valueOf(r));
                editG.setText(String.valueOf(g));
                editB.setText(String.valueOf(b));

                // colorView 설정
                colorView.setBackgroundColor(Color.rgb(r, g, b));
            }
        });

        // 검정 선택시
        blackSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // RGB 색상값 설정
                r = 0;
                g = 0;
                b = 0;

                // SeekBar 설정
                Rseek.setProgress(r);
                Gseek.setProgress(g);
                Bseek.setProgress(b);

                // EditText설정
                editR.setText(String.valueOf(r));
                editG.setText(String.valueOf(g));
                editB.setText(String.valueOf(b));

                // colorView 설정
                colorView.setBackgroundColor(Color.rgb(r, g, b));
            }
        });

        // 설정 버튼
        accBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultCode = 1;
                // 전달할 data
                Intent resultData = new Intent();
                resultData.putExtra("r", r);
                resultData.putExtra("g", g);
                resultData.putExtra("b", b);

                setResult(resultCode, resultData);
                finish();
            }
        });

        // 취소 버튼
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

