package com.example.this_is_changwon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TravelDao4 {

    @Query("SELECT * FROM spot4")
    List<TravelSpot4> findAll();

    @Query("SELECT * FROM spot4 WHERE id=:id")
    TravelSpot4 findById(int id);

    @Query("DELETE FROM spot4")
    void deleteAllTravelSpots();

    @Query("DELETE FROM spot4 WHERE title = :name")
    void deleteTravelSpotByName(String name);

    @Insert
    void insert1(TravelSpot4 travelSpot4);
    @Update
    void update1(TravelSpot4 travelSpot4);
    @Delete
    void delete1(TravelSpot4 travelSpot4);

}
