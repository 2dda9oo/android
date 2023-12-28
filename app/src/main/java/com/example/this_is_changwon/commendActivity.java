package com.example.this_is_changwon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class commendActivity extends AppCompatActivity {

    private RadioGroup radioWhoGroup;
    private RadioGroup radioWhereGroup;
    private RadioGroup radioHowGroup;
    private RadioGroup radioTimeGroup;
    Button startButton, commend_add;

    private String currentLat;
    private String currentLon;

    List<recommendedSpot> finalrecommend = new ArrayList<>();
    private Appdatabase db;
    private recommendAdapter recommendedAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commend);

        radioWhoGroup = findViewById(R.id.radio_who);
        radioWhereGroup = findViewById(R.id.radio_what);
        radioHowGroup = findViewById(R.id.radio_how);
        radioTimeGroup = findViewById(R.id.radio_time);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            currentLat = extras.getString("latitude");
            currentLon = extras.getString("longitude");
        }

        db = Appdatabase.getInstance(this);

        recyclerView = findViewById(R.id.recommend_recycler);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        finalrecommend.clear();
        recommendedAdapter = new recommendAdapter(commendActivity.this, finalrecommend);
        recyclerView.setAdapter(recommendedAdapter);

        commend_add = (Button) findViewById(R.id.commend_add);
        startButton = (Button) findViewById(R.id.commend_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.recommendedSpotDao().deleteAllrecommendedSpot();
                // 선택된 라디오 버튼의 ID를 가져와서 처리하는 코드
                int selectedWhoId = radioWhoGroup.getCheckedRadioButtonId();
                int selectedWhatId = radioWhereGroup.getCheckedRadioButtonId();
                int selectedHowId = radioHowGroup.getCheckedRadioButtonId();
                int selectedTimeId = radioTimeGroup.getCheckedRadioButtonId();

                // 선택된 라디오 버튼이 하나라도 없으면 Toast 메시지 출력
                if (selectedWhoId == -1 || selectedWhatId == -1 || selectedHowId == -1 || selectedTimeId == -1) {
                    Toast.makeText(getApplicationContext(), "모든 조건을 설정해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 선택된 라디오 버튼의 ID를 기반으로 선택 정보 가져오기
                String whoChoice = getRadioButtonChoice(selectedWhoId);
                String whereChoice = getRadioButtonChoice(selectedWhatId);
                String howChoice = getRadioButtonChoice(selectedHowId);
                String timeChoice = getRadioButtonChoice(selectedTimeId);

                // 현재 시간 정보 가져오기
                String currentTime = getCurrentTime();

                Log.d("뭐뭐 선택되었나", whoChoice + " " + whereChoice + " " +howChoice + " " + timeChoice);
                Log.d("현재 시간", currentTime);
                Log.d("현재 위도와 경도", currentLat+ " " + currentLon);


                double currentLatValue = Double.parseDouble(currentLon);
                double currentLonValue = Double.parseDouble(currentLat);
                LatLng currentLatLng = new LatLng(currentLatValue, currentLonValue);

                List<recommendedSpot> recommendedSpots = new ArrayList<>();

                switch (timeChoice) {
                    case "~4시간":
                        String routePattern = RouteCommend(currentTime, timeChoice);
                        Log.d("추천 일정 패턴", routePattern);
                        StringTokenizer stRoute = new StringTokenizer(routePattern, "-");
                        while(stRoute.hasMoreTokens()){
                            String token = stRoute.nextToken();
                            if(token.equals("1")){
                                Log.d("추천일정 번호", token);
                                String csvFilePath = "final.csv";
                                List<cafeSpot> nearbySpots = new ArrayList<>();
                                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "음식");
                                List<cafeSpot> morningSpots = filterSpotListByTime(spotList, "아침");


                                if(howChoice.equals("자차")){
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 2 / 111.0;
                                            double REFERLNG = 2 / 88.74;

                                            for (cafeSpot spot : morningSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //2km 내 음식점이 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 4 / 111.0;
                                                REFERLNG = 4 / 88.74;

                                                for (cafeSpot spot : morningSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //4km 내 음식점이 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 10 / 111.0;
                                                REFERLNG = 10 / 88.74;

                                                for (cafeSpot spot : morningSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 3 / 111.0;
                                            double REFERLNG = 3 / 88.74;

                                            for (cafeSpot spot : morningSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //3km 내 음식점 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 6 / 111.0;
                                                REFERLNG = 6 / 88.74;

                                                for (cafeSpot spot : morningSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //6km 내 음식점 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 12 / 111.0;
                                                REFERLNG = 12 / 88.74;

                                                for (cafeSpot spot : morningSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }

                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 5 / 111.0;
                                        double REFERLNG = 5 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : morningSpots) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //5km 내 음식점이 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 8 / 111.0;
                                            REFERLNG = 8 / 88.74;

                                            for (cafeSpot spot : morningSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                        //8km 내 음식점이 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 12 / 111.0;
                                            REFERLNG = 12 / 88.74;

                                            for (cafeSpot spot : morningSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }

                                } else if(howChoice.equals("대중교통")) {
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : morningSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 음식점이 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 2 / 111.0;
                                                REFERLNG = 2 / 88.74;

                                                for (cafeSpot spot : morningSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //2km 내 음식점이 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 7 / 111.0;
                                                REFERLNG = 7 / 88.74;

                                                for (cafeSpot spot : morningSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : morningSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 음식점이 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 2 / 111.0;
                                                REFERLNG = 2 / 88.74;

                                                for (cafeSpot spot : morningSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //2km 내 음식점이 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 5 / 111.0;
                                                REFERLNG = 5 / 88.74;

                                                for (cafeSpot spot : morningSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 3 / 111.0;
                                        double REFERLNG = 3 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : morningSpots) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //5km 내 음식점이 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 5 / 111.0;
                                            REFERLNG = 5 / 88.74;

                                            for (cafeSpot spot : morningSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                        //5km 내 음식점이 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 8 / 111.0;
                                            REFERLNG = 8 / 88.74;

                                            for (cafeSpot spot : morningSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }
                                }

                                for(cafeSpot nearspot : nearbySpots){
                                    String name = nearspot.getName();
                                    String time = nearspot.getTime();
                                    Log.d("Spot Info", "밥집 이름: " + name + ", 식사시간: " + time);
                                }

                                if (!nearbySpots.isEmpty()) {
                                    Random random = new Random();
                                    int randomIndex = random.nextInt(nearbySpots.size());
                                    cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                                    recommendedSpot recommended = new recommendedSpot(
                                            nearbySpot.getName(),
                                            nearbySpot.getType(),
                                            nearbySpot.getAddress(),
                                            nearbySpot.getImage(),
                                            nearbySpot.getLat(),
                                            nearbySpot.getLon()
                                    );

                                    if (!recommendedSpots.contains(recommended)) {
                                        recommendedSpots.add(recommended);
                                    } else {
                                        int attempts = 0;
                                        int maxAttempts = nearbySpots.size() * 2;

                                        while (attempts < maxAttempts) {
                                            randomIndex = random.nextInt(nearbySpots.size());
                                            nearbySpot = nearbySpots.get(randomIndex);

                                            recommended = new recommendedSpot(
                                                    nearbySpot.getName(),
                                                    nearbySpot.getType(),
                                                    nearbySpot.getAddress(),
                                                    nearbySpot.getImage(),
                                                    nearbySpot.getLat(),
                                                    nearbySpot.getLon()
                                            );

                                            if (!recommendedSpots.contains(recommended)) {
                                                recommendedSpots.add(recommended);
                                                break;
                                            }

                                            attempts++;
                                        }
                                    }
                                }



                            } else if(token.equals("2")){
                                Log.d("추천일정 번호", token);
                                String csvFilePath = "final.csv";
                                List<cafeSpot> nearbySpots = new ArrayList<>();
                                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "음식");
                                List<cafeSpot> lunchSpots = filterSpotListByTime(spotList, "점심");

                                if(howChoice.equals("자차")){
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 2 / 111.0;
                                            double REFERLNG = 2 / 88.74;

                                            for (cafeSpot spot : lunchSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //2km 내 음식점이 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 4 / 111.0;
                                                REFERLNG = 4 / 88.74;

                                                for (cafeSpot spot : lunchSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //4km 내 음식점이 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 10 / 111.0;
                                                REFERLNG = 10 / 88.74;

                                                for (cafeSpot spot : lunchSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 3 / 111.0;
                                            double REFERLNG = 3 / 88.74;

                                            for (cafeSpot spot : lunchSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //3km 내 음식점 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 6 / 111.0;
                                                REFERLNG = 6 / 88.74;

                                                for (cafeSpot spot : lunchSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //6km 내 음식점 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 12 / 111.0;
                                                REFERLNG = 12 / 88.74;

                                                for (cafeSpot spot : lunchSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }

                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 5 / 111.0;
                                        double REFERLNG = 5 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : lunchSpots) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //5km 내 음식점이 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 8 / 111.0;
                                            REFERLNG = 8 / 88.74;

                                            for (cafeSpot spot : lunchSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                        //8km 내 음식점이 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 12 / 111.0;
                                            REFERLNG = 12 / 88.74;

                                            for (cafeSpot spot : lunchSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }

                                } else if(howChoice.equals("대중교통")) {
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : lunchSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 음식점이 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 2 / 111.0;
                                                REFERLNG = 2 / 88.74;

                                                for (cafeSpot spot : lunchSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //2km 내 음식점이 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 7 / 111.0;
                                                REFERLNG = 7 / 88.74;

                                                for (cafeSpot spot : lunchSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : lunchSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 음식점이 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 2 / 111.0;
                                                REFERLNG = 2 / 88.74;

                                                for (cafeSpot spot : lunchSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //2km 내 음식점이 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 5 / 111.0;
                                                REFERLNG = 5 / 88.74;

                                                for (cafeSpot spot : lunchSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 3 / 111.0;
                                        double REFERLNG = 3 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : lunchSpots) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //5km 내 음식점이 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 5 / 111.0;
                                            REFERLNG = 5 / 88.74;

                                            for (cafeSpot spot : lunchSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                        //5km 내 음식점이 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 8 / 111.0;
                                            REFERLNG = 8 / 88.74;

                                            for (cafeSpot spot : lunchSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }
                                }

                                for(cafeSpot nearspot : nearbySpots){
                                    String name = nearspot.getName();
                                    String time = nearspot.getTime();
                                    Log.d("Spot Info", "밥집 이름: " + name + ", 식사시간: " + time);
                                }

                                if (!nearbySpots.isEmpty()) {
                                    Random random = new Random();
                                    int randomIndex = random.nextInt(nearbySpots.size());
                                    cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                                    recommendedSpot recommended = new recommendedSpot(
                                            nearbySpot.getName(),
                                            nearbySpot.getType(),
                                            nearbySpot.getAddress(),
                                            nearbySpot.getImage(),
                                            nearbySpot.getLat(),
                                            nearbySpot.getLon()
                                    );

                                    if (!recommendedSpots.contains(recommended)) {
                                        recommendedSpots.add(recommended);
                                    } else {
                                        int attempts = 0;
                                        int maxAttempts = nearbySpots.size() * 2;

                                        while (attempts < maxAttempts) {
                                            randomIndex = random.nextInt(nearbySpots.size());
                                            nearbySpot = nearbySpots.get(randomIndex);

                                            recommended = new recommendedSpot(
                                                    nearbySpot.getName(),
                                                    nearbySpot.getType(),
                                                    nearbySpot.getAddress(),
                                                    nearbySpot.getImage(),
                                                    nearbySpot.getLat(),
                                                    nearbySpot.getLon()
                                            );

                                            if (!recommendedSpots.contains(recommended)) {
                                                recommendedSpots.add(recommended);
                                                break;
                                            }

                                            attempts++;
                                        }
                                    }
                                }


                            } else if(token.equals("3")){
                                Log.d("추천일정 번호", token);
                                String csvFilePath = "final.csv";
                                List<cafeSpot> nearbySpots = new ArrayList<>();
                                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "음식");
                                for(cafeSpot spot : spotList){
                                    String name = spot.getName();
                                    String time = spot.getTime();
                                    Log.d("음식점 선정", "밥집 이름: " + name + ", 식사시간: " + time);
                                }
                                List<cafeSpot> dinnerSpots = filterSpotListByTime(spotList, "저녁");
                                for(cafeSpot spot : dinnerSpots){
                                    String name = spot.getName();
                                    String time = spot.getTime();
                                    Log.d("저녁 식당 선정", "밥집 이름: " + name + ", 식사시간: " + time);
                                }

                                if(howChoice.equals("자차")){
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 2 / 111.0;
                                            double REFERLNG = 2 / 88.74;

                                            for (cafeSpot spot : dinnerSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //2km 내 음식점이 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 4 / 111.0;
                                                REFERLNG = 4 / 88.74;

                                                for (cafeSpot spot : dinnerSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //4km 내 음식점이 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 10 / 111.0;
                                                REFERLNG = 10 / 88.74;

                                                for (cafeSpot spot : dinnerSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 3 / 111.0;
                                            double REFERLNG = 3 / 88.74;

                                            for (cafeSpot spot : dinnerSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //3km 내 음식점 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 6 / 111.0;
                                                REFERLNG = 6 / 88.74;

                                                for (cafeSpot spot : dinnerSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //6km 내 음식점 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 12 / 111.0;
                                                REFERLNG = 12 / 88.74;

                                                for (cafeSpot spot : dinnerSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }

                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 5 / 111.0;
                                        double REFERLNG = 5 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : dinnerSpots) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //5km 내 음식점이 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 8 / 111.0;
                                            REFERLNG = 8 / 88.74;

                                            for (cafeSpot spot : dinnerSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                        //8km 내 음식점이 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 12 / 111.0;
                                            REFERLNG = 12 / 88.74;

                                            for (cafeSpot spot : dinnerSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }

                                } else if(howChoice.equals("대중교통")) {
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : dinnerSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 음식점이 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 2 / 111.0;
                                                REFERLNG = 2 / 88.74;

                                                for (cafeSpot spot : dinnerSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //2km 내 음식점이 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 7 / 111.0;
                                                REFERLNG = 7 / 88.74;

                                                for (cafeSpot spot : dinnerSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : dinnerSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 음식점이 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 2 / 111.0;
                                                REFERLNG = 2 / 88.74;

                                                for (cafeSpot spot : dinnerSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //2km 내 음식점이 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 5 / 111.0;
                                                REFERLNG = 5 / 88.74;

                                                for (cafeSpot spot : dinnerSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 3 / 111.0;
                                        double REFERLNG = 3 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : dinnerSpots) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //5km 내 음식점이 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 5 / 111.0;
                                            REFERLNG = 5 / 88.74;

                                            for (cafeSpot spot : dinnerSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                        //5km 내 음식점이 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 8 / 111.0;
                                            REFERLNG = 8 / 88.74;

                                            for (cafeSpot spot : dinnerSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }
                                }

                                for(cafeSpot nearspot : nearbySpots){
                                    String name = nearspot.getName();
                                    String time = nearspot.getTime();
                                    Log.d("Spot Info", "밥집 이름: " + name + ", 식사시간: " + time);
                                }

                                if (!nearbySpots.isEmpty()) {
                                    Random random = new Random();
                                    int randomIndex = random.nextInt(nearbySpots.size());
                                    cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                                    recommendedSpot recommended = new recommendedSpot(
                                            nearbySpot.getName(),
                                            nearbySpot.getType(),
                                            nearbySpot.getAddress(),
                                            nearbySpot.getImage(),
                                            nearbySpot.getLat(),
                                            nearbySpot.getLon()
                                    );

                                    if (!recommendedSpots.contains(recommended)) {
                                        recommendedSpots.add(recommended);
                                    } else {
                                        int attempts = 0;
                                        int maxAttempts = nearbySpots.size() * 2;

                                        while (attempts < maxAttempts) {
                                            randomIndex = random.nextInt(nearbySpots.size());
                                            nearbySpot = nearbySpots.get(randomIndex);

                                            recommended = new recommendedSpot(
                                                    nearbySpot.getName(),
                                                    nearbySpot.getType(),
                                                    nearbySpot.getAddress(),
                                                    nearbySpot.getImage(),
                                                    nearbySpot.getLat(),
                                                    nearbySpot.getLon()
                                            );

                                            if (!recommendedSpots.contains(recommended)) {
                                                recommendedSpots.add(recommended);
                                                break;
                                            }

                                            attempts++;
                                        }
                                    }
                                }


                            } else if(token.equals("4")) {
                                Log.d("추천일정 번호", token);
                                String csvFilePath = "final.csv";
                                List<cafeSpot> nearbySpots = new ArrayList<>();
                                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "브런치");

                                if(howChoice.equals("자차")){
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 2 / 111.0;
                                            double REFERLNG = 2 / 88.74;

                                            for (cafeSpot spot : spotList) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //2km 내 카페가 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 4 / 111.0;
                                                REFERLNG = 4 / 88.74;

                                                for (cafeSpot spot : spotList) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //4km 내 카페가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 8 / 111.0;
                                                REFERLNG = 8 / 88.74;

                                                for (cafeSpot spot : spotList) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : spotList) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 카페가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 3 / 111.0;
                                                REFERLNG = 3 / 88.74;

                                                for (cafeSpot spot : spotList) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }

                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 2 / 111.0;
                                        double REFERLNG = 2 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : spotList) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //2km 내 카페가 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 5 / 111.0;
                                            REFERLNG = 5 / 88.74;

                                            for (cafeSpot spot : spotList) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }

                                } else if(howChoice.equals("대중교통")) {
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : spotList) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 카페가 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 2 / 111.0;
                                                REFERLNG = 2 / 88.74;

                                                for (cafeSpot spot : spotList) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //2km 내 카페가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 5 / 111.0;
                                                REFERLNG = 5 / 88.74;

                                                for (cafeSpot spot : spotList) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : spotList) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 카페가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 5 / 111.0;
                                                REFERLNG = 5 / 88.74;

                                                for (cafeSpot spot : spotList) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 3 / 111.0;
                                        double REFERLNG = 3 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : spotList) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //5km 내 카페가 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 8 / 111.0;
                                            REFERLNG = 8 / 88.74;

                                            for (cafeSpot spot : spotList) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }
                                }

                                for(cafeSpot nearspot : nearbySpots){
                                    String name = nearspot.getName();
                                    String time = nearspot.getTime();
                                    Log.d("Spot Info", "밥집 이름: " + name + ", 식사시간: " + time);
                                }

                                if (!nearbySpots.isEmpty()) {
                                    Random random = new Random();
                                    int randomIndex = random.nextInt(nearbySpots.size());
                                    cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                                    recommendedSpot recommended = new recommendedSpot(
                                            nearbySpot.getName(),
                                            nearbySpot.getType(),
                                            nearbySpot.getAddress(),
                                            nearbySpot.getImage(),
                                            nearbySpot.getLat(),
                                            nearbySpot.getLon()
                                    );

                                    if (!recommendedSpots.contains(recommended)) {
                                        recommendedSpots.add(recommended);
                                    } else {
                                        int attempts = 0;
                                        int maxAttempts = nearbySpots.size() * 2;

                                        while (attempts < maxAttempts) {
                                            randomIndex = random.nextInt(nearbySpots.size());
                                            nearbySpot = nearbySpots.get(randomIndex);

                                            recommended = new recommendedSpot(
                                                    nearbySpot.getName(),
                                                    nearbySpot.getType(),
                                                    nearbySpot.getAddress(),
                                                    nearbySpot.getImage(),
                                                    nearbySpot.getLat(),
                                                    nearbySpot.getLon()
                                            );

                                            if (!recommendedSpots.contains(recommended)) {
                                                recommendedSpots.add(recommended);
                                                break;
                                            }

                                            attempts++;
                                        }
                                    }
                                }


                            } else if(token.equals("5")) {
                                Log.d("추천일정 번호", token);
                                String csvFilePath = "final.csv";
                                List<cafeSpot> nearbySpots = new ArrayList<>();
                                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "카페");

                                if(howChoice.equals("자차")){
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : spotList) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 카페가 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 3 / 111.0;
                                                REFERLNG = 3 / 88.74;

                                                for (cafeSpot spot : spotList) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //4km 내 카페가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 8 / 111.0;
                                                REFERLNG = 8 / 88.74;

                                                for (cafeSpot spot : spotList) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : spotList) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 카페가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 3 / 111.0;
                                                REFERLNG = 3 / 88.74;

                                                for (cafeSpot spot : spotList) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }

                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 1 / 111.0;
                                        double REFERLNG = 1 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : spotList) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //2km 내 카페가 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 5 / 111.0;
                                            REFERLNG = 5 / 88.74;

                                            for (cafeSpot spot : spotList) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }

                                } else if(howChoice.equals("대중교통")) {
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : spotList) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 카페가 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 2 / 111.0;
                                                REFERLNG = 2 / 88.74;

                                                for (cafeSpot spot : spotList) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //2km 내 카페가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 5 / 111.0;
                                                REFERLNG = 5 / 88.74;

                                                for (cafeSpot spot : spotList) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 1 / 111.0;
                                            double REFERLNG = 1 / 88.74;

                                            for (cafeSpot spot : spotList) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //1km 내 카페가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 5 / 111.0;
                                                REFERLNG = 5 / 88.74;

                                                for (cafeSpot spot : spotList) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 3 / 111.0;
                                        double REFERLNG = 3 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : spotList) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //5km 내 카페가 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 8 / 111.0;
                                            REFERLNG = 8 / 88.74;

                                            for (cafeSpot spot : spotList) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }
                                }

                                for(cafeSpot nearspot : nearbySpots){
                                    String name = nearspot.getName();
                                    String time = nearspot.getTime();
                                    Log.d("Spot Info", "카페 이름: " + name + ", 시간: " + time);
                                }

                                if (!nearbySpots.isEmpty()) {
                                    Random random = new Random();
                                    int randomIndex = random.nextInt(nearbySpots.size());
                                    cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                                    recommendedSpot recommended = new recommendedSpot(
                                            nearbySpot.getName(),
                                            nearbySpot.getType(),
                                            nearbySpot.getAddress(),
                                            nearbySpot.getImage(),
                                            nearbySpot.getLat(),
                                            nearbySpot.getLon()
                                    );

                                    if (!recommendedSpots.contains(recommended)) {
                                        recommendedSpots.add(recommended);
                                    } else {
                                        int attempts = 0;
                                        int maxAttempts = nearbySpots.size() * 2;

                                        while (attempts < maxAttempts) {
                                            randomIndex = random.nextInt(nearbySpots.size());
                                            nearbySpot = nearbySpots.get(randomIndex);

                                            recommended = new recommendedSpot(
                                                    nearbySpot.getName(),
                                                    nearbySpot.getType(),
                                                    nearbySpot.getAddress(),
                                                    nearbySpot.getImage(),
                                                    nearbySpot.getLat(),
                                                    nearbySpot.getLon()
                                            );

                                            if (!recommendedSpots.contains(recommended)) {
                                                recommendedSpots.add(recommended);
                                                break;
                                            }

                                            attempts++;
                                        }
                                    }
                                }


                            } else if(token.equals("6")) {
                                Log.d("추천일정 번호", token);
                                String csvFilePath = "final.csv";
                                List<cafeSpot> nearbySpots = new ArrayList<>();
                                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "여행지");
                                List<cafeSpot> sunnySpots = filterSpotListByTime(spotList, "낮");

                                if(howChoice.equals("자차")){
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 3 / 111.0;
                                            double REFERLNG = 3 / 88.74;

                                            for (cafeSpot spot : sunnySpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //3km 내 여행지가 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 8 / 111.0;
                                                REFERLNG = 8 / 88.74;

                                                for (cafeSpot spot : sunnySpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //8km 내 여행지가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 15 / 111.0;
                                                REFERLNG = 15 / 88.74;

                                                for (cafeSpot spot : sunnySpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 3 / 111.0;
                                            double REFERLNG = 3 / 88.74;

                                            for (cafeSpot spot : sunnySpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //3km 내 여행지가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 10 / 111.0;
                                                REFERLNG = 10 / 88.74;

                                                for (cafeSpot spot : sunnySpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }

                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 5 / 111.0;
                                        double REFERLNG = 5 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : sunnySpots) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //5km 내 여행지가 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 12 / 111.0;
                                            REFERLNG = 12 / 88.74;

                                            for (cafeSpot spot : sunnySpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }

                                } else if(howChoice.equals("대중교통")) {
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 3 / 111.0;
                                            double REFERLNG = 3 / 88.74;

                                            for (cafeSpot spot : sunnySpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //3km 내 여행지가 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 7 / 111.0;
                                                REFERLNG = 7 / 88.74;

                                                for (cafeSpot spot : sunnySpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //7km 내 여행지가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 12 / 111.0;
                                                REFERLNG = 12 / 88.74;

                                                for (cafeSpot spot : sunnySpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 3 / 111.0;
                                            double REFERLNG = 3 / 88.74;

                                            for (cafeSpot spot : sunnySpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //3km 내 여행지가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 6 / 111.0;
                                                REFERLNG = 6 / 88.74;

                                                for (cafeSpot spot : sunnySpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //6km 내 여행지가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 12 / 111.0;
                                                REFERLNG = 12 / 88.74;

                                                for (cafeSpot spot : sunnySpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 3 / 111.0;
                                        double REFERLNG = 3 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : sunnySpots) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //3km 내 여행지가 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 5 / 111.0;
                                            REFERLNG = 5 / 88.74;

                                            for (cafeSpot spot : sunnySpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                        //5km 내 여행지가 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 7 / 111.0;
                                            REFERLNG = 7 / 88.74;

                                            for (cafeSpot spot : sunnySpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                        //7km 내 여행지가 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 10 / 111.0;
                                            REFERLNG = 10 / 88.74;

                                            for (cafeSpot spot : sunnySpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }
                                }

                                for(cafeSpot nearspot : nearbySpots){
                                    String name = nearspot.getName();
                                    String time = nearspot.getTime();
                                    Log.d("Spot Info", "장소 이름: " + name + ", 주야: " + time);
                                }

                                if (!nearbySpots.isEmpty()) {
                                    Random random = new Random();
                                    int randomIndex = random.nextInt(nearbySpots.size());
                                    cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                                    recommendedSpot recommended = new recommendedSpot(
                                            nearbySpot.getName(),
                                            nearbySpot.getType(),
                                            nearbySpot.getAddress(),
                                            nearbySpot.getImage(),
                                            nearbySpot.getLat(),
                                            nearbySpot.getLon()
                                    );

                                    // 같은 이름을 가진 추천 데이터가 이미 있는지 확인
                                    boolean hasDuplicateName = false;
                                    for (recommendedSpot existingRecommended : recommendedSpots) {
                                        if (existingRecommended.getName().equals(recommended.getName())) {
                                            hasDuplicateName = true;
                                            break;
                                        }
                                    }

                                    if (!hasDuplicateName) {
                                        recommendedSpots.add(recommended);
                                    } else {
                                        // 유일한 이름을 가진 추천 데이터를 찾아 추가하려 시도
                                        int attempts = 0;
                                        int maxAttempts = nearbySpots.size() * 2;

                                        while (attempts < maxAttempts) {
                                            randomIndex = random.nextInt(nearbySpots.size());
                                            nearbySpot = nearbySpots.get(randomIndex);

                                            recommended = new recommendedSpot(
                                                    nearbySpot.getName(),
                                                    nearbySpot.getType(),
                                                    nearbySpot.getAddress(),
                                                    nearbySpot.getImage(),
                                                    nearbySpot.getLat(),
                                                    nearbySpot.getLon()
                                            );

                                            boolean hasUniqueName = true;
                                            for (recommendedSpot existingRecommended : recommendedSpots) {
                                                if (existingRecommended.getName().equals(recommended.getName())) {
                                                    hasUniqueName = false;
                                                    break;
                                                }
                                            }

                                            if (hasUniqueName) {
                                                recommendedSpots.add(recommended);
                                                break;
                                            }

                                            attempts++;
                                        }
                                    }
                                }



                            } else {
                                Log.d("추천일정 번호", token);
                                String csvFilePath = "final.csv";
                                List<cafeSpot> nearbySpots = new ArrayList<>();
                                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "여행지");
                                List<cafeSpot> nightSpots = filterSpotListByTime(spotList, "밤");

                                if(howChoice.equals("자차")){
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 3 / 111.0;
                                            double REFERLNG = 3 / 88.74;

                                            for (cafeSpot spot : nightSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //3km 내 여행지가 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 8 / 111.0;
                                                REFERLNG = 8 / 88.74;

                                                for (cafeSpot spot : nightSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //8km 내 여행지가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 15 / 111.0;
                                                REFERLNG = 15 / 88.74;

                                                for (cafeSpot spot : nightSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 3 / 111.0;
                                            double REFERLNG = 3 / 88.74;

                                            for (cafeSpot spot : nightSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //3km 내 여행지가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 10 / 111.0;
                                                REFERLNG = 10 / 88.74;

                                                for (cafeSpot spot : nightSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }

                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 5 / 111.0;
                                        double REFERLNG = 5 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : nightSpots) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //5km 내 여행지가 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 12 / 111.0;
                                            REFERLNG = 12 / 88.74;

                                            for (cafeSpot spot : nightSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }

                                } else if(howChoice.equals("대중교통")) {
                                    if (!recommendedSpots.isEmpty()) {
                                        // recommendedSpots 리스트가 비어있지 않은 경우
                                        boolean hasRecommendedTravelSpot = false;
                                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                                            if (recommendedSpot.getType().equals("여행지")) {
                                                hasRecommendedTravelSpot = true;
                                                break;
                                            }
                                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                                        boolean spotsAdded = false;

                                        if(hasRecommendedTravelSpot){
                                            double REFERLAT = 3 / 111.0;
                                            double REFERLNG = 3 / 88.74;

                                            for (cafeSpot spot : nightSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //3km 내 여행지가 없을 때
                                            if (!spotsAdded){
                                                REFERLAT = 7 / 111.0;
                                                REFERLNG = 7 / 88.74;

                                                for (cafeSpot spot : nightSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //7km 내 여행지가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 12 / 111.0;
                                                REFERLNG = 12 / 88.74;

                                                for (cafeSpot spot : nightSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            double REFERLAT = 3 / 111.0;
                                            double REFERLNG = 3 / 88.74;

                                            for (cafeSpot spot : nightSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                            //3km 내 여행지가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 6 / 111.0;
                                                REFERLNG = 6 / 88.74;

                                                for (cafeSpot spot : nightSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                            //6km 내 여행지가 없을 때
                                            if (!spotsAdded) {
                                                REFERLAT = 12 / 111.0;
                                                REFERLNG = 12 / 88.74;

                                                for (cafeSpot spot : nightSpots) {
                                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                        nearbySpots.add(spot);
                                                        spotsAdded = true;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // recommendedSpots 리스트가 비어있는 경우

                                        double REFERLAT = 3 / 111.0;
                                        double REFERLNG = 3 / 88.74;
                                        boolean spotsAdded = false;

                                        for (cafeSpot spot : nightSpots) {
                                            double spotLatValue = Double.parseDouble(spot.getLat());
                                            double spotLonValue = Double.parseDouble(spot.getLon());
                                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                nearbySpots.add(spot);
                                                spotsAdded = true;
                                            }
                                        }
                                        //3km 내 여행지가 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 5 / 111.0;
                                            REFERLNG = 5 / 88.74;

                                            for (cafeSpot spot : nightSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                        //5km 내 여행지가 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 7 / 111.0;
                                            REFERLNG = 7 / 88.74;

                                            for (cafeSpot spot : nightSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                        //7km 내 여행지가 없을 때
                                        if (!spotsAdded) {
                                            REFERLAT = 10 / 111.0;
                                            REFERLNG = 10 / 88.74;

                                            for (cafeSpot spot : nightSpots) {
                                                double spotLatValue = Double.parseDouble(spot.getLat());
                                                double spotLonValue = Double.parseDouble(spot.getLon());
                                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                                    nearbySpots.add(spot);
                                                    spotsAdded = true;
                                                }
                                            }
                                        }
                                    }
                                }

                                for(cafeSpot nearspot : nearbySpots){
                                    String name = nearspot.getName();
                                    String time = nearspot.getTime();
                                    Log.d("Spot Info", "장소 이름: " + name + ", 주야: " + time);
                                }

                                if (!nearbySpots.isEmpty()) {
                                    Random random = new Random();
                                    int randomIndex = random.nextInt(nearbySpots.size());
                                    cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                                    recommendedSpot recommended = new recommendedSpot(
                                            nearbySpot.getName(),
                                            nearbySpot.getType(),
                                            nearbySpot.getAddress(),
                                            nearbySpot.getImage(),
                                            nearbySpot.getLat(),
                                            nearbySpot.getLon()
                                    );

                                    if (!recommendedSpots.contains(recommended)) {
                                        recommendedSpots.add(recommended);
                                    } else {
                                        int attempts = 0;
                                        int maxAttempts = nearbySpots.size() * 2;

                                        while (attempts < maxAttempts) {
                                            randomIndex = random.nextInt(nearbySpots.size());
                                            nearbySpot = nearbySpots.get(randomIndex);

                                            recommended = new recommendedSpot(
                                                    nearbySpot.getName(),
                                                    nearbySpot.getType(),
                                                    nearbySpot.getAddress(),
                                                    nearbySpot.getImage(),
                                                    nearbySpot.getLat(),
                                                    nearbySpot.getLon()
                                            );

                                            if (!recommendedSpots.contains(recommended)) {
                                                recommendedSpots.add(recommended);
                                                break;
                                            }

                                            attempts++;
                                        }
                                    }
                                }
                            }
                        }

                        for (recommendedSpot recommended : recommendedSpots) {
                            db.recommendedSpotDao().insert(recommended);
                            Log.d("RecommendedSpot", "Name: " + recommended.getName());
                        }
                        Toast.makeText(getApplicationContext(),"여행 일정을 추천중입니다", Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finalrecommend.clear();
                                finalrecommend.addAll(db.recommendedSpotDao().findAll());
                                recommendedAdapter.notifyDataSetChanged();
                                commend_add.setVisibility(View.VISIBLE);
                                for (recommendedSpot spot2 : finalrecommend) {
                                    Log.d("최종 결과", "Name: " + spot2.getName());
                                }
                            }
                        }, 3000); // 3000 milliseconds = 3 seconds

                        break;
                    case "~8시간":
                        for (recommendedSpot recommended : startRecommending(recommendedSpots, currentTime, timeChoice, whoChoice, whereChoice, howChoice, currentLatLng)) {
                        db.recommendedSpotDao().insert(recommended);
                        Log.d("RecommendedSpot", "Name: " + recommended.getName());
                        }
                        Toast.makeText(getApplicationContext(),"여행 일정을 추천중입니다", Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finalrecommend.clear();
                                finalrecommend.addAll(db.recommendedSpotDao().findAll());
                                recommendedAdapter.notifyDataSetChanged();
                                commend_add.setVisibility(View.VISIBLE);
                                for (recommendedSpot spot2 : finalrecommend) {
                                    Log.d("최종 결과", "Name: " + spot2.getName());
                                }
                            }
                        }, 3000); // 3000 milliseconds = 3 seconds
                        break;
                    case "~12시간":
                        for (recommendedSpot recommended : startRecommending(recommendedSpots, currentTime, timeChoice, whoChoice, whereChoice, howChoice, currentLatLng)) {
                        db.recommendedSpotDao().insert(recommended);
                        Log.d("RecommendedSpot", "Name: " + recommended.getName());
                        }
                        Toast.makeText(getApplicationContext(),"여행 일정을 추천중입니다", Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finalrecommend.clear();
                                finalrecommend.addAll(db.recommendedSpotDao().findAll());
                                recommendedAdapter.notifyDataSetChanged();
                                commend_add.setVisibility(View.VISIBLE);
                                for (recommendedSpot spot2 : finalrecommend) {
                                    Log.d("최종 결과", "Name: " + spot2.getName());
                                }
                            }
                        }, 3000); // 3000 milliseconds = 3 seconds
                        break;
                    case "하루종일":
                        for (recommendedSpot recommended : startRecommending(recommendedSpots, currentTime, timeChoice, whoChoice, whereChoice, howChoice, currentLatLng)) {
                            db.recommendedSpotDao().insert(recommended);
                            Log.d("RecommendedSpot", "Name: " + recommended.getName());
                        }
                        Toast.makeText(getApplicationContext(),"여행 일정을 추천중입니다", Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finalrecommend.clear();
                                finalrecommend.addAll(db.recommendedSpotDao().findAll());
                                recommendedAdapter.notifyDataSetChanged();
                                commend_add.setVisibility(View.VISIBLE);
                                for (recommendedSpot spot2 : finalrecommend) {
                                    Log.d("최종 결과", "Name: " + spot2.getName());
                                }
                            }
                        }, 3000); // 3000 milliseconds = 3 seconds
                        break;

                }
            }
        });
        recommendedAdapter.notifyDataSetChanged();

        commend_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog dl = new Dialog(commendActivity.this);
                dl.setContentView(R.layout.recommend_create);

                EditText editText = dl.findViewById(R.id.edit_reconame);

                LinearLayout layout = (LinearLayout) dl.findViewById(R.id.dynamic_space);
                LinearLayout layout_below = (LinearLayout) dl.findViewById(R.id.btn_space);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, // width
                        LinearLayout.LayoutParams.WRAP_CONTENT // height
                );
                params.weight = 1f;
                Button addbtn = new Button(commendActivity.this);
                addbtn.setText("일정 추가");
                addbtn.setLayoutParams(params);
                layout_below.addView(addbtn);
                dl.show();

                addbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String inputname = editText.getText().toString();
                        if(!inputname.equals("")){
                            if(db.table1().findAll().toString() == "[]"){
                                Log.d("TravelSpot 테이블 비었나?", "ㅇㅇ");
                                List<recommendedSpot> recommendedSpots = db.recommendedSpotDao().findAll();

                                Data data = new Data(inputname, "spot0");
                                db.dataDao().insert(data);
                                for(recommendedSpot spot : recommendedSpots) {
                                    TravelSpot travelSpot = new TravelSpot(spot.getName(), spot.getAddress(), spot.getLat(), spot.getLon());
                                    db.table1().insert(travelSpot);
                                }
                                Toast.makeText(getApplicationContext(),"일정 관리에 추가되었습니다", Toast.LENGTH_LONG).show();
                                dl.dismiss();
                            } else if(db.table2().findAll().toString() == "[]"){
                                Log.d("TravelSpot1 테이블 비었나?", "ㅇㅇ");
                                List<recommendedSpot> recommendedSpots = db.recommendedSpotDao().findAll();

                                Data data = new Data(inputname, "spot1");
                                db.dataDao().insert(data);
                                for(recommendedSpot spot : recommendedSpots) {
                                    TravelSpot1 travelSpot1 = new TravelSpot1(spot.getName(), spot.getAddress(), spot.getLat(), spot.getLon());
                                    db.table2().insert1(travelSpot1);
                                }
                                Toast.makeText(getApplicationContext(),"일정 관리에 추가되었습니다", Toast.LENGTH_LONG).show();
                                dl.dismiss();
                            } else if(db.table3().findAll().toString() == "[]"){
                                Log.d("TravelSpot2 테이블 비었나?", "ㅇㅇ");
                                List<recommendedSpot> recommendedSpots = db.recommendedSpotDao().findAll();

                                Data data = new Data(inputname, "spot2");
                                db.dataDao().insert(data);
                                for(recommendedSpot spot : recommendedSpots) {
                                    TravelSpot2 travelSpot2 = new TravelSpot2(spot.getName(), spot.getAddress(), spot.getLat(), spot.getLon());
                                    db.table3().insert1(travelSpot2);
                                }
                                Toast.makeText(getApplicationContext(),"일정 관리에 추가되었습니다", Toast.LENGTH_LONG).show();
                                dl.dismiss();
                            } else if(db.table4().findAll().toString() == "[]"){
                                Log.d("TravelSpot3 테이블 비었나?", "ㅇㅇ");
                                List<recommendedSpot> recommendedSpots = db.recommendedSpotDao().findAll();

                                Data data = new Data(inputname, "spot3");
                                db.dataDao().insert(data);
                                for(recommendedSpot spot : recommendedSpots) {
                                    TravelSpot3 travelSpot3 = new TravelSpot3(spot.getName(), spot.getAddress(), spot.getLat(), spot.getLon());
                                    db.table4().insert1(travelSpot3);
                                }
                                Toast.makeText(getApplicationContext(),"일정 관리에 추가되었습니다", Toast.LENGTH_LONG).show();
                                dl.dismiss();
                            } else if(db.table5().findAll().toString() == "[]"){
                                Log.d("TravelSpot4 테이블 비었나?", "ㅇㅇ");
                                List<recommendedSpot> recommendedSpots = db.recommendedSpotDao().findAll();

                                Data data = new Data(inputname, "spot4");
                                db.dataDao().insert(data);
                                for(recommendedSpot spot : recommendedSpots) {
                                    TravelSpot4 travelSpot4 = new TravelSpot4(spot.getName(), spot.getAddress(), spot.getLat(), spot.getLon());
                                    db.table5().insert1(travelSpot4);
                                }
                                Toast.makeText(getApplicationContext(),"일정 관리에 추가되었습니다", Toast.LENGTH_LONG).show();
                                dl.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(),"더이상 일정을 추가할 수 없습니다", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),"일정의 이름을 입력하세요", Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }
        });
    }

    // 검색할 반경 지정
    public boolean withinSightMarker(double REFERLAT, double REFERLNG, LatLng currentPosition, LatLng markerPosition) {
        boolean withinSiMaLat = Math.abs(currentPosition.latitude - markerPosition.latitude) <= REFERLAT;
        boolean withinSiMaLng = Math.abs(currentPosition.longitude - markerPosition.longitude) <= REFERLNG;
        return withinSiMaLat && withinSiMaLng;
    }


    // 선택된 라디오 버튼의 ID에 따라 선택 정보 반환
    private String getRadioButtonChoice(int radioButtonId) {
        String choice = "";
        switch (radioButtonId) {
            case R.id.couple_btn:
                choice = "연인과";
                break;
            case R.id.family_btn:
                choice = "가족과";
                break;
            case R.id.friends_btn:
                choice = "친구와";
                break;
            case R.id.healing_btn:
                choice = "힐링";
                break;
            case R.id.drive_btn:
                choice = "드라이브";
                break;
            case R.id.foodfighter_btn:
                choice = "식도락";
                break;
            case R.id.car_btn:
                choice = "자차";
                break;
            case R.id.bus_btn:
                choice = "대중교통";
                break;
            case R.id.in_4:
                choice = "~4시간";
                break;
            case R.id.in_8:
                choice = "~8시간";
                break;
            case R.id.in_12:
                choice = "~12시간";
                break;
            case R.id.in_24:
                choice = "하루종일";
                break;
        }
        return choice;
    }

    // 현재 시간 정보 반환
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        Calendar calendar = Calendar.getInstance();
        return sdf.format(calendar.getTime());
    }

    private String RouteCommend(String nowhour, String traveltime){
        String pattern = "";
        switch (traveltime){
            case "~4시간":
                if(nowhour.equals("21") || nowhour.equals("22") || nowhour.equals("23") || nowhour.equals("24") || nowhour.equals("01") || nowhour.equals("02")
                        || nowhour.equals("03") || nowhour.equals("04") || nowhour.equals("05") || nowhour.equals("06")){
                    pattern = "1-5-6";
                } else if (nowhour.equals("07")){
                    pattern = "1-5-6";
                } else if (nowhour.equals("08")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "1-6-6";
                    } else {
                        pattern = "6-4";
                    }
                } else if (nowhour.equals("09")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "6-2";
                    } else {
                        pattern = "6-4-5";
                    }
                } else if (nowhour.equals("10")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "4-6";
                    } else {
                        pattern = "6-2-6";
                    }
                } else if (nowhour.equals("11")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "2-6-6";
                    } else {
                        pattern = "2-5-6";
                    }
                } else if (nowhour.equals("12")){
                    pattern = "2-6-5";
                } else if (nowhour.equals("13")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "5-6-6";
                    } else {
                        pattern = "6-5-6";
                    }
                } else if (nowhour.equals("14")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "5-6-6";
                    } else {
                        pattern = "6-5-6";
                    }
                } else if (nowhour.equals("15")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "5-6-3";
                    } else {
                        pattern = "6-6-3";
                    }
                } else if (nowhour.equals("16")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "6-3-5";
                    } else {
                        pattern = "6-3-7";
                    }
                } else if (nowhour.equals("17")){
                    Random random = new Random();
                    int randomNum = random.nextInt(3); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "6-3-5";
                    } else if(randomNum == 1) {
                        pattern = "6-5-7";
                    } else {
                        pattern = "3-7-5";
                    }
                } else if (nowhour.equals("18")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "3-5-7";
                    } else {
                        pattern = "3-7-7";
                    }
                } else if (nowhour.equals("19")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "3-5";
                    } else {
                        pattern = "7-5";
                    }
                } else if (nowhour.equals("20")){
                    pattern = "7-5";
                }
                break;
            case "~8시간":
                if(nowhour.equals("21") || nowhour.equals("22") || nowhour.equals("23") || nowhour.equals("24") || nowhour.equals("01") || nowhour.equals("02")
                        || nowhour.equals("03") || nowhour.equals("04") || nowhour.equals("05") || nowhour.equals("06")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "1-5-6-2-6";
                    } else {
                        pattern = "4-6-5-6-3";
                    }
                } else if (nowhour.equals("07")){
                    Random random = new Random();
                    int randomNum = random.nextInt(3); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "1-5-6-2-6";
                    } else if(randomNum == 1){
                        pattern = "1-6-2-6-5";
                    } else {
                        pattern = "6-4-6-5";
                    }
                } else if (nowhour.equals("08")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "1-6-2-5-6";
                    } else {
                        pattern = "6-4-5-6";
                    }
                } else if (nowhour.equals("09")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "4-6-5-6-3";
                    } else {
                        pattern = "6-6-2-5-6";
                    }
                } else if (nowhour.equals("10")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "4-5-6-6-3";
                    } else {
                        pattern = "4-6-5-6-3";
                    }
                } else if (nowhour.equals("11")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "2-5-6-6-3";
                    } else {
                        pattern = "2-6-6-6-3-5";
                    }
                } else if (nowhour.equals("12")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "2-5-6-6-3-5";
                    } else {
                        pattern = "4-6-5-6-3-7";
                    }
                } else if (nowhour.equals("13")){
                    Random random = new Random();
                    int randomNum = random.nextInt(3); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "2-5-6-7-3-7";
                    } else if(randomNum == 1){
                        pattern = "6-6-6-3-5";
                    } else {
                        pattern = "5-6-6-3-7";
                    }
                } else if (nowhour.equals("14")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "6-5-6-3-7-5";
                    } else {
                        pattern = "5-6-6-3-5-7";
                    }
                } else if (nowhour.equals("15")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "6-6-3-5-7";
                    } else {
                        pattern = "5-6-3-7-5";
                    }
                } else if (nowhour.equals("16")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "6-3-5-7";
                    } else {
                        pattern = "7-3-7-5";
                    }
                } else if (nowhour.equals("17")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "3-5-7";
                    } else {
                        pattern = "7-3-5";
                    }
                } else if (nowhour.equals("18")){
                    pattern = "3-7-5";
                } else if(nowhour.equals("19")){
                    Random random = new Random();
                    int randomNum = random.nextInt(3); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "3-7-5";
                    } else if(randomNum == 1){
                        pattern = "5-7";
                    } else {
                        pattern = "7-5";
                    }
                } else {
                    pattern = "7-5";
                }
                break;
            case "~12시간":
                if(nowhour.equals("21") || nowhour.equals("22") || nowhour.equals("23") || nowhour.equals("24") || nowhour.equals("01") || nowhour.equals("02")
                        || nowhour.equals("03") || nowhour.equals("04") || nowhour.equals("05") || nowhour.equals("06")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "1-6-2-5-6-3";
                    } else {
                        pattern = "4-6-6-5-6-3-5";
                    }
                } else if (nowhour.equals("07")){
                    Random random = new Random();
                    int randomNum = random.nextInt(3); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "1-6-2-5-6-3";
                    } else if(randomNum == 1){
                        pattern = "1-5-6-2-6-7-3";
                    } else {
                        pattern = "6-4-5-6-7-3";
                    }
                } else if (nowhour.equals("08")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "1-6-2-5-6-3-7";
                    } else {
                        pattern = "6-4-6-6-7-3-5";
                    }
                } else if (nowhour.equals("09")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "4-6-6-5-6-3-7";
                    } else {
                        pattern = "6-2-5-6-3-7-5";
                    }
                } else if (nowhour.equals("10")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "4-6-5-6-7-3-7";
                    } else {
                        pattern = "4-6-6-5-3-7-5";
                    }
                } else if (nowhour.equals("11")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "2-6-5-6-3-7-5";
                    } else {
                        pattern = "2-5-6-6-3-7-5";
                    }
                } else if (nowhour.equals("12")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "4-5-6-6-3-5-7";
                    } else {
                        pattern = "2-6-5-6-3-7-5";
                    }
                } else if (nowhour.equals("13")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "2-5-6-6-3-5-7";
                    } else {
                        pattern = "2-6-5-6-3-7-5";
                    }
                } else if (nowhour.equals("14")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "5-6-6-3-7-5";
                    } else {
                        pattern = "6-6-5-7-3-7";
                    }
                } else if (nowhour.equals("15")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "5-6-3-7-5";
                    } else {
                        pattern = "6-7-3-7-5";
                    }
                } else if (nowhour.equals("16")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "6-3-5-7";
                    } else {
                        pattern = "6-3-7-5";
                    }
                } else if (nowhour.equals("17")){
                    Random random = new Random();
                    int randomNum = random.nextInt(2); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "3-7-5";
                    } else {
                        pattern = "7-3-5";
                    }
                } else if (nowhour.equals("18")){
                    pattern = "3-7-5";
                } else if(nowhour.equals("19")){
                    Random random = new Random();
                    int randomNum = random.nextInt(3); // 0 또는 1 중 랜덤으로 선택
                    if (randomNum == 0) {
                        pattern = "3-7-5";
                    } else if(randomNum == 1){
                        pattern = "5-7";
                    } else {
                        pattern = "7-5";
                    }
                } else {
                    pattern = "7-5";
                }
                break;
            case "하루종일":
                pattern = "4-6-2-6-5-7-3-7-5";
                break;
        }
        return pattern;
    }

    private List<recommendedSpot> startRecommending(List<recommendedSpot> recommendedSpots, String currentTime, String timeChoice, String whoChoice, String whereChoice, String howChoice, LatLng currentLatLng){
        String routePattern2 = RouteCommend(currentTime, timeChoice);
        Log.d("추천 일정 패턴", routePattern2);
        StringTokenizer stRoute2 = new StringTokenizer(routePattern2, "-");
        while(stRoute2.hasMoreTokens()) {
            String token = stRoute2.nextToken();
            if(token.equals("1")){
                Log.d("추천일정 번호", token);
                String csvFilePath = "final.csv";
                List<cafeSpot> nearbySpots = new ArrayList<>();
                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "음식");
                List<cafeSpot> morningSpots = filterSpotListByTime(spotList, "아침");


                if(howChoice.equals("자차")){
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 2 / 111.0;
                            double REFERLNG = 2 / 88.74;

                            for (cafeSpot spot : morningSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //2km 내 음식점이 없을 때
                            if (!spotsAdded){
                                REFERLAT = 5 / 111.0;
                                REFERLNG = 5 / 88.74;

                                for (cafeSpot spot : morningSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //5km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 10 / 111.0;
                                REFERLNG = 10 / 88.74;

                                for (cafeSpot spot : morningSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        } else {
                            double REFERLAT = 3 / 111.0;
                            double REFERLNG = 3 / 88.74;

                            for (cafeSpot spot : morningSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //3km 내 음식점 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 8 / 111.0;
                                REFERLNG = 8 / 88.74;

                                for (cafeSpot spot : morningSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //8km 내 음식점 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 12 / 111.0;
                                REFERLNG = 12 / 88.74;

                                for (cafeSpot spot : morningSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }

                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 5 / 111.0;
                        double REFERLNG = 5 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : morningSpots) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                nearbySpots.add(spot);
                                spotsAdded = true;
                            }
                        }
                        //5km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 10 / 111.0;
                            REFERLNG = 10 / 88.74;

                            for (cafeSpot spot : morningSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                        //10km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 15 / 111.0;
                            REFERLNG = 15 / 88.74;

                            for (cafeSpot spot : morningSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                    }

                } else if(howChoice.equals("대중교통")) {
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : morningSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 음식점이 없을 때
                            if (!spotsAdded){
                                REFERLAT = 2 / 111.0;
                                REFERLNG = 2 / 88.74;

                                for (cafeSpot spot : morningSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //2km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 7 / 111.0;
                                REFERLNG = 7 / 88.74;

                                for (cafeSpot spot : morningSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        } else {
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : morningSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : morningSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //3km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 7 / 111.0;
                                REFERLNG = 7 / 88.74;

                                for (cafeSpot spot : morningSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }
                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 3 / 111.0;
                        double REFERLNG = 3 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : morningSpots) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                nearbySpots.add(spot);
                                spotsAdded = true;
                            }
                        }
                        //3km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 5 / 111.0;
                            REFERLNG = 5 / 88.74;

                            for (cafeSpot spot : morningSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                        //5km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 8 / 111.0;
                            REFERLNG = 8 / 88.74;

                            for (cafeSpot spot : morningSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                    }
                }

                for(cafeSpot nearspot : nearbySpots){
                    String name = nearspot.getName();
                    String time = nearspot.getTime();
                    Log.d("Spot Info", "밥집 이름: " + name + ", 식사시간: " + time);
                }

                if (!nearbySpots.isEmpty()) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(nearbySpots.size());
                    cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                    recommendedSpot recommended = new recommendedSpot(
                            nearbySpot.getName(),
                            nearbySpot.getType(),
                            nearbySpot.getAddress(),
                            nearbySpot.getImage(),
                            nearbySpot.getLat(),
                            nearbySpot.getLon()
                    );

                    if (!recommendedSpots.contains(recommended)) {
                        recommendedSpots.add(recommended);
                    } else {
                        int attempts = 0;
                        int maxAttempts = nearbySpots.size() * 2;

                        while (attempts < maxAttempts) {
                            randomIndex = random.nextInt(nearbySpots.size());
                            nearbySpot = nearbySpots.get(randomIndex);

                            recommended = new recommendedSpot(
                                    nearbySpot.getName(),
                                    nearbySpot.getType(),
                                    nearbySpot.getAddress(),
                                    nearbySpot.getImage(),
                                    nearbySpot.getLat(),
                                    nearbySpot.getLon()
                            );

                            if (!recommendedSpots.contains(recommended)) {
                                recommendedSpots.add(recommended);
                                break;
                            }

                            attempts++;
                        }
                    }
                }

            } else if(token.equals("2")){
                Log.d("추천일정 번호", token);
                String csvFilePath = "final.csv";
                List<cafeSpot> nearbySpots = new ArrayList<>();
                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "음식");
                List<cafeSpot> lunchSpots = filterSpotListByTime(spotList, "점심");

                if(howChoice.equals("자차")){
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : lunchSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 음식점이 없을 때
                            if (!spotsAdded){
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : lunchSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //3km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 10 / 111.0;
                                REFERLNG = 10 / 88.74;

                                for (cafeSpot spot : lunchSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        } else {
                            double REFERLAT = 2 / 111.0;
                            double REFERLNG = 2 / 88.74;

                            for (cafeSpot spot : lunchSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //2km 내 음식점 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 5 / 111.0;
                                REFERLNG = 5 / 88.74;

                                for (cafeSpot spot : lunchSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //5km 내 음식점 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 12 / 111.0;
                                REFERLNG = 12 / 88.74;

                                for (cafeSpot spot : lunchSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }

                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 8 / 111.0;
                        double REFERLNG = 8 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : lunchSpots) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                nearbySpots.add(spot);
                                spotsAdded = true;
                            }
                        }
                        //10km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 10 / 111.0;
                            REFERLNG = 10 / 88.74;

                            for (cafeSpot spot : lunchSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                        //10km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 12 / 111.0;
                            REFERLNG = 12 / 88.74;

                            for (cafeSpot spot : lunchSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                    }

                } else if(howChoice.equals("대중교통")) {
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : lunchSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 음식점이 없을 때
                            if (!spotsAdded){
                                REFERLAT = 2 / 111.0;
                                REFERLNG = 2 / 88.74;

                                for (cafeSpot spot : lunchSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //2km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 7 / 111.0;
                                REFERLNG = 7 / 88.74;

                                for (cafeSpot spot : lunchSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        } else {
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : lunchSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : lunchSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //3km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 7 / 111.0;
                                REFERLNG = 7 / 88.74;

                                for (cafeSpot spot : lunchSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }
                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 2 / 111.0;
                        double REFERLNG = 2 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : lunchSpots) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                nearbySpots.add(spot);
                                spotsAdded = true;
                            }
                        }
                        //2km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 5 / 111.0;
                            REFERLNG = 5 / 88.74;

                            for (cafeSpot spot : lunchSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                        //5km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 8 / 111.0;
                            REFERLNG = 8 / 88.74;

                            for (cafeSpot spot : lunchSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                    }
                }

                for(cafeSpot nearspot : nearbySpots){
                    String name = nearspot.getName();
                    String time = nearspot.getTime();
                    Log.d("Spot Info", "밥집 이름: " + name + ", 식사시간: " + time);
                }

                if (!nearbySpots.isEmpty()) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(nearbySpots.size());
                    cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                    recommendedSpot recommended = new recommendedSpot(
                            nearbySpot.getName(),
                            nearbySpot.getType(),
                            nearbySpot.getAddress(),
                            nearbySpot.getImage(),
                            nearbySpot.getLat(),
                            nearbySpot.getLon()
                    );

                    if (!recommendedSpots.contains(recommended)) {
                        recommendedSpots.add(recommended);
                    } else {
                        int attempts = 0;
                        int maxAttempts = nearbySpots.size() * 2;

                        while (attempts < maxAttempts) {
                            randomIndex = random.nextInt(nearbySpots.size());
                            nearbySpot = nearbySpots.get(randomIndex);

                            recommended = new recommendedSpot(
                                    nearbySpot.getName(),
                                    nearbySpot.getType(),
                                    nearbySpot.getAddress(),
                                    nearbySpot.getImage(),
                                    nearbySpot.getLat(),
                                    nearbySpot.getLon()
                            );

                            if (!recommendedSpots.contains(recommended)) {
                                recommendedSpots.add(recommended);
                                break;
                            }

                            attempts++;
                        }
                    }
                }


            } else if(token.equals("3")){
                Log.d("추천일정 번호", token);
                String csvFilePath = "final.csv";
                List<cafeSpot> nearbySpots = new ArrayList<>();
                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "음식");
                for(cafeSpot spot : spotList){
                    String name = spot.getName();
                    String time = spot.getTime();
                    Log.d("음식점 선정", "밥집 이름: " + name + ", 식사시간: " + time);
                }
                List<cafeSpot> dinnerSpots = filterSpotListByTime(spotList, "저녁");
                for(cafeSpot spot : dinnerSpots){
                    String name = spot.getName();
                    String time = spot.getTime();
                    Log.d("저녁 식당 선정", "밥집 이름: " + name + ", 식사시간: " + time);
                }

                if(howChoice.equals("자차")){
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 음식점이 없을 때
                            if (!spotsAdded){
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //3km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 10 / 111.0;
                                REFERLNG = 10 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        } else {
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 음식점 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //3km 내 음식점 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 10 / 111.0;
                                REFERLNG = 10 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }

                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 10 / 111.0;
                        double REFERLNG = 10 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : dinnerSpots) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                nearbySpots.add(spot);
                                spotsAdded = true;
                            }
                        }
                        //10km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 12 / 111.0;
                            REFERLNG = 12 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                        //12km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 15 / 111.0;
                            REFERLNG = 15 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                    }

                } else if(howChoice.equals("대중교통")) {
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 음식점이 없을 때
                            if (!spotsAdded){
                                REFERLAT = 2 / 111.0;
                                REFERLNG = 2 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //2km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 7 / 111.0;
                                REFERLNG = 7 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        } else {
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //3km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 7 / 111.0;
                                REFERLNG = 7 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }
                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 5 / 111.0;
                        double REFERLNG = 5 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : dinnerSpots) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                nearbySpots.add(spot);
                                spotsAdded = true;
                            }
                        }
                        //5km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 7 / 111.0;
                            REFERLNG = 7 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                        //7km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 10 / 111.0;
                            REFERLNG = 10 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                    }
                }

                for(cafeSpot nearspot : nearbySpots){
                    String name = nearspot.getName();
                    String time = nearspot.getTime();
                    Log.d("Spot Info", "밥집 이름: " + name + ", 식사시간: " + time);
                }

                if (!nearbySpots.isEmpty()) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(nearbySpots.size());
                    cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                    recommendedSpot recommended = new recommendedSpot(
                            nearbySpot.getName(),
                            nearbySpot.getType(),
                            nearbySpot.getAddress(),
                            nearbySpot.getImage(),
                            nearbySpot.getLat(),
                            nearbySpot.getLon()
                    );

                    if (!recommendedSpots.contains(recommended)) {
                        recommendedSpots.add(recommended);
                    } else {
                        int attempts = 0;
                        int maxAttempts = nearbySpots.size() * 2;

                        while (attempts < maxAttempts) {
                            randomIndex = random.nextInt(nearbySpots.size());
                            nearbySpot = nearbySpots.get(randomIndex);

                            recommended = new recommendedSpot(
                                    nearbySpot.getName(),
                                    nearbySpot.getType(),
                                    nearbySpot.getAddress(),
                                    nearbySpot.getImage(),
                                    nearbySpot.getLat(),
                                    nearbySpot.getLon()
                            );

                            if (!recommendedSpots.contains(recommended)) {
                                recommendedSpots.add(recommended);
                                break;
                            }

                            attempts++;
                        }
                    }
                }


            } else if(token.equals("4")) {
                Log.d("추천일정 번호", token);
                String csvFilePath = "final.csv";
                List<cafeSpot> nearbySpots = new ArrayList<>();
                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "브런치");

                if(howChoice.equals("자차")){
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 음식점이 없을 때
                            if (!spotsAdded){
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //3km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 10 / 111.0;
                                REFERLNG = 10 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        } else {
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }

                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 5 / 111.0;
                        double REFERLNG = 5 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : spotList) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                nearbySpots.add(spot);
                                spotsAdded = true;
                            }
                        }
                        //5km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 10 / 111.0;
                            REFERLNG = 10 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                    }

                } else if(howChoice.equals("대중교통")) {
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 카페가 없을 때
                            if (!spotsAdded){
                                REFERLAT = 2 / 111.0;
                                REFERLNG = 2 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //2km 내 카페가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 5 / 111.0;
                                REFERLNG = 5 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        } else {
                            double REFERLAT = 3 / 111.0;
                            double REFERLNG = 3 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //3km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 6 / 111.0;
                                REFERLNG = 6 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            } //6km 내 음식점이 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 10 / 111.0;
                                REFERLNG = 10 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }
                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 3 / 111.0;
                        double REFERLNG = 3 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : spotList) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                nearbySpots.add(spot);
                                spotsAdded = true;
                            }
                        }
                        //3km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 6 / 111.0;
                            REFERLNG = 6 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        } //6km 내 음식점이 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 10 / 111.0;
                            REFERLNG = 10 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                    }
                }

                for(cafeSpot nearspot : nearbySpots){
                    String name = nearspot.getName();
                    String time = nearspot.getTime();
                    Log.d("Spot Info", "밥집 이름: " + name + ", 식사시간: " + time);
                }

                if (!nearbySpots.isEmpty()) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(nearbySpots.size());
                    cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                    recommendedSpot recommended = new recommendedSpot(
                            nearbySpot.getName(),
                            nearbySpot.getType(),
                            nearbySpot.getAddress(),
                            nearbySpot.getImage(),
                            nearbySpot.getLat(),
                            nearbySpot.getLon()
                    );

                    if (!recommendedSpots.contains(recommended)) {
                        recommendedSpots.add(recommended);
                    } else {
                        int attempts = 0;
                        int maxAttempts = nearbySpots.size() * 2;

                        while (attempts < maxAttempts) {
                            randomIndex = random.nextInt(nearbySpots.size());
                            nearbySpot = nearbySpots.get(randomIndex);

                            recommended = new recommendedSpot(
                                    nearbySpot.getName(),
                                    nearbySpot.getType(),
                                    nearbySpot.getAddress(),
                                    nearbySpot.getImage(),
                                    nearbySpot.getLat(),
                                    nearbySpot.getLon()
                            );

                            if (!recommendedSpots.contains(recommended)) {
                                recommendedSpots.add(recommended);
                                break;
                            }

                            attempts++;
                        }
                    }
                }


            } else if(token.equals("5")) {
                Log.d("추천일정 번호", token);
                String csvFilePath = "final.csv";
                List<cafeSpot> nearbySpots = new ArrayList<>();
                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "카페");

                if(howChoice.equals("자차")){
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 카페가 없을 때
                            if (!spotsAdded){
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //3km 내 카페가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 10 / 111.0;
                                REFERLNG = 10 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        } else {
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 카페가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            } //7km 내 카페가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 10 / 111.0;
                                REFERLNG = 10 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }

                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 5 / 111.0;
                        double REFERLNG = 5 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : spotList) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                nearbySpots.add(spot);
                                spotsAdded = true;
                            }
                        }
                        //5km 내 카페가 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 10 / 111.0;
                            REFERLNG = 10 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                    }

                } else if(howChoice.equals("대중교통")) {
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //3km 내 카페가 없을 때
                            if (!spotsAdded){
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //4km 내 카페가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 8 / 111.0;
                                REFERLNG = 8 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        } else {
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, new LatLng(lastRecommendedLat, lastRecommendedLng), spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                            //1km 내 카페가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            } //3km 내 카페가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 10 / 111.0;
                                REFERLNG = 10 / 88.74;

                                for (cafeSpot spot : spotList) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }
                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 4 / 111.0;
                        double REFERLNG = 4 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : spotList) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                nearbySpots.add(spot);
                                spotsAdded = true;
                            }
                        }
                        //5km 내 카페가 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 10 / 111.0;
                            REFERLNG = 10 / 88.74;

                            for (cafeSpot spot : spotList) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                    }
                }

                for(cafeSpot nearspot : nearbySpots){
                    String name = nearspot.getName();
                    String time = nearspot.getTime();
                    Log.d("Spot Info", "카페 이름: " + name + ", 시간: " + time);
                }

                while(true){
                    if (!nearbySpots.isEmpty()) {
                        Random random = new Random();
                        int randomIndex = random.nextInt(nearbySpots.size());
                        cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                        boolean spotsCheck = false;
                        if(recommendedSpots.size() == 0 || nearbySpots.size() == 1){
                            recommendedSpot recommended = new recommendedSpot(
                                    nearbySpot.getName(),
                                    nearbySpot.getType(),
                                    nearbySpot.getAddress(),
                                    nearbySpot.getImage(),
                                    nearbySpot.getLat(),
                                    nearbySpot.getLon()
                            );
                            recommendedSpots.add(recommended);
                            break;
                        } else {
                            for (int i = 0; i < recommendedSpots.size() - 1; i++) {
                                recommendedSpot recommnded = recommendedSpots.get(i);
                                if (recommnded.getName().equals(nearbySpot.getName())) {
                                    spotsCheck = true;
                                }
                            }
                            if (spotsCheck) {
                                continue;
                            } else {
                                recommendedSpot recommended = new recommendedSpot(
                                        nearbySpot.getName(),
                                        nearbySpot.getType(),
                                        nearbySpot.getAddress(),
                                        nearbySpot.getImage(),
                                        nearbySpot.getLat(),
                                        nearbySpot.getLon()
                                );
                                recommendedSpots.add(recommended);
                                break;
                            }
                        }
                    }
                }

            } else if(token.equals("6")) {
                Log.d("추천일정 번호", token);
                String csvFilePath = "final.csv";
                List<cafeSpot> nearbySpots = new ArrayList<>();
                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "여행지");
                List<cafeSpot> sunnySpots = filterSpotListByTime(spotList, "낮");

                if(howChoice.equals("자차")){
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        Log.d("마지막 리스트 좌표", "Lat : " + lastRecommendedSpot.getLat() + ", Lon: " + lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : sunnySpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                spotsAdded = false;
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }

                            //1km 내 여행지가 없을 때
                            if (!spotsAdded){
                                REFERLAT = 4 / 111.0;
                                REFERLNG = 4 / 88.74;

                                for (cafeSpot spot : sunnySpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    spotsAdded = false;
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    }
                                }
                            }
                            //4km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 12 / 111.0;
                                REFERLNG = 12 / 88.74;

                                for (cafeSpot spot : sunnySpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    spotsAdded = false;
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    }
                                }
                            }
                        } else {
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : sunnySpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                spotsAdded = false;
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //1km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 4 / 111.0;
                                REFERLNG = 4 / 88.74;

                                for (cafeSpot spot : sunnySpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    spotsAdded = false;
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    }
                                }
                            }
                            //4km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 12 / 111.0;
                                REFERLNG = 12 / 88.74;

                                for (cafeSpot spot : sunnySpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    spotsAdded = false;
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 5 / 111.0;
                        double REFERLNG = 5 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : sunnySpots) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                if(recommendedSpots.size() != 0){
                                    for(int i = 0; i < recommendedSpots.size(); i++){
                                        recommendedSpot recommnded = recommendedSpots.get(i);
                                        if (!recommnded.getName().equals(spot.getName())) {
                                            nearbySpots.add(spot);
                                        } else {
                                            spotsAdded = false;
                                            continue;
                                        }
                                    }
                                } else {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                        //5km 내 여행지가 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 12 / 111.0;
                            REFERLNG = 12 / 88.74;

                            for (cafeSpot spot : sunnySpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                spotsAdded = false;
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }
                    }

                } else if(howChoice.equals("대중교통")) {
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : sunnySpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                spotsAdded = false;
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //1km 내 여행지가 없을 때
                            if (!spotsAdded){
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : sunnySpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    spotsAdded = false;
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    }
                                }
                            }
                            //3km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 8 / 111.0;
                                REFERLNG = 8 / 88.74;

                                for (cafeSpot spot : sunnySpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    spotsAdded = false;
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    }
                                }
                            }
                            //8km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 14 / 111.0;
                                REFERLNG = 14 / 88.74;

                                for (cafeSpot spot : sunnySpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    spotsAdded = false;
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    }
                                }
                            }
                        } else {
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : sunnySpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                spotsAdded = false;
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                            //1km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : sunnySpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    spotsAdded = false;
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    }
                                }
                            }
                            //3km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 8 / 111.0;
                                REFERLNG = 8 / 88.74;

                                for (cafeSpot spot : sunnySpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    spotsAdded = false;
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    }
                                }
                            }
                            //8km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 14 / 111.0;
                                REFERLNG = 14 / 88.74;

                                for (cafeSpot spot : sunnySpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    spotsAdded = false;
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 1 / 111.0;
                        double REFERLNG = 1 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : sunnySpots) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                if(recommendedSpots.size() != 0){
                                    for(int i = 0; i < recommendedSpots.size(); i++){
                                        recommendedSpot recommnded = recommendedSpots.get(i);
                                        if (!recommnded.getName().equals(spot.getName())) {
                                            nearbySpots.add(spot);
                                        } else {
                                            spotsAdded = false;
                                            continue;
                                        }
                                    }
                                } else {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            }
                        }
                        //1km 내 여행지가 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 4 / 111.0;
                            REFERLNG = 4 / 88.74;

                            for (cafeSpot spot : sunnySpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                spotsAdded = false;
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }
                        //4km 내 여행지가 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 8 / 111.0;
                            REFERLNG = 8 / 88.74;

                            for (cafeSpot spot : sunnySpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                spotsAdded = false;
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }
                        //14km 내 여행지가 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 14 / 111.0;
                            REFERLNG = 14 / 88.74;

                            for (cafeSpot spot : sunnySpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                spotsAdded = false;
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                }
                            }
                        }
                    }
                }

                for(cafeSpot nearspot : nearbySpots){
                    String name = nearspot.getName();
                    String time = nearspot.getTime();
                    Log.d("Spot Info", "장소 이름: " + name + ", 주야: " + time);
                }

                while(true){
                    if (!nearbySpots.isEmpty()) {
                        Random random = new Random();
                        int randomIndex = random.nextInt(nearbySpots.size());
                        cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                        boolean spotsCheck = false;
                        if(recommendedSpots.size() == 0 || nearbySpots.size() == 1){
                            recommendedSpot recommended = new recommendedSpot(
                                    nearbySpot.getName(),
                                    nearbySpot.getType(),
                                    nearbySpot.getAddress(),
                                    nearbySpot.getImage(),
                                    nearbySpot.getLat(),
                                    nearbySpot.getLon()
                            );
                            recommendedSpots.add(recommended);
                            break;
                        } else {
                            for (int i = 0; i < recommendedSpots.size(); i++) {
                                recommendedSpot recommnded = recommendedSpots.get(i);
                                if (recommnded.getName().equals(nearbySpot.getName())) {
                                    spotsCheck = true;
                                }
                            }
                            if (spotsCheck) {
                                continue;
                            } else {
                                recommendedSpot recommended = new recommendedSpot(
                                        nearbySpot.getName(),
                                        nearbySpot.getType(),
                                        nearbySpot.getAddress(),
                                        nearbySpot.getImage(),
                                        nearbySpot.getLat(),
                                        nearbySpot.getLon()
                                );
                                recommendedSpots.add(recommended);
                                break;
                            }
                        }
                    }
                }

            } else {
                Log.d("추천일정 번호", token);
                String csvFilePath = "final.csv";
                List<cafeSpot> nearbySpots = new ArrayList<>();
                List<cafeSpot> spotList = readCSV(csvFilePath, whoChoice, whereChoice, howChoice, "여행지");
                List<cafeSpot> dinnerSpots = filterSpotListByTime(spotList, "밤");

                if(howChoice.equals("자차")){
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 true 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                } if(nearbySpots.size() < 3) {
                                    nearbySpots.clear();
                                    spotsAdded = false;
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-1km", "장소 이름: " + name + ", 주야: " + time);
                            }
                            //1km 내 여행지가 없을 때
                            if (!spotsAdded){
                                REFERLAT = 5 / 111.0;
                                REFERLNG = 5 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    } if(nearbySpots.size() < 3) {
                                        nearbySpots.clear();
                                        spotsAdded = false;
                                    }
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-5km", "장소 이름: " + name + ", 주야: " + time);
                            }
                            //5km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 8 / 111.0;
                                REFERLNG = 8 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    } if(nearbySpots.size() < 3) {
                                        nearbySpots.clear();
                                        spotsAdded = false;
                                    }
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-8km", "장소 이름: " + name + ", 주야: " + time);
                            }
                            //8km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 15 / 111.0;
                                REFERLNG = 15 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    } if(nearbySpots.size() < 3) {
                                        nearbySpots.clear();
                                        spotsAdded = false;
                                    }
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-15km", "장소 이름: " + name + ", 주야: " + time);
                            }
                        } else {
                            double REFERLAT = 2 / 111.0;
                            double REFERLNG = 2 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                } if(nearbySpots.size() < 3) {
                                    nearbySpots.clear();
                                    spotsAdded = false;
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-2km", "장소 이름: " + name + ", 주야: " + time);
                            }
                            //2km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 4 / 111.0;
                                REFERLNG = 4 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    } if(nearbySpots.size() < 3) {
                                        nearbySpots.clear();
                                        spotsAdded = false;
                                    }
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-4km", "장소 이름: " + name + ", 주야: " + time);
                            }
                            //4km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 8 / 111.0;
                                REFERLNG = 8 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    } if(nearbySpots.size() < 3) {
                                        nearbySpots.clear();
                                        spotsAdded = false;
                                    }
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-8km", "장소 이름: " + name + ", 주야: " + time);
                            }
                            //8km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 15 / 111.0;
                                REFERLNG = 15 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    } if(nearbySpots.size() < 3) {
                                        nearbySpots.clear();
                                        spotsAdded = false;
                                    }
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-15km", "장소 이름: " + name + ", 주야: " + time);
                            }
                        }

                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 3 / 111.0;
                        double REFERLNG = 3 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : dinnerSpots) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                if(recommendedSpots.size() != 0){
                                    for(int i = 0; i < recommendedSpots.size(); i++){
                                        recommendedSpot recommnded = recommendedSpots.get(i);
                                        if (!recommnded.getName().equals(spot.getName())) {
                                            nearbySpots.add(spot);
                                        } else {
                                            continue;
                                        }
                                    }
                                } else {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            } if(nearbySpots.size() < 3) {
                                nearbySpots.clear();
                                spotsAdded = false;
                            }
                        }
                        for(cafeSpot nearspot : nearbySpots){
                            String name = nearspot.getName();
                            String time = nearspot.getTime();
                            Log.d("7번-3km", "장소 이름: " + name + ", 주야: " + time);
                        }
                        //1km 내 여행지가 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 6 / 111.0;
                            REFERLNG = 6 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                } if(nearbySpots.size() < 3) {
                                    nearbySpots.clear();
                                    spotsAdded = false;
                                }
                            }
                        }
                        for(cafeSpot nearspot : nearbySpots){
                            String name = nearspot.getName();
                            String time = nearspot.getTime();
                            Log.d("7번-5km", "장소 이름: " + name + ", 주야: " + time);
                        }
                        //5km 내 여행지가 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 8 / 111.0;
                            REFERLNG = 8 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                } if(nearbySpots.size() < 3) {
                                    nearbySpots.clear();
                                    spotsAdded = false;
                                }
                            }
                        }
                        for(cafeSpot nearspot : nearbySpots){
                            String name = nearspot.getName();
                            String time = nearspot.getTime();
                            Log.d("7번-8km", "장소 이름: " + name + ", 주야: " + time);
                        }
                        //8km 내 여행지가 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 15 / 111.0;
                            REFERLNG = 15 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                } if(nearbySpots.size() < 3) {
                                    nearbySpots.clear();
                                    spotsAdded = false;
                                }
                            }
                        }
                        for(cafeSpot nearspot : nearbySpots){
                            String name = nearspot.getName();
                            String time = nearspot.getTime();
                            Log.d("7번-15km", "장소 이름: " + name + ", 주야: " + time);
                        }
                    }

                } else if(howChoice.equals("대중교통")) {
                    if (!recommendedSpots.isEmpty()) {
                        // recommendedSpots 리스트가 비어있지 않은 경우
                        boolean hasRecommendedTravelSpot = false;
                        for (recommendedSpot recommendedSpot : recommendedSpots) {
                            if (recommendedSpot.getType().equals("여행지")) {
                                hasRecommendedTravelSpot = true;
                                break;
                            }
                        } // recommendedSpots에 여행지 속성 데이터가 있을때 boolean값 지정

                        recommendedSpot lastRecommendedSpot = recommendedSpots.get(recommendedSpots.size() - 1);
                        double lastRecommendedLat = Double.parseDouble(lastRecommendedSpot.getLat());
                        double lastRecommendedLng = Double.parseDouble(lastRecommendedSpot.getLon());
                        boolean spotsAdded = false;

                        if(hasRecommendedTravelSpot){
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                } if(nearbySpots.size() < 3) {
                                    nearbySpots.clear();
                                    spotsAdded = false;
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-1km", "장소 이름: " + name + ", 주야: " + time);
                            }
                            //1km 내 여행지가 없을 때
                            if (!spotsAdded){
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    } if(nearbySpots.size() < 3) {
                                        nearbySpots.clear();
                                        spotsAdded = false;
                                    }
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-3km", "장소 이름: " + name + ", 주야: " + time);
                            }
                            //3km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 8 / 111.0;
                                REFERLNG = 8 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    } if(nearbySpots.size() < 3) {
                                        nearbySpots.clear();
                                        spotsAdded = false;
                                    }
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-8km", "장소 이름: " + name + ", 주야: " + time);
                            }
                            //8km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 15 / 111.0;
                                REFERLNG = 15 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    } if(nearbySpots.size() < 3) {
                                        nearbySpots.clear();
                                        spotsAdded = false;
                                    }
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-15km", "장소 이름: " + name + ", 주야: " + time);
                            }
                        } else {
                            double REFERLAT = 1 / 111.0;
                            double REFERLNG = 1 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                } if(nearbySpots.size() < 3) {
                                    nearbySpots.clear();
                                    spotsAdded = false;
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-1km", "장소 이름: " + name + ", 주야: " + time);
                            }
                            //1km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 3 / 111.0;
                                REFERLNG = 3 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    } if(nearbySpots.size() < 3) {
                                        nearbySpots.clear();
                                        spotsAdded = false;
                                    }
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-3km", "장소 이름: " + name + ", 주야: " + time);
                            }
                            //3km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 7 / 111.0;
                                REFERLNG = 7 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    } if(nearbySpots.size() < 3) {
                                        nearbySpots.clear();
                                        spotsAdded = false;
                                    }
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-7km", "장소 이름: " + name + ", 주야: " + time);
                            }
                            //7km 내 여행지가 없을 때
                            if (!spotsAdded) {
                                REFERLAT = 14 / 111.0;
                                REFERLNG = 14 / 88.74;

                                for (cafeSpot spot : dinnerSpots) {
                                    double spotLatValue = Double.parseDouble(spot.getLat());
                                    double spotLonValue = Double.parseDouble(spot.getLon());
                                    LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                    if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                        if(recommendedSpots.size() != 0){
                                            for(int i = 0; i < recommendedSpots.size(); i++){
                                                recommendedSpot recommnded = recommendedSpots.get(i);
                                                if (!recommnded.getName().equals(spot.getName())) {
                                                    nearbySpots.add(spot);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } else {
                                            nearbySpots.add(spot);
                                            spotsAdded = true;
                                        }
                                    } if(nearbySpots.size() < 3) {
                                        nearbySpots.clear();
                                        spotsAdded = false;
                                    }
                                }
                            }
                            for(cafeSpot nearspot : nearbySpots){
                                String name = nearspot.getName();
                                String time = nearspot.getTime();
                                Log.d("7번-14km", "장소 이름: " + name + ", 주야: " + time);
                            }
                        }
                    } else {
                        // recommendedSpots 리스트가 비어있는 경우

                        double REFERLAT = 1 / 111.0;
                        double REFERLNG = 1 / 88.74;
                        boolean spotsAdded = false;

                        for (cafeSpot spot : dinnerSpots) {
                            double spotLatValue = Double.parseDouble(spot.getLat());
                            double spotLonValue = Double.parseDouble(spot.getLon());
                            LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                            if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                if(recommendedSpots.size() != 0){
                                    for(int i = 0; i < recommendedSpots.size(); i++){
                                        recommendedSpot recommnded = recommendedSpots.get(i);
                                        if (!recommnded.getName().equals(spot.getName())) {
                                            nearbySpots.add(spot);
                                        } else {
                                            continue;
                                        }
                                    }
                                } else {
                                    nearbySpots.add(spot);
                                    spotsAdded = true;
                                }
                            } if(nearbySpots.size() < 3) {
                                nearbySpots.clear();
                                spotsAdded = false;
                            }
                        }
                        for(cafeSpot nearspot : nearbySpots){
                            String name = nearspot.getName();
                            String time = nearspot.getTime();
                            Log.d("7번-1km", "장소 이름: " + name + ", 주야: " + time);
                        }
                        //1km 내 여행지가 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 3 / 111.0;
                            REFERLNG = 3 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                } if(nearbySpots.size() < 3) {
                                    nearbySpots.clear();
                                    spotsAdded = false;
                                }
                            }
                        }
                        for(cafeSpot nearspot : nearbySpots){
                            String name = nearspot.getName();
                            String time = nearspot.getTime();
                            Log.d("7번-3km", "장소 이름: " + name + ", 주야: " + time);
                        }
                        //3km 내 여행지가 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 7 / 111.0;
                            REFERLNG = 7 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                } if(nearbySpots.size() < 3) {
                                    nearbySpots.clear();
                                    spotsAdded = false;
                                }
                            }
                        }
                        for(cafeSpot nearspot : nearbySpots){
                            String name = nearspot.getName();
                            String time = nearspot.getTime();
                            Log.d("7번-7km", "장소 이름: " + name + ", 주야: " + time);
                        }
                        //7km 내 여행지가 없을 때
                        if (!spotsAdded) {
                            REFERLAT = 14 / 111.0;
                            REFERLNG = 14 / 88.74;

                            for (cafeSpot spot : dinnerSpots) {
                                double spotLatValue = Double.parseDouble(spot.getLat());
                                double spotLonValue = Double.parseDouble(spot.getLon());
                                LatLng spotLatLng = new LatLng(spotLatValue, spotLonValue);

                                if (withinSightMarker(REFERLAT, REFERLNG, currentLatLng, spotLatLng)) {
                                    if(recommendedSpots.size() != 0){
                                        for(int i = 0; i < recommendedSpots.size(); i++){
                                            recommendedSpot recommnded = recommendedSpots.get(i);
                                            if (!recommnded.getName().equals(spot.getName())) {
                                                nearbySpots.add(spot);
                                            } else {
                                                continue;
                                            }
                                        }
                                    } else {
                                        nearbySpots.add(spot);
                                        spotsAdded = true;
                                    }
                                } if(nearbySpots.size() < 3) {
                                    nearbySpots.clear();
                                    spotsAdded = false;
                                }
                            }
                        }
                        for(cafeSpot nearspot : nearbySpots){
                            String name = nearspot.getName();
                            String time = nearspot.getTime();
                            Log.d("7번-14km", "장소 이름: " + name + ", 주야: " + time);
                        }
                    }
                }

                while(true){
                    if (!nearbySpots.isEmpty()) {
                        Random random = new Random();
                        int randomIndex = random.nextInt(nearbySpots.size());
                        cafeSpot nearbySpot = nearbySpots.get(randomIndex);

                        boolean spotsCheck = false;
                        if(recommendedSpots.size() == 0 || nearbySpots.size() == 1){
                            recommendedSpot recommended = new recommendedSpot(
                                    nearbySpot.getName(),
                                    nearbySpot.getType(),
                                    nearbySpot.getAddress(),
                                    nearbySpot.getImage(),
                                    nearbySpot.getLat(),
                                    nearbySpot.getLon()
                            );
                            recommendedSpots.add(recommended);
                            break;
                        } else {
                            for (int i = 0; i < recommendedSpots.size(); i++) {
                                recommendedSpot recommnded = recommendedSpots.get(i);
                                if (recommnded.getName().equals(nearbySpot.getName())) {
                                    spotsCheck = true;
                                }
                            }
                            if (spotsCheck) {
                                continue;
                            } else {
                                recommendedSpot recommended = new recommendedSpot(
                                        nearbySpot.getName(),
                                        nearbySpot.getType(),
                                        nearbySpot.getAddress(),
                                        nearbySpot.getImage(),
                                        nearbySpot.getLat(),
                                        nearbySpot.getLon()
                                );
                                recommendedSpots.add(recommended);
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }

            }
        }
        return recommendedSpots;
    }

    private List<cafeSpot> readCSV(String csvFilePath, String value1, String value2, String value3, String classify) {
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


                if (who.equals(value1) && what.equals(value2) && how.equals(value3) && type.equals(classify)) { // 또는 다른 조건으로 변경 가능
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


    List<cafeSpot> filterSpotListByTime(List<cafeSpot> spotList, String desiredTime) {
        List<cafeSpot> filteredList = new ArrayList<>();

        for (cafeSpot spot : spotList) {
            if (spot.getTime().equals(desiredTime)) {
                filteredList.add(spot);
            }
        }

        return filteredList;
    }


    private void saverecommendedListToDatabase(List<recommendedSpot> spotList) {
        Appdatabase appDatabase = Appdatabase.getInstance(getApplicationContext());
        recommendedSpotDao recommendedspotdao = appDatabase.recommendedSpotDao();
        for (recommendedSpot spot : spotList) {
            recommendedspotdao.insert(spot);
        }
    }


}

