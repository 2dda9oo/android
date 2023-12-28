package com.example.this_is_changwon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TravelDao {

    @Query("SELECT * FROM spot0")
    List<TravelSpot> findAll();

    @Query("SELECT * FROM spot0 WHERE id=:id")
    TravelSpot findById(int id);

    @Query("DELETE FROM spot0")
    void deleteAllTravelSpots();

    @Query("DELETE FROM spot0 WHERE name = :name")
    void deleteTravelSpotByName(String name);

    @Insert
    void insert(TravelSpot travelSpot);
    @Update
    void update(TravelSpot travelSpot);
    @Delete
    void delete(TravelSpot travelSpot);


}
