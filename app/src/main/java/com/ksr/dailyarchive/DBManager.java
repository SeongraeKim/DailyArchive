package com.ksr.dailyarchive;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBManager extends SQLiteOpenHelper {
    SQLiteDatabase db;
    public DBManager(Context context) {
        super(context, "DailyArchiveDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE diaryDB (date text, content text);");                              // SQLite 테이블 생성
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    public ArrayList selectAllDB(){                                                                 // 모든 데이터 조회
        ArrayList<ListData> arrayList = new ArrayList<>();
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT date, content FROM DiaryDB;", null);

        while (cursor.moveToNext()){
            int date = cursor.getInt(0);
            String content = cursor.getString(1);

            arrayList.add(new ListData(date, content));
        }
        cursor.close();
        db.close();

        return arrayList;
    }

    public String selectDB(int date){                                                               // 날짜에 해당하는 일기 데이터 조회
        String content = null;

        db = getReadableDatabase();
        Cursor cursor = db.rawQuery(" SELECT content FROM diaryDB " +
                " WHERE date = '"+ date +"'; ", null);
        if(cursor != null && cursor.moveToFirst()) {
            content = cursor.getString(0);
        }
        cursor.close();
        db.close();

        return content;
    }

    public void insertDB(int date, String content){                                                 // 데이터 추가
        db = getWritableDatabase();
        db.execSQL(" INSERT INTO diaryDB " +
                " VALUES('" + date + "', '" + content + "'); ");
        db.close();
    }

    public void updateDB(int date, String content){                                                 // 데이터 수정
        db = getWritableDatabase();
        db.execSQL(" UPDATE DiaryDB " +
                " SET date = '" + date + "' , content = '" + content + "' " +
                " WHERE date = '" + date + "'; ");
        db.close();
    }

    public void deleteDB(int date){                                                                 // 데이터 삭제
        db = getWritableDatabase();
        db.execSQL(" DELETE FROM diaryDB " +
                " WHERE date = '" + date + "'; ");
        db.close();
    }
}
