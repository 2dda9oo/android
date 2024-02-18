package com.example.this_is_changwon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DataDao {
    @Insert
    void insert(Data mainData);

    @Delete
    void delete(Data mainData);

    @Delete
    void reset(List<Data> mainData);

    @Query("UPDATE table_data SET name = :sName WHERE ID = :sID")
    void update(int sID, String sName);

    @Query("SELECT * FROM table_data")
    List<Data> getAll();

    @Query("SELECT name FROM table_data WHERE id=:id")
    String findById(int id);

    @Query("SELECT title FROM table_data WHERE id = :id")
    String getTitleById(int id);

    @Query("SELECT name FROM table_data WHERE title = :title")
    String findNameByTitle(String title);

}
