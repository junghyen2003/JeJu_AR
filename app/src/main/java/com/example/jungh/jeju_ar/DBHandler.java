package com.example.jungh.jeju_ar;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jungh on 2017-01-05.
 */

public class DBHandler {

    private List<DBRecord> mDBRecordList;
    private DBRecord mSelectedDBRecord=  new DBRecord();
    private String mTheme;
    private String mThemeName;
    private String mNewestDBInfo;
    private Object mContext;

    public static final String RESTAURANT = "RESTAURANT";
    public static final String TOURIST = "TOURIST";
    public static final String SHOPPING = "SHOPPING";
    public static final String RESTROOM = "RESTROOM";
    public static final String ALL= "ALL";

    public DBHandler(Context context){
        mContext = context;
    }

    // DB 내용 복사
    public void copyDB() {
        AssetManager assetManager = ((Context) mContext).getResources().getAssets();

        OutputStream fos = null;
        InputStream fis = null;

        try {
            // DBpath.txt 파일 복사
            fis = assetManager.open("DBpath.txt");

            File file = new File("/data/data/com.example.jungh.jeju_ar/files/");
            file.mkdir();
            fos = new FileOutputStream("/data/data/com.example.jungh.jeju_ar/files/DBpath.txt");

            byte[] buffer = new byte[1024];
            int readCount = 0;
            while (true) {
                readCount = fis.read(buffer, 0, 1024);
                if (readCount == -1) {
                    break;
                }
                if (readCount < 1024) {
                    fos.write(buffer, 0, readCount);
                    break;
                }
                fos.write(buffer, 0, readCount);
            }
            fos.flush();
            fos.close();
            fis.close();

            FileReader fr;

            // 복사된 notice.txt 파일의 내용 한줄을 읽음
            // 그 내용은 현재 최신의 .db 파일 이름에 대한 정보
            fr = new FileReader("/data/data/com.example.jungh.jeju_ar/files/DBpath.txt");
            BufferedReader br = new BufferedReader(fr);
            mNewestDBInfo = br.readLine();
            Log.i("readline", mNewestDBInfo);

            // 복사 한 DBpath.txt 파일의 내용 한줄 읽기
            // 내용은 현재 최신 DB 이름
            fis = assetManager.open(mNewestDBInfo);

            file = new File("/data/data/com.example.jungh.jeju_ar/databases/");
            file.mkdir();
            fos = new FileOutputStream("/data/data/com.example.jungh.jeju_ar/databases/" + mNewestDBInfo);

            buffer = new byte[1024];
            readCount = 0;
            while (true) {
                readCount = fis.read(buffer, 0, 1024);

                if (readCount == -1) {
                    break;
                }

                if (readCount < 1024) {
                    fos.write(buffer, 0, readCount);
                    break;
                }

                fos.write(buffer, 0, readCount);
            }
            fos.flush();

            fos.close();
            fis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DB 읽기
    public void readDB() {
        mDBRecordList = new ArrayList<DBRecord>();

        FileReader fr;

        // 최신 db 파일이름이 저장된 DBpath.txt 파일 열고 경로 읽음
        try {
            fr = new FileReader("/data/data/com.example.jungh.jeju_ar/files/DBpath.txt");
            BufferedReader br = new BufferedReader(fr);
            mNewestDBInfo = br.readLine();
        }catch (Exception e) {
            e.printStackTrace();
        }

        // 읽어들인 경로로 .db 파일 열기
        SQLiteDatabase sqlDB = SQLiteDatabase.openDatabase("/data/data/com.example.jungh.jeju_ar/databases/"+mNewestDBInfo, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        String sqlStr = null;

        // 테마에 따라 다른 쿼리문
        if(mTheme.equals(RESTAURANT)){
            sqlStr = "select * from TestTable where theme = '식당'";
        } else if (mTheme.equals(TOURIST)){
            sqlStr = "select * from TestTable where theme = '관광지'";
        } else if(mTheme.equals(SHOPPING)){
            sqlStr = "select * from TestTable where theme = '쇼핑'";
        } else if(mTheme.equals(RESTROOM)){
            sqlStr = "select * from TestTable where theme = '화장실'";
        } else if(mTheme.equals(ALL)){
            sqlStr = "select * from TestTable";
        }

        // 커서를 레코드 하나씩 이동, 레코드 리스트에 추가
        Cursor cursor = sqlDB.rawQuery(sqlStr, null);

        cursor.moveToFirst();

        while(cursor.isAfterLast() == false){
            DBRecord dbRecord = new DBRecord();
            dbRecord.setTheme(cursor.getString(cursor.getColumnIndex("theme")));
            dbRecord.setLatitude(cursor.getDouble(cursor.getColumnIndex("lat")));
            dbRecord.setLongitude(cursor.getDouble(cursor.getColumnIndex("lon")));
            dbRecord.setName(cursor.getString(cursor.getColumnIndex("name")));
            dbRecord.setTb_locations_id(cursor.getInt(cursor.getColumnIndex("tb_locations_id")));
            dbRecord.setVr_theme(cursor.getString(cursor.getColumnIndex("vr_theme")));
            dbRecord.setVr_path(cursor.getString(cursor.getColumnIndex("vr_path")));
            mDBRecordList.add(dbRecord);
            cursor.moveToNext();
        }

        cursor.close();
        sqlDB.close();
    }

    // 레코드 리스트 반환
    public List<DBRecord> getmDBRecordList() {
        return mDBRecordList;
    }

    // 테마 선택(식당, 관광지, 쇼핑, 화장실, 전부)
    public void setmTheme(String theme){
        mTheme = theme;
    }

    // 선택된 테마
    public String getmTheme(){
        return mTheme;
    }

    // ----------------------------------------------------------------------
    // 이름을 통한 레코드 검색
    public void searchRecord() {
        FileReader fr;
        // 최신 db 파일이름이 저장된 DBpath.txt 파일 열고 경로 읽음
        try {
            fr = new FileReader("/data/data/com.example.jungh.jeju_ar/files/DBpath.txt");
            BufferedReader br = new BufferedReader(fr);
            mNewestDBInfo = br.readLine();
        }catch (Exception e) {
            e.printStackTrace();
        }
        // 읽어들인 경로로 .db 파일 열기
        SQLiteDatabase sqlDB = SQLiteDatabase.openDatabase("/data/data/com.example.jungh.jeju_ar/databases/"+mNewestDBInfo, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        String sqlStr = null;
        sqlStr = "select * from TestTable where name = '"+mThemeName+"'";
        Cursor cursor = sqlDB.rawQuery(sqlStr, null);
        cursor.moveToFirst();
        mSelectedDBRecord.setTheme(cursor.getString(cursor.getColumnIndex("theme")));
        mSelectedDBRecord.setLatitude(cursor.getDouble(cursor.getColumnIndex("lat")));
        mSelectedDBRecord.setLongitude(cursor.getDouble(cursor.getColumnIndex("lon")));
        mSelectedDBRecord.setName(cursor.getString(cursor.getColumnIndex("name")));
        mSelectedDBRecord.setTb_locations_id(cursor.getInt(cursor.getColumnIndex("tb_locations_id")));
        mSelectedDBRecord.setVr_theme(cursor.getString(cursor.getColumnIndex("vr_theme")));
        mSelectedDBRecord.setVr_path(cursor.getString(cursor.getColumnIndex("vr_path")));
        cursor.close();
        sqlDB.close();
    }

    // 선택된 레코드 반환
    public DBRecord getmSelectedDBRecord() {
        return mSelectedDBRecord;
    }

    // 검색하려는 테마이름 선택
    public void setmThemeName(String themeName){
        mThemeName = themeName;
    }

    // 선택된 테마이름
    public String getmThemeName() {
        return mThemeName;
    }
}
