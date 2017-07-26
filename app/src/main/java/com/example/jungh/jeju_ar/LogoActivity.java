package com.example.jungh.jeju_ar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import static android.os.Build.VERSION_CODES.M;

public class LogoActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    // 현재 GPS 사용 여부
    boolean isGPSEnabled = false;

    public final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        permission_check();
    }

    //권한 사용 체크
    public void permission_check() {
        if (Build.VERSION.SDK_INT >= M) {
            // 권한이 없을 경우
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    ||  ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        ||ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // 사용자가 임의로 권한을 취소시킨 경우
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA);
                } else {
                    // 최초로 권한을 요청하는 경우 (첫 실행)
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA);
                }
            } else {
                // 사용 권한이 모두 있을 경우
                start_main();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(LogoActivity.this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (!(grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(LogoActivity.this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (!(grantResults.length > 0 && grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(LogoActivity.this, "파일 저장 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    start_main();
                }
                return;
            }
        }
    }

    public void start_main() {
        if (turnGPSOn()){
            Handler handler = new Handler();
            // 데이터베이스 다운로드
            new DBHandler(this).copyDB();
            Toast.makeText(this, "DB 최신화 완료...", Toast.LENGTH_SHORT).show();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(LogoActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        }
    }

    private boolean turnGPSOn() {
        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {
            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
            gsDialog.setTitle("위치 서비스 설정");
            gsDialog.setMessage("위치 서비스 기능을 설정하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
            // Dialog 뒤로가기 막기
            gsDialog.setCancelable(false);
            gsDialog.setPositiveButton("네", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                    isGPSEnabled   = true;
                }
            })
                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(),"GPS를 켜고 다시 시도해 주시기 바랍니다.",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }).create().show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRestart(){
        super.onRestart();
        start_main();
    }
}
