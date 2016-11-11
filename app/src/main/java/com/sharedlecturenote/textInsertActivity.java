package com.sharedlecturenote;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class textInsertActivity extends Activity {

    private class strTypeClass {
        static final int NORMAL = 1;
        static final int BOLD = 2;
        static final int TILT = 3;
    }

    // 글자종류, 글자속성
    int typeface, strType;
    // 글자크기
    float strSize;
    // 글자색상
    int r, g, b;

    int resultCode;

    // OnActivityResult에서 사용하기 위해 먼저 선언
    ImageView strColor;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(resultCode) {
            case 1 :
                // 색상 얻음
                r = data.getIntExtra("r", 0);
                g = data.getIntExtra("g", 0);
                b = data.getIntExtra("b", 0);
                // 색상으로 변경
                strColor.setBackgroundColor(Color.rgb(r, g, b));
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_insert);

        // 글꼴 선택 dropdown
        Spinner typefaceSpinner = (Spinner) findViewById(R.id.typefaceSelect);
        // 글꼴 크기 Edit
        EditText sizeEdit = (EditText) findViewById(R.id.strSizeEdit);
        // 글자색상 변경 ImageView
        strColor = (ImageView) findViewById(R.id.strColorView);

        // 글자 속성
        final ImageView textNormal = (ImageView) findViewById(R.id.textNormal);
        final ImageView textBold = (ImageView) findViewById(R.id.textBold);
        final ImageView textTilt = (ImageView) findViewById(R.id.textTilt);

        // 글자 쓰기
        final TextView strView = (TextView) findViewById(R.id.stringView);
        final EditText strEdit = (EditText) findViewById(R.id.editString);

        Button accBtn = (Button) findViewById(R.id.strAcc);
        Button cancelBtn = (Button) findViewById(R.id.strCancel);

        // 전달받은 값 얻어오기
        Intent intent = getIntent();
        r = intent.getIntExtra("r", 0);
        g = intent.getIntExtra("g", 0);
        b = intent.getIntExtra("b", 0);
        typeface = intent.getIntExtra("typeface", 1);
        strType = intent.getIntExtra("strType", 1);
        strSize = intent.getFloatExtra("strSize", 40.0f);

        // 전달받은 값으초 초기화
        strColor.setBackgroundColor(Color.rgb(r, g, b));
        // typeface 값은 1, 2, 3 각각 momospace, sans_serif, serif
        // Spinner는 0,1 2
        typefaceSpinner.setSelection(typeface-1);
        // textView set
        strView.setText("");
        // 글씨체
        switch(typeface) {
            case 1 :
                strView.setTypeface(Typeface.DEFAULT);
                break;
            case 2 :
                strView.setTypeface(Typeface.MONOSPACE);
                break;
            case 3 :
                strView.setTypeface(Typeface.SANS_SERIF);
                break;
            case 4:
                strView.setTypeface(Typeface.SERIF);
                break;
        }
        // 글씨색상
        strView.setTextColor(Color.rgb(r, g, b));
        // 글씨 속성
        switch(strType) {
            case 1 :
                strView.setTypeface(null, Typeface.NORMAL);
                break;
            case 2 :
                strView.setTypeface(null, Typeface.BOLD);
                break;
            case 3 :
                strView.setTypeface(null, Typeface.ITALIC);
                break;
        }

        // 글꼴 속성 선택
        switch(strType) {
            case strTypeClass.NORMAL :
                textNormal.setColorFilter(Color.rgb(0, 0, 0), android.graphics.PorterDuff.Mode.MULTIPLY);
                textBold.clearColorFilter();
                textTilt.clearColorFilter();
                break;
            case strTypeClass.BOLD :
                strType = strTypeClass.BOLD;
                textNormal.clearColorFilter();
                textBold.setColorFilter(Color.rgb(0, 0, 0), android.graphics.PorterDuff.Mode.MULTIPLY);
                textTilt.clearColorFilter();
                break;
            case strTypeClass.TILT :
                strType = strTypeClass.TILT;
                textNormal.clearColorFilter();
                textBold.clearColorFilter();
                textTilt.setColorFilter(Color.rgb(0, 0, 0), android.graphics.PorterDuff.Mode.MULTIPLY);
                break;
        }

        // 글꼴 선택값
        typefaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                typeface = i+1;

                switch(typeface) {
                    case 1 :
                        strView.setTypeface(Typeface.DEFAULT);
                        break;
                    case 2 :
                        strView.setTypeface(Typeface.MONOSPACE);
                        break;
                    case 3 :
                        strView.setTypeface(Typeface.SANS_SERIF);
                        break;
                    case 4:
                        strView.setTypeface(Typeface.SERIF);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // EditText 초기값
        sizeEdit.setText(String.valueOf(strSize));

        // 올바른 크기가 입력되었는지 확인 후 값 저장
        sizeEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // 값을 얻음
                String input = textView.getText().toString();
                float size = Float.valueOf(input);

                if(size > 100.f) {
                    Toast.makeText(getApplicationContext(), "최대값은 100입니다.", Toast.LENGTH_SHORT).show();
                } else if(size <= 0) {
                    Toast.makeText(getApplicationContext(), "0보다 큰수를 입력하여 주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    // 값 적용
                    strSize = size;
                }
                return false;
            }
        });

        // 글자 속성
        textNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1
                strType = strTypeClass.NORMAL;
                textNormal.setColorFilter(Color.rgb(0, 0, 0), android.graphics.PorterDuff.Mode.MULTIPLY);
                textBold.clearColorFilter();
                textTilt.clearColorFilter();

                // 글씨 속성 보통
                strView.setTypeface(null, Typeface.NORMAL);
            }
        });

        textBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 2
                strType = strTypeClass.BOLD;
                textNormal.clearColorFilter();
                textBold.setColorFilter(Color.rgb(0, 0, 0), android.graphics.PorterDuff.Mode.MULTIPLY);
                textTilt.clearColorFilter();

                // 글씨 속성 보통
                strView.setTypeface(null, Typeface.BOLD);
            }
        });

        textTilt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 3
                strType = strTypeClass.TILT;
                textNormal.clearColorFilter();
                textBold.clearColorFilter();
                textTilt.setColorFilter(Color.rgb(0, 0, 0), android.graphics.PorterDuff.Mode.MULTIPLY);

                // 글씨 속성 보통
                strView.setTypeface(null, Typeface.BOLD);
            }
        });

        // 글자 색상
        strColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // colorPickActivity 실행
                Intent colorIntent = new Intent(textInsertActivity.this, colorPickActivity.class);
                colorIntent.putExtra("r", r);
                colorIntent.putExtra("g", g);
                colorIntent.putExtra("b", b);
                startActivityForResult(colorIntent, 0);

                // 글자색상 바로 보여지게
                strView.setTextColor(Color.rgb(r, g, b));
            }
        });

        // 글자 입력시
        strEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 변화를 바로 textView에 입력
                strView.setText(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        accBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultCode = 1;

                // 입력된 string 얻음
                String str = strEdit.getText().toString();
                if(str.length() > 50) {
                    Toast.makeText(getApplicationContext(), "글자는 최대 50글자를 넘을 수 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent resultData = new Intent();
                    resultData.putExtra("r", r);
                    resultData.putExtra("g", g);
                    resultData.putExtra("b", b);
                    resultData.putExtra("strSize", strSize);
                    resultData.putExtra("typeface", typeface);
                    resultData.putExtra("strType", strType);
                    resultData.putExtra("string", str);
                    setResult(resultCode, resultData);
                    finish();
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
