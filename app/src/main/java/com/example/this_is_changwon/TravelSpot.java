package com.example.this_is_changwon;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spot0")
public class TravelSpot {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String addr;
    private String lon;
    private String lat;

    public TravelSpot(String name, String addr, String lon, String lat) {
        this.name = name;
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
