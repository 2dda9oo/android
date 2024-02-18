package com.example.this_is_changwon;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "searchdata")
public class SearchData {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String sclsCd;
    private String ksicNm;
    private String addr;
    private String lon;
    private String lat;

    public SearchData(String name, String sclsCd, String ksicNm, String addr, String lon, String lat){
        this.name = name;
        this.sclsCd = sclsCd;
        this.ksicNm = ksicNm;
        this.addr = addr;
        this.lon = lon;
        this.lat = lat;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getAddr(){
        return addr;
    }
    public void setAddr(String addr){
        this.addr = addr;
    }

    public String getSclsCd(){return sclsCd;}
    public void setSclsCd(String sclsCd){this.sclsCd = sclsCd;}

    public String getKsicNm(){return ksicNm;}
    public void setKsicNm(String ksicNm){this.ksicNm = ksicNm;}

    public String getLon(){
        return lon;
    }
    public void setLon(String lon){
        this.lon = lon;
    }

    public String getLat(){
        return lat;
    }
    public void setLat(String lat){
        this.lat = lat;
    }


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
