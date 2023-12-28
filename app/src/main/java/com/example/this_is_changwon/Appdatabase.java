package com.example.this_is_changwon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

@Database(entities = {Data.class, TravelSpot.class, TravelSpot1.class, TravelSpot2.class, TravelSpot3.class, TravelSpot4.class, SearchData.class, cafeSpot.class, Spot2.class, recommendedSpot.class}, version = 11, exportSchema = false)
public abstract class Appdatabase extends RoomDatabase {

    private static Appdatabase db;
    private static String DATABASE_NAME = "db";


    public synchronized static Appdatabase getInstance(Context context)
    {
        if (db == null)
        {
            db = Room.databaseBuilder(context.getApplicationContext(), Appdatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return db;
    }

    public abstract DataDao dataDao();

    public abstract TravelDao table1();
    public abstract TravelDao1 table2();
    public abstract TravelDao2 table3();
    public abstract TravelDao3 table4();
    public abstract TravelDao4 table5();

    public abstract SearchDao searchDao();

    public abstract cafeSpotDao cafeSpotDao();
    public abstract Spot2Dao spot2Dao();
    public abstract recommendedSpotDao recommendedSpotDao();

}
