package com.example.myapplication.Interface;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.myapplication.R;
import com.example.myapplication.Toolkit.SQLiteHelper;
import com.example.myapplication.Components.SwipeBackActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zbm阿铭 on 2017/5/18.
 */

public class HistoryActivity extends SwipeBackActivity {
    private ListView mList;
    private Button historyBack,emptyHistory;
    private SQLiteOpenHelper mOpenHelper;
    private ArrayList<Map<String,Object>> mHistoryData;
    private ArrayList<Integer> historyID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOpenHelper=new SQLiteHelper(this,"historyDB",null,1);
        mHistoryData=new ArrayList<>();
        historyID=new ArrayList<>();
        setContentView(R.layout.history);
        getHistory();
        mList=(ListView)findViewById(R.id.history_list);
        historyBack=(Button)findViewById(R.id.history_back);
        historyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        final SimpleAdapter adapter=new SimpleAdapter(this,mHistoryData, android.R.layout.simple_list_item_2,
                new String[]{"网页","网址"},new int[]{android.R.id.text1,android.R.id.text2});
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent();
                intent.putExtra("currentUri",mHistoryData.get(position).get("网址").toString());
                SQLiteDatabase db=mOpenHelper.getWritableDatabase();
                db.delete(SQLiteHelper.TB_NAME,"historyID=?",new String[]{String.valueOf(historyID.get(position))});
                getHistory();
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("zbm","position "+position);
                //AlertDialog.Builder builder=new AlertDialog.Builder(HistoryActivity.this,0);
                SQLiteDatabase db=mOpenHelper.getWritableDatabase();
                db.delete(SQLiteHelper.TB_NAME,"historyID=?",new String[]{String.valueOf(historyID.get(position))});
                getHistory();
                adapter.notifyDataSetChanged();
                return true;
            }
        });
        emptyHistory=(Button)findViewById(R.id.empty_history);
        emptyHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db=mOpenHelper.getWritableDatabase();
                db.delete(SQLiteHelper.TB_NAME,null,null);
                getHistory();
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void getHistory(){
        int i=0;
        mHistoryData.clear();
        historyID.clear();
        SQLiteDatabase db=mOpenHelper.getWritableDatabase();
        Cursor mCursor=db.rawQuery("select * from "+SQLiteHelper.TB_NAME,null);
        if(mCursor.moveToFirst()){
            do{
                Map<String,Object> item=new HashMap<>();
                item.put("网页",mCursor.getString(mCursor.getColumnIndex("historyNAME")));
                item.put("网址",mCursor.getString(mCursor.getColumnIndex("historyURL")));
                if(mHistoryData.size()==0){
                    mHistoryData.add(item);
                    historyID.add(mCursor.getInt(mCursor.getColumnIndex("historyID")));
                }else {
                    mHistoryData.add(0,item);
                    historyID.add(0,mCursor.getInt(mCursor.getColumnIndex("historyID")));
                }
            }while (mCursor.moveToNext());
        }
        mCursor.close();
    }
}
