package com.example.myapplication.Interface;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.myapplication.Assistance.HistoryItemBean;
import com.example.myapplication.Assistance.HistoryListAdapter;
import com.example.myapplication.Assistance.IDockingHeaderUpdateListener;
import com.example.myapplication.Components.HistoryListView;
import com.example.myapplication.R;
import com.example.myapplication.Toolkit.SQLiteHelper;
import com.example.myapplication.Components.SwipeBackActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Zbm阿铭 on 2017/5/18.
 */

public class HistoryActivity extends SwipeBackActivity {
    private HistoryListView mList;
    private Button historyBack,emptyHistory;
    private SQLiteOpenHelper mOpenHelper;
    private Map<String,List<HistoryItemBean>> mHistoryData;
    private List[] children;  //用于暂时记录分组成员
    private List<String> parentList;  //header view的日期标题
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOpenHelper=new SQLiteHelper(this,"historyDB",null,1);
        mHistoryData=new HashMap<>();
        parentList=new ArrayList<>();
        setContentView(R.layout.history);
        getHistory();
        mList= (HistoryListView) findViewById(R.id.history_list);
        historyBack=(Button)findViewById(R.id.history_back);
        historyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        final HistoryListAdapter adapter=new HistoryListAdapter(this,mList,parentList,mHistoryData);
        mList.setAdapter(adapter);


        mList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                HistoryItemBean hb=mHistoryData.get(parentList.get(groupPosition)).get(childPosition);
                Intent intent=new Intent();
                intent.putExtra("currentUri",hb.getHistoryURI());
                SQLiteDatabase db=mOpenHelper.getWritableDatabase();
                db.delete(SQLiteHelper.TB_NAME,"historyID=?",new String[]{hb.getHistoryID()+""});
                setResult(RESULT_OK,intent);
                finish();
                return true;
            }
        });
        View headerView = getLayoutInflater().inflate(R.layout.history_of_date, mList, false);

        //更新标题
        mList.setDockingHeader(headerView, new IDockingHeaderUpdateListener() {
            @Override
            public void onUpdate(View headerView, int groupPosition, boolean expanded) {
                String groupTitle = parentList.get(groupPosition);
                TextView titleView = (TextView) headerView.findViewById(R.id.history_date);
                titleView.setText(groupTitle);
            }
        });

        //长按删除item
        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int groupPos = (Integer)view.getTag(R.id.web_title); //参数值是在setTag时使用的对应资源id号
                int childPos = (Integer)view.getTag(R.id.web_url);
                if(childPos!=-1){
                    SQLiteDatabase db=mOpenHelper.getWritableDatabase();
                    db.delete(SQLiteHelper.TB_NAME,"historyID=?",new String[]{mHistoryData.get(parentList.get(groupPos)).get(childPos).getHistoryID()+""});
                    getHistory();
                    adapter.notifyDataSetChanged();
                }

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

        //默认展开所有组
        for(int i = 0; i < adapter.getGroupCount(); i++)
            mList.expandGroup(i);
    }
    private void getHistory(){
        String lastDate="";  //保持上个组的日期
        int key=-1;  //记录子成员索引
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
        String currentDate=format.format(new Date(System.currentTimeMillis()));
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DATE,-1);
        String yesterday=format.format(calendar.getTime());
        mHistoryData.clear();
        parentList.clear();
        children=null;
        SQLiteDatabase db=mOpenHelper.getWritableDatabase();

        //查询3天内的历史记录
        Cursor mCursor=db.rawQuery("select * from "+SQLiteHelper.TB_NAME+" where historyTIME>=datetime('now','start of day','-3 day') and historyTIME<datetime('now','start of day','+0 day') order by historyTIME desc",null);
        //先计算有多少个组
        if(mCursor.moveToFirst()){
            do{
                String date=mCursor.getString(mCursor.getColumnIndex("historyTIME"));
                //需要添加头节点
                if(!lastDate.equals(date)){
                    if(currentDate.equals(date)){

                        parentList.add("今天");

                    }else if(yesterday.equals(date)){

                        parentList.add("昨天");
                    }else{

                        parentList.add(date);
                    }
                }
                lastDate=date;
             }while (mCursor.moveToNext());
            children=new List[parentList.size()];
            //接着再计算成员
            mCursor.moveToFirst();
            for (int i=0;i<parentList.size();i++){
                children[i]=new ArrayList<HistoryItemBean>();
            }
            lastDate="";
            do{
                String date=mCursor.getString(mCursor.getColumnIndex("historyTIME"));
                if(!lastDate.equals(date)){
                    key+=1;
                }
                String id=mCursor.getString(mCursor.getColumnIndex("historyID"));
                String name=mCursor.getString(mCursor.getColumnIndex("historyNAME"));
                String url=mCursor.getString(mCursor.getColumnIndex("historyURL"));
                children[key].add(0,new HistoryItemBean(Integer.parseInt(id),name,url));
                lastDate=date;
            }while (mCursor.moveToNext());

            //该步把所有组加入list
            for (int i=0;i<children.length;i++){
                mHistoryData.put(parentList.get(i),children[i]);
            }
        }
        mCursor.close();
    }
}
