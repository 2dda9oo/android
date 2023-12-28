package com.example.this_is_changwon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface cafeSpotDao {
    @Query("SELECT * FROM cafeSpot_table")
    List<cafeSpot> findAll();

    @Query("SELECT * FROM cafeSpot_table WHERE id=:id")
    cafeSpot findByID(int id);

    @Query("DELETE FROM cafeSpot_table")
    void deleteAllcafeSpot();

    @Insert
    void insert(cafeSpot cafeSpot);
    @Update
    void update(cafeSpot cafeSpot);
    @Delete
    void delete(cafeSpot cafeSpot);
}
