package com.example.this_is_changwon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TravelDao1 {

    @Query("SELECT * FROM spot1")
    List<TravelSpot1> findAll();

    @Query("SELECT * FROM spot1 WHERE id=:id")
    TravelSpot1 findById(int id);

    @Query("DELETE FROM spot1")
    void deleteAllTravelSpots();

    @Query("DELETE FROM spot1 WHERE title = :name")
    void deleteTravelSpotByName(String name);

    @Insert
    void insert1(TravelSpot1 travelSpot1);
    @Update
    void update1(TravelSpot1 travelSpot1);
    @Delete
    void delete1(TravelSpot1 travelSpot1);

}
