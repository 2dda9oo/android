package com.example.this_is_changwon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Spot2Dao {
    @Query("SELECT * FROM spot_table2")
    List<Spot2> findAll();

    @Query("SELECT * FROM spot_table2 WHERE id=:id")
    Spot2 findById(int id);

    @Query("DELETE FROM spot_table2")
    void deleteAllSpot();

    @Insert
    void insert(Spot2 spot2);
    @Update
    void update(Spot2 spot2);
    @Delete
    void delete(Spot2 spot2);
}
