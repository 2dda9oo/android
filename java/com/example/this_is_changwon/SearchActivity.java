package com.example.this_is_changwon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private List<SearchData> Main_dataList = new ArrayList<>();
    private SearchAdapter Main_Adapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private final String TAG = this.getClass().getSimpleName();
    private String clientId = "5MB5dzIWO9Iw2DT3rWKs";
    private String clientSecret = "4lzPNQjrlu";

    private Appdatabase db;

    Button spot, food, kor, jap, ame, chi, chicken, soju, coffee, bread;
    RadioGroup radioGroup, radioGroup_init;
    SearchView searchView;

    String foodData;
    String sdata, sresult, ImageUrl;
    StringTokenizer stfood, cutting_stfood, search_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        spot = (Button) findViewById(R.id.search_spot);
        kor = (Button) findViewById(R.id.search_korean);
        jap = (Button) findViewById(R.id.search_japanease);
        ame = (Button) findViewById(R.id.search_american);
        chi = (Button) findViewById(R.id.search_chinease);
        chicken = (Button) findViewById(R.id.search_chicken);
        soju = (Button) findViewById(R.id.search_drink);
        coffee = (Button) findViewById(R.id.search_coffee);
        bread = (Button) findViewById(R.id.search_bread);

        db = Appdatabase.getInstance(this);

        recyclerView = findViewById(R.id.search_recycler);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        Main_dataList = db.searchDao().findAll();
        Main_Adapter = new SearchAdapter(SearchActivity.this, Main_dataList);
        recyclerView.setAdapter(Main_Adapter);
        Main_Adapter.notifyDataSetChanged();

        radioGroup = findViewById(R.id.radio);
        radioGroup_init = findViewById(R.id.radio_sigungu);
        searchView = findViewById(R.id.search_space);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                getSearchResult("창원" + s, new SearchCallback() {
                    @Override
                    public void onSuccess(String result) {
                        sdata = result;
                        sresult = parseXML(sdata);
                        if(sresult.equals("")){
                            Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                        } else {

                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    db.searchDao().deleteAllTravelSpots();

                                    int count = 0;
                                    int num1 = 0;
                                    int num2 = 0;
                                    int num3 = 0;
                                    int num4 = 0;
                                    int num5 = 0;

                                    StringTokenizer stBuffer = new StringTokenizer(sresult,"*");
                                    String title[] = new String[stBuffer.countTokens()];
                                    String category[] = new String[stBuffer.countTokens()];
                                    String address[] = new String[stBuffer.countTokens()];
                                    String mapx[] = new String[stBuffer.countTokens()];
                                    String mapy[] = new String[stBuffer.countTokens()];
                                    while(stBuffer.hasMoreTokens()){
                                        String cutting = stBuffer.nextToken();
                                        search_result = new StringTokenizer(cutting,"@");
                                        while(search_result.hasMoreTokens()){
                                            if(count % 5 == 0){
                                                title[num1] = search_result.nextToken();
                                                num1++;
                                                count++;
                                            }else if(count % 5 == 1){
                                                category[num2] = search_result.nextToken();
                                                num2++;
                                                count++;
                                            }else if(count % 5 == 2){
                                                address[num3] = search_result.nextToken();
                                                num3++;
                                                count++;
                                            }else if(count % 5 == 3){
                                                mapx[num4] = search_result.nextToken();
                                                mapx[num4] = mapx[num4].substring(0, 3) + "." + mapx[num4].substring(3);
                                                Log.d("위도", "결과값: " + mapx[num4]);
                                                num4++;
                                                count++;
                                            }else if(count % 5 == 4){
                                                mapy[num5] = search_result.nextToken();
                                                mapy[num5] = mapy[num5].substring(0, 2) + "." + mapy[num5].substring(2);
                                                Log.d("경도", "결과값: " + mapy[num5]);
                                                num5++;
                                                count++;
                                            }
                                        }
                                    }runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < title.length; i++){
                                                sdata = new SearchData(title[i], category[i], null, address[i], mapx[i], mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", title[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int num) {
                Log.d("클릭됨?", "ㅇㅇ");

                int selected_sigungu = radioGroup_init.getCheckedRadioButtonId();
                Log.d("클릭됨?", String.valueOf(selected_sigungu));
                RadioButton selectedRbtn = findViewById(selected_sigungu);
                String sigungu_name = selectedRbtn.getText().toString();
                Log.d("이름은?", sigungu_name);

                if(sigungu_name.equals("마산회원구")) { //마산회원구 선택

                    switch (num){

                        case R.id.search_spot:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_SHORT).show();

                            String csvFilePath = "final.csv";
                            List<cafeSpot> spotList = readCSV(csvFilePath, "여행지");
                            List<cafeSpot> filteredSpots = new ArrayList<>();

                            Set<String> addedNames = new HashSet<>(); // 이미 추가한 이름을 기록할 Set

                            for (cafeSpot spot : spotList) {
                                if (spot.getAddress().contains("마산회원구") && !addedNames.contains(spot.getName())) {
                                    filteredSpots.add(spot);
                                    addedNames.add(spot.getName()); // 추가한 이름을 기록
                                }
                            }
                            SearchData sdata;
                            for(cafeSpot spot : filteredSpots){
                                sdata = new SearchData(spot.getName(), spot.getType(), spot.getTime(), spot.getAddress(), spot.getLat(), spot.getLon());
                                db.searchDao().insert(sdata);
                                Log.d("식당 이름", spot.getName());
                            }
                            Main_dataList.clear();
                            Main_dataList.addAll(db.searchDao().findAll());
                            Main_Adapter.notifyDataSetChanged();
                            break;

                        case R.id.search_korean:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");

                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(7, "01");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_japanease:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(7,"03");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_american:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(7, "04");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_chinease:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(7, "02");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_chicken:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getChicken(7);
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_drink:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(7, "11");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_coffee:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(7, "12");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_bread:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(7, "10");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;


                    }

                } else if(sigungu_name.equals("마산합포구")){ // 마산합포구 선택

                    switch (num){
                        case R.id.search_spot:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_SHORT).show();

                            String csvFilePath = "final.csv";
                            List<cafeSpot> spotList = readCSV(csvFilePath, "여행지");
                            List<cafeSpot> filteredSpots = new ArrayList<>();

                            Set<String> addedNames = new HashSet<>(); // 이미 추가한 이름을 기록할 Set

                            for (cafeSpot spot : spotList) {
                                if (spot.getAddress().contains("마산합포구") && !addedNames.contains(spot.getName())) {
                                    filteredSpots.add(spot);
                                    addedNames.add(spot.getName()); // 추가한 이름을 기록
                                }
                            }
                            SearchData sdata;
                            for(cafeSpot spot : filteredSpots){
                                sdata = new SearchData(spot.getName(), spot.getType(), spot.getTime(), spot.getAddress(), spot.getLat(), spot.getLon());
                                db.searchDao().insert(sdata);
                                Log.d("식당 이름", spot.getName());
                            }
                            Main_dataList.clear();
                            Main_dataList.addAll(db.searchDao().findAll());
                            Main_Adapter.notifyDataSetChanged();
                            break;
                        case R.id.search_korean:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");

                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(5, "01");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_japanease:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(5,"03");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_american:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(5, "04");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_chinease:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(5, "02");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_chicken:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getChicken(5);
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_drink:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(5, "11");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_coffee:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(5, "12");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_bread:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(5, "10");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                    }

                } else if(selected_sigungu == 2131362311){ // 성산구 선택

                    switch (num){
                        case R.id.search_spot:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_SHORT).show();

                            String csvFilePath = "final.csv";
                            List<cafeSpot> spotList = readCSV(csvFilePath, "여행지");
                            List<cafeSpot> filteredSpots = new ArrayList<>();

                            Set<String> addedNames = new HashSet<>(); // 이미 추가한 이름을 기록할 Set

                            for (cafeSpot spot : spotList) {
                                if (spot.getAddress().contains("성산구") && !addedNames.contains(spot.getName())) {
                                    filteredSpots.add(spot);
                                    addedNames.add(spot.getName()); // 추가한 이름을 기록
                                }
                            }
                            SearchData sdata;
                            for(cafeSpot spot : filteredSpots){
                                sdata = new SearchData(spot.getName(), spot.getType(), spot.getTime(), spot.getAddress(), spot.getLat(), spot.getLon());
                                db.searchDao().insert(sdata);
                                Log.d("식당 이름", spot.getName());
                            }
                            Main_dataList.clear();
                            Main_dataList.addAll(db.searchDao().findAll());
                            Main_Adapter.notifyDataSetChanged();
                            break;
                        case R.id.search_korean:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");

                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(3, "01");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_japanease:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(3,"03");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_american:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(3, "04");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_chinease:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(3, "02");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_chicken:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getChicken(3);
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_drink:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(3, "11");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_coffee:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(3, "12");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_bread:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(3, "10");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;


                    }

                } else if(selected_sigungu == 2131362409){ // 의창구 선택

                    switch (num){
                        case R.id.search_spot:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_SHORT).show();

                            String csvFilePath = "final.csv";
                            List<cafeSpot> spotList = readCSV(csvFilePath, "여행지");
                            List<cafeSpot> filteredSpots = new ArrayList<>();

                            Set<String> addedNames = new HashSet<>(); // 이미 추가한 이름을 기록할 Set

                            for (cafeSpot spot : spotList) {
                                if (spot.getAddress().contains("의창구") && !addedNames.contains(spot.getName())) {
                                    filteredSpots.add(spot);
                                    addedNames.add(spot.getName()); // 추가한 이름을 기록
                                }
                            }
                            SearchData sdata;
                            for(cafeSpot spot : filteredSpots){
                                sdata = new SearchData(spot.getName(), spot.getType(), spot.getTime(), spot.getAddress(), spot.getLat(), spot.getLon());
                                db.searchDao().insert(sdata);
                                Log.d("식당 이름", spot.getName());
                            }
                            Main_dataList.clear();
                            Main_dataList.addAll(db.searchDao().findAll());
                            Main_Adapter.notifyDataSetChanged();
                            break;
                        case R.id.search_korean:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");

                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(1, "01");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_japanease:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(1,"03");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_american:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(1, "04");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_chinease:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(1, "02");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_chicken:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getChicken(1);
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_drink:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(1, "11");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_coffee:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(1, "12");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_bread:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(1, "10");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;


                    }

                } else if(selected_sigungu == 2131362086){ // 진해구 선택

                    switch (num){
                        case R.id.search_spot:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_SHORT).show();

                            String csvFilePath = "final.csv";
                            List<cafeSpot> spotList = readCSV(csvFilePath, "여행지");
                            List<cafeSpot> filteredSpots = new ArrayList<>();

                            Set<String> addedNames = new HashSet<>(); // 이미 추가한 이름을 기록할 Set

                            for (cafeSpot spot : spotList) {
                                if (spot.getAddress().contains("진해구") && !addedNames.contains(spot.getName())) {
                                    filteredSpots.add(spot);
                                    addedNames.add(spot.getName()); // 추가한 이름을 기록
                                }
                            }
                            SearchData sdata;
                            for(cafeSpot spot : filteredSpots){
                                sdata = new SearchData(spot.getName(), spot.getType(), spot.getTime(), spot.getAddress(), spot.getLat(), spot.getLon());
                                db.searchDao().insert(sdata);
                                Log.d("식당 이름", spot.getName());
                            }
                            Main_dataList.clear();
                            Main_dataList.addAll(db.searchDao().findAll());
                            Main_Adapter.notifyDataSetChanged();
                            break;
                        case R.id.search_korean:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");

                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(9, "01");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_japanease:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(9,"03");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_american:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(9, "04");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_chinease:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(9, "02");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;

                        case R.id.search_chicken:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getChicken(9);
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_drink:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(9, "11");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_coffee:

                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(9, "12");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;
                        case R.id.search_bread:
                            Toast.makeText(getApplicationContext(), "검색중입니다. 잠시만 기다려주세요", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("클릭됨?", "ㅇㅇ");
                                    db.searchDao().deleteAllTravelSpots();
                                    foodData = getfood(9, "10");
                                    int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0, count = 0;
                                    stfood = new StringTokenizer(foodData, "*");

                                    String foodNm[] = new String[stfood.countTokens()];
                                    String foodSi[] = new String[stfood.countTokens()];
                                    String foodki[] = new String[stfood.countTokens()];
                                    String inoAddr[] = new String[stfood.countTokens()];
                                    String food_mapx[] = new String[stfood.countTokens()];
                                    String food_mapy[] = new String[stfood.countTokens()];

                                    while (stfood.hasMoreTokens()){
                                        cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                                        while(cutting_stfood.hasMoreTokens()){
                                            if(count % 6 == 0){
                                                foodNm[num1] = cutting_stfood.nextToken();
                                                num1++;
                                                count++;
                                            } else if (count % 6 == 1){
                                                foodSi[num2] = cutting_stfood.nextToken();
                                                num2++;
                                                count++;
                                            } else if (count % 6 == 2){
                                                foodki[num3] = cutting_stfood.nextToken();
                                                num3++;
                                                count++;
                                            } else if (count % 6 == 3){
                                                inoAddr[num4] = cutting_stfood.nextToken();
                                                num4++;
                                                count++;
                                            } else if (count % 6 == 4){
                                                food_mapx[num5] = cutting_stfood.nextToken();
                                                num5++;
                                                count++;
                                            } else if (count % 6 == 5){
                                                food_mapy[num6] = cutting_stfood.nextToken();
                                                num6++;
                                                count++;
                                            }
                                        }
                                    } runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SearchData sdata;
                                            for(int i = 0; i < foodNm.length; i++){
                                                sdata = new SearchData(foodNm[i], foodSi[i], foodki[i], inoAddr[i], food_mapx[i], food_mapy[i]);
                                                db.searchDao().insert(sdata);
                                                Log.d("식당 이름", foodNm[i]);
                                            }
                                            Main_dataList.clear();
                                            Main_dataList.addAll(db.searchDao().findAll());
                                            Main_Adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();

                            break;


                    }

                }
            }
        });

    }

    String getfood(int snum, String id){   // 식당 선택
        StringBuffer buffer = new StringBuffer();
        String queryUrl = "https://apis.data.go.kr/B553077/api/open/sdsc2/storeListInDong?serviceKey=JxPb3zcF6wDWwkLFct1EFT5xiiKWL5mS3vdQM%2F8kQXCqv%2BSW2ntspIlw1fad%2B5f%2FC8SRGRr2wqw68Vd6zWG3AQ%3D%3D&pageNo=1&numOfRows=30&divId=signguCd&key=4812"+String.valueOf(snum)+"&indsLclsCd=I2&indsMclsCd=I2"+id+"&type=xml";
        try{
            URL url = new URL(queryUrl);
            InputStream inputStream = url.openStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new InputStreamReader(inputStream, "UTF-8"));

            String tag;

            xmlPullParser.next();
            int eventType = xmlPullParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        tag = xmlPullParser.getName();
                        if(tag.equals("item")) ;

                        else if(tag.equals(("bizesNm"))) {
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if(tag.equals(("indsSclsNm"))){ // 음식점 분류
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if(tag.equals(("ksicNm"))){ // 음식점 세부사항
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if (tag.equals("lnoAdr")) {
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        }else if(tag.equals("lon")){
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if(tag.equals("lat")){
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xmlPullParser.getName();
                        if(tag.equals("item")) buffer.append("*");
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return buffer.toString();
    }

    String getChicken(int snum){
        StringBuffer buffer = new StringBuffer();
        String queryUrl = "https://apis.data.go.kr/B553077/api/open/sdsc2/storeListInDong?serviceKey=JxPb3zcF6wDWwkLFct1EFT5xiiKWL5mS3vdQM%2F8kQXCqv%2BSW2ntspIlw1fad%2B5f%2FC8SRGRr2wqw68Vd6zWG3AQ%3D%3D&pageNo=1&numOfRows=30&divId=signguCd&key=4812"+String.valueOf(snum)+"&indsLclsCd=I2&indsMclsCd=I210&indsSclsCd=I21006&type=xml";
        try{
            URL url = new URL(queryUrl);
            InputStream inputStream = url.openStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new InputStreamReader(inputStream, "UTF-8"));

            String tag;

            xmlPullParser.next();
            int eventType = xmlPullParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        tag = xmlPullParser.getName();
                        if(tag.equals("item")) ;

                        else if(tag.equals(("bizesNm"))) {
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if(tag.equals(("indsSclsNm"))){ // 음식점 분류
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if(tag.equals(("ksicNm"))){ // 음식점 세부사항
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if (tag.equals("lnoAdr")) {
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        }else if(tag.equals("lon")){
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if(tag.equals("lat")){
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xmlPullParser.getName();
                        if(tag.equals("item")) buffer.append("*");
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return buffer.toString();
    }

    String parseXML(String xmlString) {
        StringBuffer buffer = new StringBuffer();
        try {
            // DocumentBuilderFactory 생성
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            // DocumentBuilder 생성
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            // XML 문자열을 InputStream으로 변환
            InputStream is = new ByteArrayInputStream(xmlString.getBytes());

            // Document 객체 생성
            Document doc = dBuilder.parse(is);

            // 파싱할 요소의 이름
            String tagName = "item";

            // 파싱할 요소의 NodeList
            NodeList nodeList = doc.getElementsByTagName(tagName);

            // NodeList를 순회하면서 필요한 정보 추출
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    // 필요한 정보 추출
                    String title = element.getElementsByTagName("title").item(0).getTextContent();
                    title = title.replaceAll("[</b>]", "");
                    buffer.append(title);
                    buffer.append("@");

                    String category = element.getElementsByTagName("category").item(0).getTextContent();
                    buffer.append(category);
                    buffer.append("@");

                    String address = element.getElementsByTagName("address").item(0).getTextContent();
                    buffer.append(address);
                    buffer.append("@");

                    String mapx = element.getElementsByTagName("mapx").item(0).getTextContent();
                    buffer.append(mapx);
                    buffer.append("@");

                    String mapy = element.getElementsByTagName("mapy").item(0).getTextContent();
                    buffer.append(mapy);
                    buffer.append("*");


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public String data1 = "ok";
    void getSearchResult(String input, SearchCallback callback) {
        ApiInterface apiInterface = ApiClient.getInstance().create(ApiInterface.class);
        Call<String> call = apiInterface.getSearchResult(clientId, clientSecret, "local.xml", input, 10, 1);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String result = getResult(response.body());
                        callback.onSuccess(result); // 콜백 호출하여 결과값 전달
                        Log.e(TAG, "성공 : " + response.body());
                    } else {
                        throw new Exception("응답 실패");
                    }
                } catch (Exception e) {
                    callback.onFailure(e); // 콜백 호출하여 에러 전달
                    Log.e(TAG, "에러 : " + e.getMessage());
                }
            }


            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    String getResult(String s) {
        data1 = s;
        Log.e(TAG, "지역변수 저장 성공? : " + data1);

        return data1;
    }

    private List<cafeSpot> readCSV(String csvFilePath, String classify) {
        List<cafeSpot> spotList = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open(csvFilePath)));
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(","); // CSV 파일에서 쉼표로 데이터를 구분하여 배열로 저장
                String name = data[0];
                String type = data[1];
                String time = data[2];
                String address= data[3];
                String who = data[4];
                String what = data[5];
                String how = data[6];
                String image = data[7];
                String lat = data[8];
                String lon = data[9];

                if (type.equals(classify)) { // 또는 다른 조건으로 변경 가능
                    cafeSpot cafespot = new cafeSpot(name, type, time, address, who, what, how, image, lat, lon);
                    spotList.add(cafespot);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return spotList;
    }

}