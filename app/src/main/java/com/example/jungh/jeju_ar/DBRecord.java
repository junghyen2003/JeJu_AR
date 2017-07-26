package com.example.jungh.jeju_ar;

/**
 * Created by jungh on 2017-01-05.
 */

// DB에서 레코드 읽고 생성, setter로 필드 저장, getter로 필드 참조
public class DBRecord {

    private String theme;
    private String name;
    private double latitude;
    private double longitude;
    private int tb_locations_id;
    private String vr_theme;
    private String vr_path;

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getVr_theme() {
        return vr_theme;
    }

    public void setVr_theme(String vr_theme) {
        this.vr_theme = vr_theme;
    }

    public String getVr_path() {
        return vr_path;
    }

    public void setVr_path(String vr_path) {
        this.vr_path = vr_path;
    }


    public int getTb_locations_id() {
        return tb_locations_id;
    }

    public void setTb_locations_id(int tb_locations_id) {
        this.tb_locations_id = tb_locations_id;
    }
}
