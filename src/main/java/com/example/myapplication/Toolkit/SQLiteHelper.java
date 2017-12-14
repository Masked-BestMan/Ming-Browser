package com.example.myapplication.Toolkit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by Zbm阿铭 on 2017/5/17.
 */

public class SQLiteHelper extends SQLiteOpenHelper {
    public static final String DB_NAME="historyDB";
    public static final String TB_NAME="historyTB";
    public static final String CREATE_HISTORYTB="create table historyTB(" +
            "historyID integer primary key autoincrement," +
            "historyURL text," +
            "historyTIME date," +
            "historyNAME text)";
    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HISTORYTB);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    public void updateColumn(SQLiteDatabase db,String oldColumn,String newColumn,String typeColumn){
        try{
            db.execSQL("ALTER TABLE" +
                    TB_NAME+" CHANGE "+oldColumn+" "+newColumn+" "+typeColumn);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
