package com.example.jungh.jeju_ar.common;

import android.os.Environment;

/**
 * Created by jungh on 2017-06-09.
 */

public class Constants {

    public static final String SERVER_IP = "http://211.225.79.43";

    //나무플러스 앱 관련 디렉토리
    public static final String SAVE_SITE_PATH = Environment.getExternalStorageDirectory() + "/namooplus/";

    //이미지가 임시로 저장되는 곳
    public static final String SAVE_IMAGE_TEMP_PATH = Environment.getExternalStorageDirectory() + "/namooplus/temp/";

    //이미지는 jpg 사용
    public static final String IMAGE_STRING_FORMAT = ".jpg";
}
