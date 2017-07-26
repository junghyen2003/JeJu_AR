package com.example.jungh.jeju_ar.VROverlap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.example.jungh.jeju_ar.R;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by jungh on 2017-05-26.
 */

public class VROverlapView extends View implements SensorEventListener {

    // 오버랩을 실행하는 레이아웃
    LinearLayout vr_overlapLayout;

    // Context
    Context mContext;

    // Target 이름, 위치, 애니메이션 경로
    String tThemename;
    double tLat;
    double tLon;
    String tVr_path;

    // 센서 관련
    SensorManager mSensorManager;
    Sensor mOrientationSensor;

    float mHeadingAngle_x;
    float mPitchAngle_y;
    float mRollAngle_z;

    int mWidth;
    int mHeight;

    // 보여지는 범위 값
    int mVisibleDistance;

    // 애니메이션 비트맵
    Bitmap mAnimationBitmap;

    // 위치정보 관련
    LocationManager mLocationManager;
    double latitude; // 위도
    double longitude; // 경도
    double altitude; // 고도
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    LocationListener mLocationListener = new LocationListener(){
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }
        @Override
        public void onProviderEnabled(String s) {
        }
        @Override
        public void onProviderDisabled(String s) {
        }
    };

    ImageView iv;
    Lock lock = new ReentrantLock();

    public VROverlapView(Context context, String tThemename, double tLat, double tLon, String tVr_path, ImageView iv){
        super(context);
        mContext = context;
        this.tThemename = tThemename;
        this.tLat = tLat;
        this.tLon = tLon;
        this.tVr_path = tVr_path;
        this.iv = iv;
        initLayout();
        initBitmaps();
        initSensor(mContext);
    }

    // 방향 센서, GPS 초기화 (초기에 호출)
    private void initSensor(Context context){
        // 방향센서
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this,mOrientationSensor,SensorManager.SENSOR_DELAY_UI);

        // GPS
        mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        // GPS 정보 가져오기
        isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 현재 네트워크 상태 값 알아오기
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (!isGPSEnabled && !isNetworkEnabled) {
            // GPS 와 네트워크사용이 가능하지 않을때 소스 구현
        } else {
            if(isGPSEnabled){
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mLocationListener);
            }
            if(isNetworkEnabled){
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, mLocationListener);
            }
        }
    }

    // activity_vr_overlap의 vr_overlapLayout
    public void initLayout(){
        vr_overlapLayout = (LinearLayout)findViewById(R.id.vr_overlapLayout);
    }

    // gif 애니메이션 이미지뷰 초기화
    private synchronized void initBitmaps(){
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(iv);
        Glide.with(mContext).load(tVr_path).into(imageViewTarget);
    }

    // 센서가 변경되었을때 이벤트
    // 센서가 변경되었을경우 onDraw 함수를 호출 -> invalidate()
   @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
       if(sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION){
           mHeadingAngle_x = sensorEvent.values[0];
           mPitchAngle_y = sensorEvent.values[1];
           mRollAngle_z = sensorEvent.values[2];

           // 센서가 변경되었을경우 onDraw 함수를 호출 -> invalidate()
           this.invalidate();
       }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    // onSensorChanged의 invalidate가 호출
    public void onDraw(Canvas canvas){
        canvas.save();
        try {
            animation_draw(canvas);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void animation_draw(Canvas mCanvas) throws InterruptedException {
        double Ax, Ay, Bx, By;
        // Ax = 경도126~, Ay = 위도36~
        if(mLocationManager != null && longitude != 0 && latitude != 0) {
            Ax = longitude; // 경도
            Ay = latitude; // 위도
        }
        else {
            // 순천향대학교 앙뜨레프레너관 위치
            Ax = 126.934068;
            Ay = 36.768926;
        }

        Bx = tLon;
        By = tLat;
        objectDraw(Ax, Ay, Bx, By, mCanvas);
    }

    // overlap 애니메이션 오브젝트 그리기
    public void objectDraw(double Ax, double Ay, double Bx, double By, Canvas mCanvas) throws InterruptedException {
        // 현재 위치와 타겟의 위치를 계산
        double mXDegree = (double) (Math.atan((double) (By - Ay) / (double) (Bx - Ax)) * 180.0 / Math.PI);

        float mYDegree = 0;

        // 기기의 기울임 각도
        mYDegree = mPitchAngle_y;


        // 4/4분면을 고려하여 0~360도가 나오게 설정
        if(Bx > Ax && By > Ay){
            ;
        }else if (Bx < Ax && By > Ay){
            mXDegree += 180;
        } else if (Bx < Ax && By < Ay){
            mXDegree += 180;
        } else if (Bx > Ax && By < Ay){
            mXDegree += 360;
        }

        // 두 위치간의 각도에 현재 스마트폰이 동쪽기준 바라보고 있는 방향 만큼 더해줌
        // 360도(한바퀴)가 넘었으면 한바퀴 회전한 것이기에 360를 빼줌
        if (mXDegree + mHeadingAngle_x < 360) {
            mXDegree += mHeadingAngle_x;
        } else if (mXDegree + mHeadingAngle_x >= 360) {
            mXDegree = mXDegree + mHeadingAngle_x - 360;
        }


        // 계산된 각도 만큼 기기 정중앙 화면 기준 어디에 나타날지 계산함
        // 정중앙은 90도, 시야각은 30도로 75 ~ 105 사이일때만 화면에 나타남
        float mX = 0;
        float mY = 0;

        if (mXDegree > 75 && mXDegree < 105) {
            if (mYDegree > -180 && mYDegree < 0) {
                mX = (float) mWidth - (float) ((mXDegree - 75) * ((float) mWidth / 30));
                mYDegree = -(mYDegree);
                mY = (float)(mYDegree * ((float) mHeight / 180));
            }
        }

        // 두 위치간의 거리를 계산함
        Location locationA = new Location("Point A");
        Location locationB = new Location("Point B");

        locationA.setLongitude(Ax);
        locationA.setLatitude(Ay);

        locationB.setLongitude(Bx);
        locationB.setLatitude(By);

        int distance = (int) locationA.distanceTo(locationB);

        // 거리 안에 있을 때 애니메이션 그리기
        if (distance <= mVisibleDistance && mX != 0){
            iv.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = iv.getLayoutParams();
            if(distance < 10){
                params.width = 2000;
                params.height = 2000;
            }else if(distance > 290){
                params.width = 1000;
                params.height = 1000;
            } else {
                params.width = 2000 - ((1000 / 280) * (distance - 10));
                params.height = 2000 - ((1000 / 280) * (distance - 10));
            }
            iv.setLayoutParams(params);

            iv.setTranslationX(mX - (iv.getWidth() / 2));
            iv.setTranslationY(mY - (iv.getHeight() / 2));
        }else{
            iv.setVisibility(View.GONE);
        }
    }

    // MainActivity에서 surfaceView의 크기를 가져와서 저장
    public void setOverlaySize(int width, int height){
        mWidth = width;
        mHeight = height;
    }

    // 보여지는 범위 설정
    public void setmVisibleDistance(int mVisibleDistance){
        this.mVisibleDistance = mVisibleDistance;
    }

}
