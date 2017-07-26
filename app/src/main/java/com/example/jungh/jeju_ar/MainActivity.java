package com.example.jungh.jeju_ar;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // 프리뷰화면
    private MainSurfaceView mSurfaceView;
    // 오버레이 화면
    private SurfaceOverlayView mSurfaceOverlayView;
    LinearLayout mcoverlayout;
    // 오버레이 화면 크기를 넘겨주기 위함
    public int mwidth;
    public int mheight;

    // 위치정보 관련
    LocationManager locationManager;
    double latitude; // 위도
    double longitude; // 경도
    double altitude; // 고도

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 가로세로 고정
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SurfaceView surface = (SurfaceView)findViewById(R.id.surfaceView);
        mSurfaceView = new MainSurfaceView(this, surface);
        ((LinearLayout)findViewById(R.id.surface_main)).addView(mSurfaceView);

        mSurfaceOverlayView = new SurfaceOverlayView(this);
        SeekBar mSeekBar;
        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        mSeekBar.setProgress(50);
        mSurfaceOverlayView.setmVisibleDistance(50);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(seekBar.getProgress() < 100) {
                    mSurfaceOverlayView.setmVisibleDistance(seekBar.getProgress());
                    mSurfaceOverlayView.invalidate();
                } else {
                    mSurfaceOverlayView.setmVisibleDistance(5000); // seekbar 값이 100 이상이면 거리 무제한(5000km)
                    mSurfaceOverlayView.invalidate();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        ((FrameLayout)findViewById(R.id.content_main)).addView(mSurfaceOverlayView);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Drawer Menu가 SurfaceView 위에 뜨도록 해결
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                drawer.bringChildToFront(drawerView);
                drawer.requestLayout();
            }
            @Override
            public void onDrawerOpened(View drawerView) {
            }
            @Override
            public void onDrawerClosed(View drawerView) {
            }
            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        //
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // -----------------------------------------------------------------------------------------
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        mcoverlayout = (LinearLayout)findViewById(R.id.coverLayout);
        mwidth = mcoverlayout.getWidth();
        mheight = mcoverlayout.getHeight();
        // coverLayout width, height 크기를 넘겨줌
        mSurfaceOverlayView.setOverlaySize(mwidth, mheight);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_restaurant) {
            mSurfaceOverlayView.setTheme("restaurant");
        } else if (id == R.id.nav_tourist) {
            mSurfaceOverlayView.setTheme("tourist");
        } else if (id == R.id.nav_shopping) {
            mSurfaceOverlayView.setTheme("shopping");
        } else if (id == R.id.nav_restroom){
            mSurfaceOverlayView.setTheme("restroom");
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        //세로
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            mSurfaceOverlayView.setOverlaySize(mwidth, mheight);
        }
        //가로
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            mSurfaceOverlayView.setOverlaySize(mheight, mwidth);
        }
    }
}
