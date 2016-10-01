package com.sharedlecturenote;

import android.graphics.Paint;

/**
 * Created by DJ on 2016-05-17.
 */
public class drawData {
    float x;
    float y;
    int sR,  sG, sB;
    int penType;
    String str;
    float strSize;
    // 글꼴
    int typeface;
    int strType;
    int isDraw, isText;
    Paint paint;

    // 최종적으로 사용할 것
    public drawData(float x_, float y_, int sR_, int sG_, int sB_, int penType_, int typeface_, String str_, float strSize_, int strType_, Paint paint_, int isDraw_, int isText_) {
        x = x_;
        y = y_;
        sR = sR_;
        sG = sG_;
        sB = sB_;
        penType = penType_;
        typeface = typeface_;
        // 문자열 부분은 null값 가능
        str = str_;
        strSize = strSize_;
        strType = strType_;
        paint = paint_;
        // string 입력 여부
        isText = isText_;
        // 점과 선 구분
        isDraw = isDraw_;
    }
}
