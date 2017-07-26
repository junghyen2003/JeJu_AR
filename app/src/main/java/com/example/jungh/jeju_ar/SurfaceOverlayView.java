package com.example.jungh.jeju_ar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by jungh on 2016-12-23.
 */

public class SurfaceOverlayView extends View implements SensorEventListener {

    LinearLayout mOverlayLayout;

    SensorManager mSensorManager;
    Sensor mOrientationSensor;

    DBHandler mDBHandler;
    List<DBRecord> mDBRecordList;

    Context mContext;

    float mHeadingAngle_x;
    float mPitchAngle_y;
    float mRollAngle_z;

    int mWidth;
    int mHeight;

    int mShadowXMargin;
    int mShadowYMargin;

    int mVisibleDistance; // 보여지는 범위 값

    Bitmap mRestaurantBitmap;
    Bitmap mTouristBitmap;
    Bitmap mShoppingBitmap;
    Bitmap mRestroomBitmap;

    Paint mPaint;
    Paint mShadowPaint;

    boolean mTouched = false;

    //int mTouchedItem;
    float mTouchedY;
    float mTouchedX;

    int mCounter = 0;
    boolean mScreenTouched = false;

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
            Toast.makeText(mContext, "GPS 사용 가능하게 설정되었습니다.", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(mContext, "GPS 사용 여부를 확인해주세요.", Toast.LENGTH_SHORT).show();
        }
    };

    private List<PointF> mPointFList = null;
    private HashMap<Integer, String> mPointHashMap;

    public SurfaceOverlayView(Context context) {
        super(context);
        mContext = context;
        initSensor(context);
        initLayout();
        initBitmaps();
        initPaints();
        initDBHandler();
        // GPS 관련
        mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, mLocationListener);
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

    // content_main의 coverLayout
    public void initLayout(){
        mOverlayLayout = (LinearLayout)findViewById(R.id.coverLayout);
    }

    // 비트맵 아이콘 초기화 (초기에 호출)
    private void initBitmaps(){
        mRestaurantBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_restaurant);
        mRestaurantBitmap = Bitmap.createScaledBitmap(mRestaurantBitmap, 200, 200, true);

        mTouristBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_tourist);
        mTouristBitmap = Bitmap.createScaledBitmap(mTouristBitmap, 200, 200, true);

        mShoppingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_shopping);
        mShoppingBitmap = Bitmap.createScaledBitmap(mShoppingBitmap, 200, 200, true);

        mRestroomBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_restroom);
        mRestroomBitmap = Bitmap.createScaledBitmap(mRestroomBitmap, 200, 200, true);
    }

    // paint 초기화 (초기에 호출)
    // 각종 paint 설정
    private void initPaints(){
        mShadowXMargin = 2;
        mShadowYMargin = 2;

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setTextSize(25);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.rgb(238, 229, 222));
        mPaint.setTextSize(25);

    }

    // DB 초기화 (초기에 호출)
    private void initDBHandler(){
        mDBHandler = new DBHandler(mContext);
        mDBHandler.setmTheme(DBHandler.ALL);
        mDBHandler.readDB();
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

    // MainActivity에서 surfaceView의 크기를 가져와서 저장
    public void setOverlaySize(int width, int height){
        mWidth = width;
        mHeight = height;
    }

    // onSensorChanged의 invalidate가 호출
    // 캔버스 위에 이미지, 버튼, 사진 삽입
    public void onDraw(Canvas canvas){
        canvas.save();
        readDBandDraw(canvas);

        // 스크린이 터치되었을때 효과를 그림
        if (mScreenTouched == true && mCounter < 15) {
            drawTouchEffect(canvas);
            mCounter++;
        } else {
            mScreenTouched = false;
            mCounter = 0;
        }
    }

    // overlay 오브젝트 그리기
    public PointF objectDraw(double Ax, double Ay, double Bx, double By, Canvas mCanvas, Paint mPaint, String name, String theme){
        // 현재 위치와 타겟의 위치를 계산
        double mXDegree = (double) (Math.atan((double) (By - Ay) / (double) (Bx - Ax)) * 180.0 / Math.PI);

        // 기기의 기울임 각도
        float mYDegree = mPitchAngle_y;

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

        Bitmap BitmapIcon = null;
        if(theme.equals("식당")){
            BitmapIcon = mRestaurantBitmap;
        } else if(theme.equals("관광지")){
            BitmapIcon = mTouristBitmap;
        } else if(theme.equals("쇼핑")){
            BitmapIcon = mShoppingBitmap;
        } else if(theme.equals("화장실")){
            BitmapIcon = mRestroomBitmap;
        }

        int iconWidth, iconHeight;
        iconWidth = BitmapIcon.getWidth();
        iconHeight = BitmapIcon.getHeight();
        PointF mPoint = new PointF();
        if(mX!=0 && mY!=0){
                if (distance <= mVisibleDistance * 1000) {
                    if (distance < 1000) {
                        mCanvas.drawBitmap(BitmapIcon, mX - (iconWidth / 2), mY
                                - (iconHeight / 2), mPaint);

                        mCanvas.drawText(name, mX - mPaint.measureText(name) / 2
                                + mShadowXMargin, mY + iconHeight / 2 + 30
                                + mShadowYMargin, mShadowPaint);

                        mCanvas.drawText(name, mX - mPaint.measureText(name) / 2, mY
                                + iconHeight / 2 + 30, mPaint);

                        mCanvas.drawText(distance + "m",
                                mX - mPaint.measureText(distance + "m") / 2
                                        + mShadowXMargin, mY + iconHeight / 2 + 60
                                        + mShadowYMargin, mShadowPaint);

                        mCanvas.drawText(distance + "m",
                                mX - mPaint.measureText(distance + "m") / 2, mY
                                        + iconHeight / 2 + 60, mPaint);

                    } else if (distance >= 1000) {
                        float fDistance = (float) distance / 1000;
                        fDistance = (float) Math.round(fDistance * 10) / 10;

                        mCanvas.drawBitmap(BitmapIcon, mX - (iconWidth / 2), mY
                                - (iconHeight / 2), mPaint);

                        mCanvas.drawText(name, mX - mPaint.measureText(name) / 2
                                + mShadowXMargin, mY + iconHeight / 2 + 30
                                + mShadowYMargin, mShadowPaint);

                        mCanvas.drawText(name, mX - mPaint.measureText(name) / 2, mY
                                + iconHeight / 2 + 30, mPaint);

                        mCanvas.drawText(fDistance + "Km",
                                mX - mPaint.measureText(fDistance + "Km") / 2
                                        + mShadowXMargin, mY + iconHeight / 2 + 60
                                        + mShadowYMargin, mShadowPaint);

                        mCanvas.drawText(fDistance + "Km",
                                mX - mPaint.measureText(fDistance + "Km") / 2, mY
                                        + iconHeight / 2 + 60, mPaint);
                    }
                    mPoint.set(mX, mY);
                }
            else{
                  return null;
                }
        }
        return mPoint;
    }

    // DB를 읽어들이고 레코드마다 하나씩 그리는 함수 호출
    public void readDBandDraw(Canvas mCanvas){
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

        mPointFList = new ArrayList<PointF>();
        mPointHashMap = new HashMap<Integer, String>();

        String mName;
        PointF mPoint;
        String mTheme;
        DBRecord mDBRecord;

        mDBRecordList = mDBHandler.getmDBRecordList();
        Iterator<DBRecord> dbRecordIterator = mDBRecordList.iterator();
        for (int i = 0; i < mDBRecordList.size(); i++){
            mDBRecord = dbRecordIterator.next();
            if(mDBRecord!=null){
                mName = mDBRecord.getName();
                Bx = mDBRecord.getLongitude();
                By = mDBRecord.getLatitude();
                mTheme = mDBRecord.getTheme();

                mPoint = objectDraw(Ax, Ay, Bx, By, mCanvas, mPaint, mName, mTheme);
                if(mPoint!=null) {
                    mPointFList.add(mPoint);
                    mPointHashMap.put(i, mName);
                }
            }
        }
    }

    // 터치이벤트 발생
    public boolean onTouchEvent(MotionEvent event){
        // 화면이 회전되었기에 좌표도 변환함
        float convertedX, convertedY, temp;
        convertedX = event.getX();
        convertedY = event.getY();

        mTouchedX = event.getX();
        mTouchedY = event.getY();

        mScreenTouched = true;

        List<Integer> mTouchedItem = new ArrayList<Integer>();

        // 테마 아이콘 클릭 시 이벤트
        mTouched = false;
        PointF tPoint = new PointF();
        Iterator<PointF> pointIterator = mPointFList.iterator();
        for (int i = 0; i < mPointFList.size(); i++) {
            tPoint = pointIterator.next();
            if (convertedX > tPoint.x - (mRestaurantBitmap.getWidth() / 2)
                    && convertedX < tPoint.x
                    + (mRestaurantBitmap.getWidth() / 2)
                    && convertedY > tPoint.y
                    - (mRestaurantBitmap.getHeight() / 2)
                    && convertedY < tPoint.y
                    + (mRestaurantBitmap.getHeight() / 2)) {
                mTouched = true;
                mTouchedItem.add(i);
            }
        }

        if(mTouched){
            int TouchedItemNum = 0;
            final CharSequence[] items = new CharSequence[mTouchedItem.size()];
            for(int i = 0; i < mTouchedItem.size(); i++){
                TouchedItemNum = mTouchedItem.get(i);
                items[i] = mPointHashMap.get(TouchedItemNum);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);     // 여기서 this는 Activity의 this
            // 여기서 부터는 알림창의 속성 설정
            builder.setTitle("위치선택")       // 제목 설정
                    .setItems(items, new DialogInterface.OnClickListener(){    // 목록 클릭시 설정
                        public void onClick(DialogInterface dialog, int index){
                            //items[index] 가 String으로 선택된 Theme 이름을 뱉음
                            //인텐트를 통한 ThemeInfoActivity로 이름 전달
                            Intent intent = new Intent(mContext,ThemeInfoActivity.class);
                            intent.putExtra("themename",items[index]);
                            mContext.startActivity(intent);
                        }
                    });

            AlertDialog dialog = builder.create();      // 알림창 객체 생성
            dialog.show();                              // 알림창 띄우기*/
        }

        return super.onTouchEvent(event);
    }

    // 스크린이 터치될때의 효과를 그림 원 3개를 물결처럼 그림
    private void drawTouchEffect(Canvas pCanvas) {
        // TODO Auto-generated method stub

        pCanvas.drawCircle(mTouchedX, mTouchedY, mCounter * 1,
                mPaint);
        pCanvas.drawCircle(mTouchedX, mTouchedY, mCounter * 2,
                mPaint);
        pCanvas.drawCircle(mTouchedX, mTouchedY, mCounter * 3,
                mPaint);
    }

    // MainActivity의 Seekbar에서 보여지는 범위 설정(0~100km)
    public void setmVisibleDistance(int mVisibleDistance){
        this.mVisibleDistance = mVisibleDistance;
    }

    // MainActivity의 DrawerMenu에서 Theme 설정
    public void setTheme(String Themename){
        if(Themename.equals("restaurant")){
            mDBHandler.setmTheme(DBHandler.RESTAURANT);
            mDBHandler.readDB();
        }else if(Themename.equals(("tourist"))){
            mDBHandler.setmTheme(DBHandler.TOURIST);
            mDBHandler.readDB();
        }else if(Themename.equals(("shopping"))){
            mDBHandler.setmTheme(DBHandler.SHOPPING);
            mDBHandler.readDB();
        }else if(Themename.equals(("restroom"))){
            mDBHandler.setmTheme(DBHandler.RESTROOM);
            mDBHandler.readDB();
        }
    }
}
