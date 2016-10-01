package com.sharedlecturenote;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static java.lang.String.valueOf;

/**
 * Created by DJ on 2016-05-01.
 */
public class roomListAdapter extends BaseAdapter {
    // Adapter에서 쓰기 위한 데이터타입 ArrayList
    private ArrayList<roomData> listItemList = new ArrayList<roomData>();
    // 라디오 버튼 check를 위한 boolean Array
    private ArrayList <Boolean> isChecked;



    public roomListAdapter() {
        isChecked = new ArrayList<Boolean>();
    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    // i번째에 위치한 item의 view 구현
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Context context = viewGroup.getContext();

        // ItemLayout을 inflate하여 view획득
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_layout, viewGroup, false);
        }

        // Widget 연결
        RadioButton selectButton = (RadioButton) view.findViewById(R.id.selectButton);
        TextView roomNumber = (TextView) view.findViewById(R.id.itemRoomNum);
        TextView masterId = (TextView) view.findViewById(R.id.itemMasterId);

        // 라디오 버튼 클릭 못하도록 설정
        selectButton.setClickable(false);
        // 포커스 받지 못하도록
        selectButton.setFocusable(false);
        selectButton.setChecked(isChecked.get(i));

        // 해당 위치의 ArrayList 배열의 원소 하나 얻음
        roomData d = listItemList.get(i);

        // 방 번호와 master ID를 TextView에 설정
        roomNumber.setText(Integer.toString(d.getNum()));
        masterId.setText(d.getId());

        // 작업한 view 반환
        return view;
    }

    @Override
    public Object getItem(int i) {
        return listItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // Adapter의 ArrayList에 item 하나 추가
    public void addItem(int n, String i) {
        roomData item = new roomData(n, i);
        listItemList.add(item);
        isChecked.add(false);
    }

    public void additem(roomData d) {
        listItemList.add(d);
        isChecked.add(false);
    }

    public void clear()
    {
        listItemList.clear();
        isChecked.clear();
    }

    // item select
    public void setChecked(int i) {
        // i번째 원소를 true
        isChecked.set(i, true);
        for(int j = 0; j<isChecked.size(); j++)
        {
            if(j != i)
            {
                isChecked.set(j, false);
            }
        }
    }
}
