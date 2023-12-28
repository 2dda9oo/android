package com.example.this_is_changwon;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spot1")
public class TravelSpot1 {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String addr;
    private String lon;
    private String lat;

    public TravelSpot1(String title, String addr, String lon, String lat) {
        this.title = title;
        this.addr = addr;
        this.lon = lon;
        this.lat = lat;
    }

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public String getAddr(){
        return addr;
    }
    public void setAddr(String addr){
        this.addr = addr;
    }

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

}
