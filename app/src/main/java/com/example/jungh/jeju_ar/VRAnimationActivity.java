package com.example.jungh.jeju_ar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.jungh.jeju_ar.VROverlap.VROverlapView;

public class VRAnimationActivity extends AppCompatActivity {
    // 프리뷰화면
    private MainSurfaceView mSurfaceView;

    // 오버레이 화면
    private VROverlapView mVROverlapView;
    LinearLayout mcoverlayout;

    // 오버레이 화면 크기를 넘겨주기 위함
    public int mwidth;
    public int mheight;

    // Target 정보 저장
    String themename;
    double lat;
    double lon;
    String vr_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_animation);

        // 가로세로 고정
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 인텐트 값 저장
        Intent intent = getIntent();
        themename = intent.getStringExtra("themename");
        lat = intent.getDoubleExtra("lat", 0);
        lon = intent.getDoubleExtra("lon", 0);
        vr_path = intent.getStringExtra("vr_path");

        // 프리뷰 생성 및 할당
        SurfaceView surface = (SurfaceView)findViewById(R.id.surfaceView);
        mSurfaceView = new MainSurfaceView(this, surface);
        ((LinearLayout)findViewById(R.id.surface_main)).addView(mSurfaceView);

        // 오버레이 뷰 생성 및 할당
        ImageView iv = (ImageView)findViewById(R.id.vr_imageVIew_overlap);
        mVROverlapView = new VROverlapView(this, themename, lat, lon, vr_path, iv);

        // 300미터 안에 있을 때 표시(m)
        mVROverlapView.setmVisibleDistance(300);

        ((FrameLayout)findViewById(R.id.vr_overlapMainLayout)).addView(mVROverlapView);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        mcoverlayout = (LinearLayout)findViewById(R.id.vr_overlapLayout);
        mwidth = mcoverlayout.getWidth();
        mheight = mcoverlayout.getHeight();
        // coverLayout width, height 크기를 넘겨줌
        mVROverlapView.setOverlaySize(mwidth, mheight);
    }
}
