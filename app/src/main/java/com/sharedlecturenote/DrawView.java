package com.sharedlecturenote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

/**
 * Created by DJ on 2016-05-17.
 */

public class DrawView extends View {
    // 그림정보 저장할 ArrayList
    private ArrayList<drawData> drawObject;
    // Paint 객체
    private Paint paintData, nowDrawing;
    // 사용할 변수
    private float x, y, penSize, strSize;
    private String str ="", userId;
    private int pA, pR, pG, pB, sR, sG, sB, penType, typeface, strType, isDraw, isText, roomNum, index;

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
        drawObject = new ArrayList<drawData> ();

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
        strSize = 20.0f;
        strType = 1;
        isText = 0;
        index = 1;

        // Paint 객체 초기화
        paintData = new Paint();
        nowDrawing = new Paint();
        paintData.setStrokeWidth(penSize);
        paintData.setAntiAlias(true);
        paintData.setColor(Color.argb(pA, pR, pG, pB));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // canvse 배경 투명하게 초기화
        canvas.drawColor(Color.TRANSPARENT);
        for(int i = 0; i < drawObject.size(); i++) {
            // isDraw가 0 이면 선이므로 이전 객체와 연결
            if(drawObject.get(i).isDraw == 0) {
                if(i > 0)
                {
                    if(drawObject.get(i-1).userId.equals(drawObject.get(i).userId) && drawObject.get(i-1).index == drawObject.get(i).index)
                        canvas.drawLine(drawObject.get(i-1).x, drawObject.get(i-1).y, drawObject.get(i).x, drawObject.get(i).y, drawObject.get(i).paint);
                }
                else
                {
                    canvas.drawLine(drawObject.get(i-1).x, drawObject.get(i-1).y, drawObject.get(i).x, drawObject.get(i).y, drawObject.get(i).paint);
                }
            } else {
                // isDraw값이 1로 ACTION_DOWN인 경우
                if(drawObject.get(i).isText == 1) {
                    // isText == true

                    // text 색상 설정
                    nowDrawing.setColor(Color.rgb(drawObject.get(i).sR, drawObject.get(i).sG, drawObject.get(i).sB));
                    // 글자 크기
                    nowDrawing.setTextSize(drawObject.get(i).strSize);
                    // 글자속성
                    if(drawObject.get(i).typeface == 1) {
                        // Typeface.DEFAULT
                        switch(drawObject.get(i).penType) {
                            case 1 :
                                // Typeface.NORMAL
                                nowDrawing.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                                break;
                            case 2 :
                                // Typeface.BOLD
                                nowDrawing.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                                break;
                            case 3 :
                                // Typeface.ITALIC
                                nowDrawing.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
                                break;
                        }
                    } else if(drawObject.get(i).typeface == 2) {
                        // Typeface.MONOSPACE
                        switch(drawObject.get(i).penType) {
                            case 1 :
                                // Typeface.NORMAL
                                nowDrawing.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
                                break;
                            case 2 :
                                // Typeface.BOLD
                                nowDrawing.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
                                break;
                            case 3 :
                                // Typeface.ITALIC
                                nowDrawing.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC));
                                break;
                        }
                    } else if(drawObject.get(i).typeface == 3) {
                        // Typeface.SANS_SERIF
                        switch(drawObject.get(i).penType) {
                            case 1 :
                                // Typeface.NORMAL
                                nowDrawing.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
                                break;
                            case 2 :
                                // Typeface.BOLD
                                nowDrawing.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
                                break;
                            case 3 :
                                // Typeface.ITALIC
                                nowDrawing.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC));
                                break;
                        }
                    } else {
                        // Typeface.SERIF
                        switch(drawObject.get(i).penType) {
                            case 1 :
                                // Typeface.NORMAL
                                nowDrawing.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
                                break;
                            case 2 :
                                // Typeface.BOLD
                                nowDrawing.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
                                break;
                            case 3 :
                                // Typeface.ITALIC
                                nowDrawing.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
                                break;
                        }
                    }
                    nowDrawing.setAntiAlias(true);
                    // Text 그림
                    canvas.drawText(drawObject.get(i).str, drawObject.get(i).x, drawObject.get(i).y, nowDrawing);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        drawData d;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDraw = 1;

                d = new drawData(userId, x, y, sR, sG, sB, penType, typeface, str, strSize, strType, paintData, isDraw, isText, index);
                drawObject.add(d);

                // 텍스트 입력이 된 경우 텍스트 입력상태 해제
                if(isText == 1) {
                    isText = 0;
                    str="";
                }
                // 화면 갱신
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                isDraw = 0;

                d = new drawData(userId, x, y, sR, sG, sB, penType, typeface, str, strSize, strType, paintData, isDraw, isText, index);
                drawObject.add(d);

                // 텍스트 입력이 된 경우 텍스트 입력상태 해제
                if(isText == 1) {
                    isText = 0;
                    str="";
                }
                // 화면 갱신
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                index++;
                break;
        }
        return true;
    }

    // dwarData를 받아 추가하는 메서드
    public void sendDrawData(drawData d) {
        drawObject.add(d);
        // 화면 갱신
        invalidate();
    }

    // 갱신없이 그림정보 추가
    public void sendDrawDataWithoutInvalidate(drawData d) {
        drawObject.add(d);
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

    public void setUserId(String id)
    {
        userId = id;
    }

    public void clearCanvas()
    {
        drawObject.clear();
        invalidate();
    }

    public int getDrawObjectCnt()
    {
        return drawObject.size();
    }

    public int getIndex()
    {
        return index;
    }

    public void deleteDrawObject(int idx, String id)
    {
        int size = drawObject.size();

        for(int i = size-1; i >= 0; i--)
        {
            // 내가 그린 그림만 지워짐
            // 그림의 주체가 같은 경우 지우기 실시
            if(drawObject.get(i).userId.equals(id))
            {
                int j = i;
                // 그림 객체의 index 값을 얻음
                int ind = drawObject.get(i).index;
                // 사용자 아이디가 동일하고 index값이 같은 경우 하나의 객체
                while(drawObject.get(j).userId.equals(id) && drawObject.get(j).index == idx)
                {
                    // 지우기
                    drawObject.remove(j);
                    j--;

                    if(j < 0)
                    {
                        break;
                    }
                }
                break;
            }
        }
        // 그림객체를 지웠으므로 index 1감소
        index--;
        // 화면 갱신
        invalidate();
    }
}