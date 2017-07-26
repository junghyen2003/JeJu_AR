package com.example.jungh.jeju_ar;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.jungh.jeju_ar.common.DataHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PreviewActivity extends AppCompatActivity {
    // 뷰 객체
    ImageView preview_imageView;
    Button preview_save;
    Button preview_previous;
    ScrollView preview_scroll;

    // 캔버스, 원본사진 비트맵, 덮을 사진 비트맵
    Bitmap origin_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        preview_imageView = (ImageView)findViewById(R.id.preview_ImageView);
        preview_save = (Button)findViewById(R.id.preview_save);
        preview_previous = (Button)findViewById(R.id.preview_previous);
        preview_scroll = (ScrollView)findViewById(R.id.preview_scroll);

        setTitle("미리보기"); // 타이틀 바 이름 변경
        preview_scroll.setVerticalScrollBarEnabled(true); // 스크롤 허용

        Intent i = getIntent();

        final String origin_bitmap_id = i.getStringExtra("origin_bitmap");

        origin_bitmap = (Bitmap)DataHolder.popDataHolder(origin_bitmap_id);

        preview_imageView.setImageBitmap(origin_bitmap);

        preview_save.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmaptoJpeg(origin_bitmap,"namooplus",origin_bitmap_id);
                Toast.makeText(getApplicationContext(),origin_bitmap_id+".jpeg로 저장되었습니다.",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        preview_previous.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) // 세로 전환시
        {
            // 배경 화면 교체 처리
            preview_imageView.setImageBitmap(origin_bitmap);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)// 가로 전환시
        {
            // 배경 화면 교체 처리
            preview_imageView.setImageBitmap(origin_bitmap);
        }
    }

    public void saveBitmaptoJpeg(Bitmap bitmap, String folder, String name){
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String foler_name = "/"+folder+"/";
        String file_name = name+".jpg";
        String string_path = ex_storage+foler_name;

        File file_path;
        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path+file_name);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + string_path +"/" + file_name)));
    }
}
