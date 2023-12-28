package com.example.this_is_changwon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SearchDao {

    @Query("SELECT * FROM searchdata")
    List<SearchData> findAll();

    @Query("SELECT * FROM searchdata WHERE id=:id")
    SearchData findById(int id);

    @Query("DELETE FROM searchdata")
    void deleteAllTravelSpots();

    @Insert
    void insert(SearchData searchData);
    @Update
    void update(SearchData searchData);
    @Delete
    void delete(SearchData searchData);
}
