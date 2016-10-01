package com.sharedlecturenote;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class penSelectActivity extends Activity {

    int resultCode;
    float penSize;
    int penType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pen_select);

        final ImageView pen = (ImageView) findViewById(R.id.pen);
        final ImageView eraser = (ImageView) findViewById(R.id.eraser);
        final ImageView highlighter = (ImageView) findViewById(R.id.highlighter);

        final EditText penSIzeEdit = (EditText) findViewById(R.id.penSizeEdit);
        final SeekBar penSizeBar = (SeekBar) findViewById(R.id.penSizeBar);

        Button AcceptButton = (Button) findViewById(R.id.setPen);
        Button cancel = (Button) findViewById(R.id.cancelPen);

        // 넘어온 데이터
        Intent intent = getIntent();

        // 펜 크기
        penSize = intent.getFloatExtra("penSize", 1.0f);

        // 펜 종류
        penType = intent.getIntExtra("penType", 1);

        // 넘어온 penSize를 EditText에 입력
        penSIzeEdit.setText(Float.toString(penSize));
        // SeekBar 최대값
        penSizeBar.setMax((20-1)*10);

        // penSize로 SeekBar 위치 계산
        int p = (int) (penSize - 1.0f) * 10;
        // SeekBar 초기위치 설정
        penSizeBar.setProgress(p);

        // 넘어온 값으로 화면 설정
        switch (penType) {
            case 1 :
                pen.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
                resultCode = 1;
                break;
            case 2 :
                highlighter.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
                resultCode = 2;
                break;
            case 3 :
                eraser.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
                resultCode = 3;
                break;
        }

        // EditText 입력 후 완료 버튼을 누르는 경우
        penSIzeEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                String input = textView.getText().toString();
                float data = Float.valueOf(input);
                // 공백인 경우
                if(input.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "공백은 불가능합니다.", Toast.LENGTH_SHORT).show();
                }
                // 범위를 넘는 경우
                else if(data > 20.0f) {
                    Toast.makeText(getApplicationContext(), "최대값은 20을 넘을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                else if(data < 1.0f) {
                    Toast.makeText(getApplicationContext(), "최소값은 1미만일 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    // 넘지 않는 경우 Bar 위치 변경
                    int p = (int) ((penSize - 1.0f) * 10);
                    penSizeBar.setProgress(p);
                    penSize = data;
                }

                return false;
            }
        });

        // SeekBar Event
        penSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // 이동 중에 EditText변환
                float x = 1.0f + (i * 0.1f);
                // 소수점 한 자리까지만 출력
                String position = String.format(Locale.KOREAN, "%.1f", x);
                penSIzeEdit.setText(position);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // pencil 선택
        pen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pen.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
                eraser.clearColorFilter();
                highlighter.clearColorFilter();

                // 크기 1로 설정
                penSizeBar.setProgress(0);
                penSIzeEdit.setText("1.0");
                penSize = 1.0f;
                penType = 1;

                resultCode = 1;
            }
        });


        // 형광펜 선택
        highlighter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pen.clearColorFilter();
                eraser.clearColorFilter();
                highlighter.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);

                // 크기 최대로 설정
                penSizeBar.setProgress((20-1) * 10);
                penSIzeEdit.setText("20.0");
                // 크기와 타입 설정
                penSize = 20.f;
                penType = 2;

                resultCode = 2;
            }
        });

        // 지우개 선택
        eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pen.clearColorFilter();
                eraser.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
                highlighter.clearColorFilter();

                // 크기 최대로 설정
                penSizeBar.setProgress((20-1) * 10);
                penSIzeEdit.setText("20.0");
                // 크기와 타입 설정
                penSize = 20.f;
                penType = 3;

                resultCode = 3;
            }
        });

        // 설정 저장 버튼
        AcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 허용범위에 맞는지 Check
                String input = penSIzeEdit.getText().toString();
                penSize = Float.valueOf(input);

                // 범위를 넘는 경우
                if(penSize > 20.0f) {
                    Toast.makeText(getApplicationContext(), "최대값은 20을 넘을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                // 넘지 않으면 종료
                else {
                    Intent resultData = new Intent();

                    resultData.putExtra("penSize", penSize);
                    resultData.putExtra("penType", penType);

                    setResult(resultCode, resultData);
                    finish();
                }
            }
        });

        // 종료 버튼
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(-1);
                finish();
            }
        });
    }

    // 정수를 float으로 변환
    public float convertFloat(int intVal) {
        float floatVal = 0.0f;
        floatVal = .1f * intVal;
        return floatVal;
    }
}
