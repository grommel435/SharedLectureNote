package com.sharedlecturenote;

/**
 * Created by DJ on 2016-05-01.
 */

// roomData를 받아서 ListView에 넣기 위한 class 선언
public class roomData {
    private int num;
    private String id;

    // 방번호와 마스터ID를 인자로 받는 생성자 선언
    public roomData(int n, String i) {
        num = n;
        id =i;
    }

    // 방번호 지정 메소드
    public void setNum(int n) {
        num = n;
    }

    // master ID 지정 메소드
    public void setId(String i) {
        id = i;
    }

    // 방번호 얻어오는 메서드
    public int getNum() {
        return num;
    }

    // master ID 언어오는 메서드
    public  String getId() {
        return id;
    }
}
