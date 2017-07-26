package com.example.jungh.jeju_ar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.example.jungh.jeju_ar.common.Utils;
import com.example.jungh.jeju_ar.listmodel.ListImageAdapter;
import com.example.jungh.jeju_ar.listmodel.SpacesItemDecoration;

import static com.example.jungh.jeju_ar.common.DataHolder.putDataHolder;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VROverlapActivity extends AppCompatActivity {

    // 프리뷰화면
    private MainSurfaceView mSurfaceView;
    LinearLayout surfaceView;

    // 비트맵
    public static Bitmap shareBitmap;

    // 카메라 아이템 레이아웃
    LinearLayout camera_item_layout;
    // 카메라 버튼
    Button take_picture_Button;
    Button auto_focus_Button;

    String themename;
    String vr_path;

    // 이미지 경로들
    String[] image_path;

    // 리스트
    RecyclerView listView;
    ListImageAdapter mListImageAdapter;

    // 현재 보여지는 컨텐트뷰 경로
    String now_path_image;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vroverlap);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.selected_imageView);
        mContentView.setDrawingCacheEnabled(true);
        camera_item_layout = (LinearLayout)findViewById(R.id.camera_item_layout);
        take_picture_Button = (Button)findViewById(R.id.take_picture_Button);
        auto_focus_Button = (Button)findViewById(R.id.auto_focus_Button);

        Intent intent = getIntent();
        themename = intent.getStringExtra("themename");
        vr_path = intent.getStringExtra("vr_path");

        // 프리뷰 생성 및 할당
        final SurfaceView surface = (SurfaceView)findViewById(R.id.surfaceView);
        mSurfaceView = new MainSurfaceView(this, surface);

        // 리스트뷰 셋팅
        listView = (RecyclerView)findViewById(R.id.image_list);
        image_path = vr_path.split(",");

        LinearLayoutManager mLayoutManager_Linear = new LinearLayoutManager(this);
        mLayoutManager_Linear.setOrientation(LinearLayoutManager.HORIZONTAL);

        listView.setLayoutManager(mLayoutManager_Linear);
        listView.addItemDecoration(new SpacesItemDecoration(
                Utils.dpToPx(VROverlapActivity.this, 5), SpacesItemDecoration.TYPE_HORIZONTAL));
        listView.setHasFixedSize(true);

        mListImageAdapter = new ListImageAdapter(VROverlapActivity.this, image_path);
        listView.setAdapter(mListImageAdapter);
        now_path_image = image_path[0];

        mListImageAdapter.setItemClick(new ListImageAdapter.ItemClick() {
            @Override
            public void onClick(View view, int position) {
                Glide.with(VROverlapActivity.this)
                        .load(image_path[position])
                        .into((ImageView) mContentView);
                now_path_image = image_path[position];
            }
        });

        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {// 세로 모드
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM|Gravity.CENTER);
            camera_item_layout.setOrientation(LinearLayout.HORIZONTAL);
            camera_item_layout.setLayoutParams(params);
            Glide.with(VROverlapActivity.this)
                    .load(now_path_image)
                    .into((ImageView) mContentView);

        }else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {// 가로 모드
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.RIGHT|Gravity.CENTER);
            camera_item_layout.setOrientation(LinearLayout.VERTICAL);
            camera_item_layout.setLayoutParams(params);
            Glide.with(VROverlapActivity.this)
                    .load(now_path_image)
                    .into((ImageView) mContentView);
        }

        // 캡처 동작 시작
        mSurfaceView.setflag(1);
        ((LinearLayout)findViewById(R.id.surface_main)).addView(mSurfaceView);
        surfaceView = (LinearLayout) findViewById(R.id.surface_main);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });



        take_picture_Button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(VROverlapActivity.this, PreviewActivity.class);

                mContentView.buildDrawingCache();

                Bitmap bm =  Bitmap.createBitmap(shareBitmap.getWidth(), shareBitmap.getHeight(), shareBitmap.getConfig());
                Canvas canvas = new Canvas(bm);
                canvas.drawBitmap(shareBitmap, 0, 0, null);
                Bitmap bp = mContentView.getDrawingCache();
                // 오버래핑 이미지 스케일링
                bp = Bitmap.createScaledBitmap(bp, shareBitmap.getWidth(), shareBitmap.getHeight(), true);
                canvas.drawBitmap(bp, 0, 0, null);

                String origin_bitmap = putDataHolder(bm);

                i.putExtra("origin_bitmap", origin_bitmap);

                mSurfaceView.setflag(0);
                startActivity(i);
            }
        });

        auto_focus_Button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSurfaceView.autoFocus();
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();
        mSurfaceView.setflag(1);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);

        take_picture_Button.setVisibility(View.VISIBLE);
        auto_focus_Button.setVisibility(View.VISIBLE);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);

        take_picture_Button.setVisibility(View.GONE);
        auto_focus_Button.setVisibility(View.GONE);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) // 세로 전환시
        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM|Gravity.CENTER);
            camera_item_layout.setOrientation(LinearLayout.HORIZONTAL);
            camera_item_layout.setLayoutParams(params);
            mSurfaceView.setflag(0);
            Glide.with(VROverlapActivity.this)
                    .load(now_path_image)
                    .into((ImageView) mContentView);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)// 가로 전환시
        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.RIGHT|Gravity.CENTER);
            camera_item_layout.setOrientation(LinearLayout.VERTICAL);
            camera_item_layout.setLayoutParams(params);
            mSurfaceView.setflag(0);
            Glide.with(VROverlapActivity.this)
                    .load(now_path_image)
                    .into((ImageView) mContentView);
        }
        mSurfaceView.setflag(1);
    }
}
