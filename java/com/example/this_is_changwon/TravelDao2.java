package com.example.this_is_changwon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TravelDao2 {

    @Query("SELECT * FROM spot2")
    List<TravelSpot2> findAll();

    @Query("SELECT * FROM spot2 WHERE id=:id")
    TravelSpot2 findById(int id);

    @Query("DELETE FROM spot2")
    void deleteAllTravelSpots();

    @Query("DELETE FROM spot2 WHERE title = :name")
    void deleteTravelSpotByName(String name);

    @Insert
    void insert1(TravelSpot2 travelSpot2);
    @Update
    void update1(TravelSpot2 travelSpot2);
    @Delete
    void delete1(TravelSpot2 travelSpot2);

}
