package com.example.this_is_changwon;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spot_table2")
public class Spot2 {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "time")
    private String time;

    @ColumnInfo(name = "inorout")
    private String inorout;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "info")
    private String info;

    @ColumnInfo(name = "detail")
    private String detail;

    @ColumnInfo(name = "who")
    private String who;

    @ColumnInfo(name = "what")
    private String what;

    @ColumnInfo(name = "how")
    private String how;

    @ColumnInfo(name = "image")
    private String image;

    @ColumnInfo(name = "lat")
    private String lat;

    @ColumnInfo(name = "lon")
    private String lon;

    @ColumnInfo(name = "season")
    private String season;

    @ColumnInfo(name = "money")
    private String money;

    public Spot2(String name, String time, String inorout, String address, String info, String detail, String who, String what, String how, String image, String lat, String lon, String season, String money){
        this.name = name;
        this.time = time;
        this.inorout = inorout;
        this.address = address;
        this.info = info;
        this.detail = detail;
        this.who = who;
        this.what = what;
        this.what = how;
        this.address = image;
        this.lat = lat;
        this.lon = lon;
        this.season = season;
        this.money = money;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInorout() {
        return inorout;
    }

    public void setInorout(String inorout) {
        this.inorout = inorout;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getHow() {
        return how;
    }

    public void setHow(String how) {
        this.how = how;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }
}
