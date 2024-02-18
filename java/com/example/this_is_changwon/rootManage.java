package com.example.this_is_changwon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Entity;
import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;

import java.util.ArrayList;
import java.util.List;

public class rootManage extends AppCompatActivity {

    private MainActivity mainActivity;
    private List<Data> Main_dataList = new ArrayList<>(); // 어댑터 선언;
    private Adapter Main_adapter;
    private RecyclerView recyclerview;
    private LinearLayoutManager linearLayoutManager;


    private Appdatabase db;

    EditText spot_name;
    Button spot_add, resetname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_manage);

        db = Appdatabase.getInstance(this);

        spot_name = findViewById(R.id.name_write);
        spot_add = findViewById(R.id.name_add);
        resetname = findViewById(R.id.name_reset);

        recyclerview = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerview.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL)); ///구분선 넣어주는 옵션
        linearLayoutManager = new LinearLayoutManager(this); // 레이아웃 매니져
        recyclerview.setLayoutManager(linearLayoutManager); // 리사이클러뷰에 set 해준다
        Main_dataList =  db.dataDao().getAll();
        Main_adapter = new Adapter(rootManage.this, Main_dataList); // 어댑터에 어레이리스트 넣어준다.
        recyclerview.setAdapter(Main_adapter);// 리사이클러뷰에 어댑터 set 해준다.



        spot_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int list_count = Main_adapter.getItemCount();
                String inputname = spot_name.getText().toString();

                if(inputname.equals("")){
                    Toast.makeText(getApplicationContext(),"일정의 이름을 입력하세요", Toast.LENGTH_SHORT).show();
                }else{
                    if(list_count < 5){

                        /*Data data = new Data();
                        data.setName(inputname);
                        db.dataDao().insert(data);*/

                        spot_name.setText("");
                        Log.d("TravelSpot 테이블 확인1", db.table1().findAll().toString());
                        Log.d("TravelSpot 테이블 확인2", db.table2().findAll().toString());
                        Log.d("TravelSpot 테이블 확인3", db.table3().findAll().toString());
                        Log.d("TravelSpot 테이블 확인4", db.table4().findAll().toString());
                        Log.d("TravelSpot 테이블 확인5", db.table5().findAll().toString());

                        if(db.table1().findAll().toString() == "[]"){
                            Log.d("TravelSpot 테이블 비었나?", "ㅇㅇ");
                            Data data = new Data(inputname, "spot0");
                            TravelSpot travelSpot = new TravelSpot("창원시청", "경상남도 창원시 성산구 중앙대로 151", "128.6818", "35.2280");
                            db.dataDao().insert(data);
                            db.table1().insert(travelSpot);
                            Log.d("설정한 테이블", data.getTitle());
                        }else if(db.table2().findAll().toString() == "[]"){
                            Log.d("TravelSpot1 테이블 비었나?", "ㅇㅇ");
                            Data data = new Data(inputname, "spot1");
                            TravelSpot1 travelSpot1 = new TravelSpot1("창원시청", "경상남도 창원시 성산구 중앙대로 151", "128.6818", "35.2280");
                            db.dataDao().insert(data);
                            db.table2().insert1(travelSpot1);

                        }else if(db.table3().findAll().toString() == "[]"){
                            Log.d("TravelSpot2 테이블 비었나?", "ㅇㅇ");
                            Data data = new Data(inputname, "spot2");
                            TravelSpot2 travelSpot2 = new TravelSpot2("창원시청", "경상남도 창원시 성산구 중앙대로 151", "128.6818", "35.2280");
                            db.dataDao().insert(data);
                            db.table3().insert1(travelSpot2);

                        }else if(db.table4().findAll().toString() == "[]"){
                            Log.d("TravelSpot3 테이블 비었나?", "ㅇㅇ");
                            Data data = new Data(inputname, "spot3");
                            TravelSpot3 travelSpot3 = new TravelSpot3("창원시청", "경상남도 창원시 성산구 중앙대로 151", "128.6818", "35.2280");
                            db.dataDao().insert(data);
                            db.table4().insert1(travelSpot3);

                        }else if(db.table5().findAll().toString() == "[]"){
                            Log.d("TravelSpot4 테이블 비었나?", "ㅇㅇ");
                            Data data = new Data(inputname, "spot4");
                            TravelSpot4 travelSpot4 = new TravelSpot4("창원시청", "경상남도 창원시 성산구 중앙대로 151", "128.6818", "35.2280");
                            db.dataDao().insert(data);
                            db.table5().insert1(travelSpot4);

                        }
                        Log.d("TravelSpot 테이블 다시 확인1", db.table1().findAll().toString());
                        Log.d("TravelSpot 테이블 다시 확인2", db.table2().findAll().toString());
                        Log.d("TravelSpot 테이블 다시 확인3", db.table3().findAll().toString());
                        Log.d("TravelSpot 테이블 다시 확인4", db.table4().findAll().toString());
                        Log.d("TravelSpot 테이블 다시 확인5", db.table5().findAll().toString());

                        Log.d("Data 테이블 확인", db.dataDao().getAll().toString());
                        Main_dataList.clear();
                        Main_dataList.addAll(db.dataDao().getAll());
                        Main_adapter.notifyDataSetChanged();

                        int update_list_count = Main_adapter.getItemCount();
                        int fristItemed = Main_dataList.get(update_list_count - 1).getId();
                        Log.d("생성순서", String.valueOf(fristItemed));


                    }else{
                        Toast.makeText(getApplicationContext(),"일정은 5개까지 생성할 수 있습니다", Toast.LENGTH_SHORT).show();
                    }


                }

            }
        });

        resetname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.dataDao().reset(Main_dataList);
                db.table1().deleteAllTravelSpots();
                db.table2().deleteAllTravelSpots();
                db.table3().deleteAllTravelSpots();
                db.table4().deleteAllTravelSpots();
                db.table5().deleteAllTravelSpots();

                Main_dataList.clear();
                Main_dataList.addAll(db.dataDao().getAll());
                Main_adapter.notifyDataSetChanged();
            }
        });
    }

    }