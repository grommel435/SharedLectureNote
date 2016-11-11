package com.sharedlecturenote;

import android.graphics.Paint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by DJ on 2016-05-23.
 */
public class makeDrawJSON {
    // 최종적으로 사용할 JSON 객체
    public JSONObject make(String userId, int index, float x, float y, float penSize, int penType, String str, int pA, int pR, int pG, int pB, int sR, int sG, int sB, float srtSize, int strType, int typeface, int isDraw, int isText, int roomNum) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("userId", userId);
            jsonObject.put("intdex", index);
            jsonObject.put("x", x);
            jsonObject.put("y", y);
            jsonObject.put("penSize", penSize);
            jsonObject.put("penType", penType);
            jsonObject.put("string", str);
            jsonObject.put("pA", pA);
            jsonObject.put("pR", pR);
            jsonObject.put("pG", pG);
            jsonObject.put("pB", pB);
            jsonObject.put("sR", sR);
            jsonObject.put("sG", sG);
            jsonObject.put("sB", sB);
            jsonObject.put("strSize", srtSize);
            jsonObject.put("strType", strType);
            jsonObject.put("typeface", typeface);
            jsonObject.put("isDraw", isDraw);
            jsonObject.put("isText", isText);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 최종 JSON객체
        JSONObject result = new JSONObject();
        try {
            result.put("ID", userId);
            result.put("roomNum", roomNum);
            result.put("result", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
}
