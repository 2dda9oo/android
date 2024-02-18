package com.example.this_is_changwon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface recommendedSpotDao {
    @Query("SELECT * FROM recommend_table")
    List<recommendedSpot> findAll();

    @Query("SELECT * FROM recommend_table WHERE id=:id")
    recommendedSpot findById(int id);

    @Query("DELETE FROM recommend_table")
    void deleteAllrecommendedSpot();

    @Insert
    void insert(recommendedSpot recommendedSpot);
    @Update
    void update(recommendedSpot recommendedSpot);
    @Delete
    void delete(recommendedSpot recommendedSpot);
}
