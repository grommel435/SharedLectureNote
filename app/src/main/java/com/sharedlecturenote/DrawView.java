package com.sharedlecturenote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

/**
 * Created by DJ on 2016-05-17.
 */
public class DrawView extends View {
    // 그림정보 저장할 ArrayList
    private ArrayList<drawData> data;
    // Paint 객체
    private Paint paintData;
    // 사용할 변수
    private float x, y, penSize, strSize;
    private String str ="";
    private int pA, pR, pG, pB, sR, sG, sB, penType, typeface, strType, isDraw, isText;

    public int getIsDraw() {
        return isDraw;
    }

    public Paint getPaintData() {
        return paintData;
    }

    public int getIsText() {
        return isText;
    }

    public void setIsText(int text) {
        isText = text;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // 초기화
    public void init() {
        // ArrayList 초기화
        data = new ArrayList<drawData> ();
        // 변수 초기값으로 초기화
        penSize = 1.0f;
        penType = 1;
        typeface = 1;
        str = "";
        pA = 255;
        pR = 0;
        pG = 0;
        pB = 0;
        sR = 0;
        sG = 0;
        sB = 0;
        strSize = 10.0f;
        strType = 1;
        isText = 0;

        // Paint 객체 초기화
        paintData = new Paint();
        paintData.setStrokeWidth(penSize);
        paintData.setAntiAlias(true);
        paintData.setColor(Color.argb(pA, pR, pG, pB));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // canvse 흰색으로 초기화
        canvas.drawColor(Color.TRANSPARENT);
        for(int i = 0; i < data.size(); i++) {
            // isDraw가 0 이면 선이므로 이전 객체와 연결
            if(data.get(i).isDraw == 0) {
                canvas.drawLine(data.get(i-1).x, data.get(i-1).y, data.get(i).x, data.get(i).y, data.get(i).paint);
            } else {
                // isDraw값이 1로 ACTION_DOWN인 경우
                if(data.get(i).isText == 1) {
                    // isText == true
                    Paint sPaint = new Paint();
                    // text 색상 설정
                    sPaint.setColor(Color.rgb(data.get(i).sR, data.get(i).sG, data.get(i).sB));
                    // 글자 크기
                    sPaint.setTextSize(data.get(i).strSize);
                    // 글자속성
                    if(data.get(i).typeface == 1) {
                        // Typeface.DEFAULT
                        switch(data.get(i).penType) {
                            case 1 :
                                // Typeface.NORMAL
                                sPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                                break;
                            case 2 :
                                // Typeface.BOLD
                                sPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                                break;
                            case 3 :
                                // Typeface.ITALIC
                                sPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
                                break;
                        }
                    } else if(data.get(i).typeface == 2) {
                        // Typeface.MONOSPACE
                        switch(data.get(i).penType) {
                            case 1 :
                                // Typeface.NORMAL
                                sPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
                                break;
                            case 2 :
                                // Typeface.BOLD
                                sPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
                                break;
                            case 3 :
                                // Typeface.ITALIC
                                sPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC));
                                break;
                        }
                    } else if(data.get(i).typeface == 3) {
                        // Typeface.SANS_SERIF
                        switch(data.get(i).penType) {
                            case 1 :
                                // Typeface.NORMAL
                                sPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
                                break;
                            case 2 :
                                // Typeface.BOLD
                                sPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
                                break;
                            case 3 :
                                // Typeface.ITALIC
                                sPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC));
                                break;
                        }
                    } else {
                        // Typeface.SERIF
                        switch(data.get(i).penType) {
                            case 1 :
                                // Typeface.NORMAL
                                sPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
                                break;
                            case 2 :
                                // Typeface.BOLD
                                sPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
                                break;
                            case 3 :
                                // Typeface.ITALIC
                                sPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
                                break;
                        }
                    }
                    sPaint.setAntiAlias(true);
                    // Text 그림
                    canvas.drawText(data.get(i).str, data.get(i).x, data.get(i).y, sPaint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDraw = 1;
                break;
            case MotionEvent.ACTION_MOVE:
                isDraw = 0;
                break;
            default:
                break;
        }

        drawData d = new drawData(x, y, sR, sG, sB, penType, typeface, str, strSize, strType, paintData, isDraw, isText);
        data.add(d);

        // 텍스트 입력이 된 경우 텍스트 입력상태 해제
        if(isText == 1) {
            isText = 0;
            str="";
        }
        // 화면 갱신
        invalidate();

        return true;
    }

    // dwarData를 받아 추가하는 메서드
    public void sendDrawData(drawData d) {
        data.add(d);
        // 화면 갱신
        invalidate();
    }

    // 갱신없이 그림정보 추가
    public void sendDrawDataWithoutInvalidate(drawData d) {
        data.add(d);
    }

    // 펜 변화 받아 적용하는 메서드
    public void setPenColorSize (int pA_, int pR_, int pG_, int pB_, float penSize_) {
        // 설정
        penSize = penSize_;
        pA = pA_;
        pR = pR_;
        pG = pG_;
        pB = pB_;

        paintData = new Paint();
        paintData.setColor(Color.argb(pA, pR, pG, pB));
        paintData.setStrokeWidth(penSize);
        paintData.setAntiAlias(true);
    }

    // 색상 변화 받아 적용하는 메서드
    public void setPenColor (int pA_, int pR_, int pG_, int pB_) {
        // 설정
        pA = pA_;
        pR = pR_;
        pG = pG_;
        pB = pB_;

        paintData = new Paint();
        paintData.setColor(Color.rgb(pR, pG, pB));
        paintData.setStrokeWidth(penSize);
        paintData.setAntiAlias(true);
    }

    // 글자 설정
    public void setStr(int r, int g, int b, int strType_, float strSize_, int typeface_, String str, int isText_) {
        sR = r;
        sG = g;
        sB = b;
        strType = strType_;
        strSize = strSize_;
        typeface = typeface_;
        this.str = str;
        isText = isText_;
    }

    // ArrayList Data 현황 반환
    public int getDrawDataSize () {
        return data.size();
    }
}
