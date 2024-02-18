package com.example.this_is_changwon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TravelDao3 {

    @Query("SELECT * FROM spot3")
    List<TravelSpot3> findAll();

    @Query("SELECT * FROM spot3 WHERE id=:id")
    TravelSpot3 findById(int id);

    @Query("DELETE FROM spot3")
    void deleteAllTravelSpots();

    @Query("DELETE FROM spot3 WHERE title = :name")
    void deleteTravelSpotByName(String name);

    @Insert
    void insert1(TravelSpot3 travelSpot3);
    @Update
    void update1(TravelSpot3 travelSpot3);
    @Delete
    void delete1(TravelSpot3 travelSpot3);

}
