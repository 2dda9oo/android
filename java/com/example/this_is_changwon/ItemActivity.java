package com.example.this_is_changwon;

import static android.service.controls.ControlsProviderService.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.Tm128;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Align;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ItemActivity extends AppCompatActivity implements OnMapReadyCallback {

    private List<TravelSpot> mData;
    private List<TravelSpot1> mData1;
    private List<TravelSpot2> mData2;
    private List<TravelSpot3> mData3;
    private List<TravelSpot4> mData4;

    private Appdatabase db;
    private List<Data> dataList;
    private FragmentActivity context;

    private String clientId = "o8kg4gcfel";
    private String clientSecret = "DKrxsEbzi5E14Rb7A8rKRlVX7EXHrWJuTiSmv2Q9";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private FusedLocationSource locationSource;
    private NaverMap naverMap2;
    private Geocoder geocoder;

    TextView table_text;
    ListView scheduleView;
    Button scheduleadd, pathCheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        db = Appdatabase.getInstance(this);

        table_text = (TextView) findViewById(R.id.schedule_name);
        scheduleView = findViewById(R.id.schedule_view);
        scheduleadd = findViewById(R.id.schedule_add);
        pathCheck = findViewById(R.id.route_batch);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.navermap2);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.navermap2, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        scheduleadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(ItemActivity.this, MainActivity.class);
                startActivity(intent2);
            }
        });

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) {
                naverMap2.setLocationTrackingMode(LocationTrackingMode.None);
                return;
            } else {
                naverMap2.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap2 = naverMap;

        //일정 검색 결과 표시화면
        Intent intent = getIntent();
        String table_name = intent.getStringExtra("table");
        Log.d("전달된 테이블 이름", table_name);

        switch (table_name){
            case "spot0":
                String sname = db.dataDao().findNameByTitle("spot0");
                table_text.setText(sname);

                TravelDao travelDao = db.table1();
                mData = travelDao.findAll();

                CustomListView ListAdapter1 = new CustomListView(mData);
                scheduleView.setAdapter(ListAdapter1);
                ListAdapter1.notifyDataSetChanged();

                List<LatLng> coordinates = new ArrayList<>();

                for(TravelSpot spot : mData){
                    StringTokenizer st = new StringTokenizer(spot.getLat(), ".");
                    if(st.countTokens() == 2){ // 공공데이터로 파싱한 위도 경도
                        LatLng latLng = new LatLng(Double.parseDouble(spot.getLat()), Double.parseDouble(spot.getLon()));
                        Log.d("위도경도", spot.getLat() +" "+ spot.getLon());
                        Marker marker = new Marker();
                        marker.setPosition(latLng);
                        marker.setIconTintColor(Color.RED);
                        marker.setCaptionText(spot.getName());
                        marker.setCaptionAligns(Align.Top);
                        marker.setCaptionColor(Color.BLACK);
                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                        marker.setCaptionTextSize(14);
                        marker.setMap(naverMap2);
                        coordinates.add(latLng);
                    } else{ // 네이버 검색 API 통한 위도 경도
                        Tm128 tm = new Tm128(Double.parseDouble(spot.getLon()), Double.parseDouble(spot.getLat()));
                        Log.d("위도경도", spot.getLat() +" "+ spot.getLon());
                        Marker marker = new Marker();
                        marker.setPosition(tm.toLatLng());
                        marker.setMap(naverMap2);
                        marker.setIconTintColor(Color.RED);
                        marker.setCaptionText(spot.getName());
                        marker.setCaptionAligns(Align.Top);
                        marker.setCaptionColor(Color.BLACK);
                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                        marker.setCaptionTextSize(14);
                        coordinates.add(tm.toLatLng());
                    }
                }

                pathCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 모든 경로의 좌표를 저장할 리스트
                        List<List<LatLng>> allPaths = new ArrayList<>();

                        for(int i = 0; i < mData.size() - 1; i++){
                            TravelSpot currentSpot = mData.get(i);
                            TravelSpot nextSpot = mData.get(i + 1);

                            String startLat = currentSpot.getLat();
                            String startLon = currentSpot.getLon();
                            String nextLat = nextSpot.getLat();
                            String nextLon = nextSpot.getLon();
                            getDirectionPath(startLon, startLat, nextLon, nextLat, new SearchCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    // 응답을 파싱하여 좌표 리스트에 추가
                                    List<LatLng> pathCoordinates = parsePathCoordinatesFromJson(result);
                                    allPaths.add(pathCoordinates);

                                    // 모든 경로 정보를 저장한 후 처리할 작업 수행
                                    if (allPaths.size() == mData.size() - 1) {
                                        // 모든 경로 정보가 저장되었으므로 여기에서 처리 작업을 수행
                                        // allPaths 리스트에는 모든 경로의 좌표가 저장되어 있음
                                        processAllPaths(allPaths);
                                    }

                                    // 모든 경로를 표시하기 위한 반복문
                                    for (List<LatLng> coordinates : allPaths) {
                                        // path 객체 생성
                                        PathOverlay path = new PathOverlay();

                                        // LatLng 배열을 PathOverlay 객체에 설정
                                        path.setCoords(coordinates);

                                        path.setPatternImage(OverlayImage.fromResource(R.drawable.ic_baseline_navigation_24));
                                        path.setPatternInterval(25);

                                        // 선의 스타일 및 이미지 설정
                                        path.setColor(Color.GREEN); // 초록색으로 설정
                                        path.setWidth(12);

                                        // 지도에 선 표시
                                        path.setMap(naverMap2);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // 실패 시 처리
                                }
                            });
                        }
                    }
                });

                break;
            case "spot1":
                String sname2 = db.dataDao().findNameByTitle("spot1");
                table_text.setText(sname2);

                TravelDao1 travelDao1 = db.table2();
                mData1 = travelDao1.findAll();

                CustomListView1 ListAdapter2 = new CustomListView1(mData1);
                scheduleView.setAdapter(ListAdapter2);
                ListAdapter2.notifyDataSetChanged();

                PathOverlay path1 = new PathOverlay();
                List<LatLng> coordinates1 = new ArrayList<>();

                for(TravelSpot1 spot1 : mData1){
                    StringTokenizer st = new StringTokenizer(spot1.getLat(), ".");
                    if(st.countTokens() == 2){ // 공공데이터로 파싱한 위도 경도
                        LatLng latLng = new LatLng(Double.parseDouble(spot1.getLat()), Double.parseDouble(spot1.getLon()));
                        Log.d("위도경도", spot1.getLat() +" "+ spot1.getLon());
                        Marker marker = new Marker();
                        marker.setPosition(latLng);
                        marker.setIconTintColor(Color.RED);
                        marker.setCaptionText(spot1.getTitle());
                        marker.setCaptionAligns(Align.Top);
                        marker.setCaptionColor(Color.BLACK);
                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                        marker.setCaptionTextSize(14);
                        marker.setMap(naverMap2);
                        coordinates1.add(latLng);
                    } else{ // 네이버 검색 API 통한 위도 경도
                        Tm128 tm = new Tm128(Double.parseDouble(spot1.getLon()), Double.parseDouble(spot1.getLat()));
                        Log.d("위도경도", spot1.getLat() +" "+ spot1.getLon());
                        Marker marker = new Marker();
                        marker.setPosition(tm.toLatLng());
                        marker.setMap(naverMap2);
                        marker.setIconTintColor(Color.RED);
                        marker.setCaptionText(spot1.getTitle());
                        marker.setCaptionAligns(Align.Top);
                        marker.setCaptionColor(Color.BLACK);
                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                        marker.setCaptionTextSize(14);
                        coordinates1.add(tm.toLatLng());
                    }
                }

                pathCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 모든 경로의 좌표를 저장할 리스트
                        List<List<LatLng>> allPaths = new ArrayList<>();

                        for(int i = 0; i < mData1.size() - 1; i++){
                            TravelSpot1 currentSpot = mData1.get(i);
                            TravelSpot1 nextSpot = mData1.get(i + 1);

                            String startLat = currentSpot.getLat();
                            String startLon = currentSpot.getLon();
                            String nextLat = nextSpot.getLat();
                            String nextLon = nextSpot.getLon();
                            getDirectionPath(startLon, startLat, nextLon, nextLat, new SearchCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    // 응답을 파싱하여 좌표 리스트에 추가
                                    List<LatLng> pathCoordinates = parsePathCoordinatesFromJson(result);
                                    allPaths.add(pathCoordinates);

                                    // 모든 경로 정보를 저장한 후 처리할 작업 수행
                                    if (allPaths.size() == mData1.size() - 1) {
                                        // 모든 경로 정보가 저장되었으므로 여기에서 처리 작업을 수행
                                        // allPaths 리스트에는 모든 경로의 좌표가 저장되어 있음
                                        processAllPaths(allPaths);
                                    }

                                    // 모든 경로를 표시하기 위한 반복문
                                    for (List<LatLng> coordinates : allPaths) {
                                        // path 객체 생성
                                        PathOverlay path = new PathOverlay();

                                        // LatLng 배열을 PathOverlay 객체에 설정
                                        path.setCoords(coordinates);

                                        path.setPatternImage(OverlayImage.fromResource(R.drawable.ic_baseline_navigation_24));
                                        path.setPatternInterval(25);

                                        // 선의 스타일 및 이미지 설정
                                        path.setColor(Color.GREEN); // 초록색으로 설정
                                        path.setWidth(12);

                                        // 지도에 선 표시
                                        path.setMap(naverMap2);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // 실패 시 처리
                                }
                            });
                        }
                    }
                });

                break;
            case "spot2":
                String sname3 = db.dataDao().findNameByTitle("spot2");
                table_text.setText(sname3);

                TravelDao2 travelDao2 = db.table3();
                mData2 = travelDao2.findAll();

                CustomListView2 ListAdapter3 = new CustomListView2(mData2);
                scheduleView.setAdapter(ListAdapter3);
                ListAdapter3.notifyDataSetChanged();

                PathOverlay path2 = new PathOverlay();
                List<LatLng> coordinates2 = new ArrayList<>();

                for(TravelSpot2 spot2 : mData2){
                    StringTokenizer st = new StringTokenizer(spot2.getLat(), ".");
                    if(st.countTokens() == 2){ // 공공데이터로 파싱한 위도 경도
                        LatLng latLng = new LatLng(Double.parseDouble(spot2.getLat()), Double.parseDouble(spot2.getLon()));
                        Log.d("위도경도", spot2.getLat() +" "+ spot2.getLon());
                        Marker marker = new Marker();
                        marker.setPosition(latLng);
                        marker.setIconTintColor(Color.RED);
                        marker.setCaptionText(spot2.getTitle());
                        marker.setCaptionAligns(Align.Top);
                        marker.setCaptionColor(Color.BLACK);
                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                        marker.setCaptionTextSize(14);
                        marker.setMap(naverMap2);
                        coordinates2.add(latLng);
                    } else{ // 네이버 검색 API 통한 위도 경도
                        Tm128 tm = new Tm128(Double.parseDouble(spot2.getLon()), Double.parseDouble(spot2.getLat()));
                        Log.d("위도경도", spot2.getLat() +" "+ spot2.getLon());
                        Marker marker = new Marker();
                        marker.setPosition(tm.toLatLng());
                        marker.setMap(naverMap2);
                        marker.setIconTintColor(Color.RED);
                        marker.setCaptionText(spot2.getTitle());
                        marker.setCaptionAligns(Align.Top);
                        marker.setCaptionColor(Color.BLACK);
                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                        marker.setCaptionTextSize(14);
                        coordinates2.add(tm.toLatLng());
                    }
                }

                pathCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 모든 경로의 좌표를 저장할 리스트
                        List<List<LatLng>> allPaths = new ArrayList<>();

                        for(int i = 0; i < mData2.size() - 1; i++){
                            TravelSpot2 currentSpot = mData2.get(i);
                            TravelSpot2 nextSpot = mData2.get(i + 1);

                            String startLat = currentSpot.getLat();
                            String startLon = currentSpot.getLon();
                            String nextLat = nextSpot.getLat();
                            String nextLon = nextSpot.getLon();
                            getDirectionPath(startLon, startLat, nextLon, nextLat, new SearchCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    // 응답을 파싱하여 좌표 리스트에 추가
                                    List<LatLng> pathCoordinates = parsePathCoordinatesFromJson(result);
                                    allPaths.add(pathCoordinates);

                                    // 모든 경로 정보를 저장한 후 처리할 작업 수행
                                    if (allPaths.size() == mData2.size() - 1) {
                                        // 모든 경로 정보가 저장되었으므로 여기에서 처리 작업을 수행
                                        // allPaths 리스트에는 모든 경로의 좌표가 저장되어 있음
                                        processAllPaths(allPaths);
                                    }

                                    // 모든 경로를 표시하기 위한 반복문
                                    for (List<LatLng> coordinates : allPaths) {
                                        // path 객체 생성
                                        PathOverlay path = new PathOverlay();

                                        // LatLng 배열을 PathOverlay 객체에 설정
                                        path.setCoords(coordinates);

                                        path.setPatternImage(OverlayImage.fromResource(R.drawable.ic_baseline_navigation_24));
                                        path.setPatternInterval(25);

                                        // 선의 스타일 및 이미지 설정
                                        path.setColor(Color.GREEN); // 초록색으로 설정
                                        path.setWidth(12);

                                        // 지도에 선 표시
                                        path.setMap(naverMap2);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // 실패 시 처리
                                }
                            });
                        }
                    }
                });

                break;
            case "spot3":
                String sname4 = db.dataDao().findNameByTitle("spot3");
                table_text.setText(sname4);

                TravelDao3 travelDao3 = db.table4();
                mData3 = travelDao3.findAll();

                CustomListView3 ListAdapter4 = new CustomListView3(mData3);
                scheduleView.setAdapter(ListAdapter4);
                ListAdapter4.notifyDataSetChanged();

                PathOverlay path3 = new PathOverlay();
                List<LatLng> coordinates3 = new ArrayList<>();

                for(TravelSpot3 spot3 : mData3){
                    StringTokenizer st = new StringTokenizer(spot3.getLat(), ".");
                    if(st.countTokens() == 2){ // 공공데이터로 파싱한 위도 경도
                        LatLng latLng = new LatLng(Double.parseDouble(spot3.getLat()), Double.parseDouble(spot3.getLon()));
                        Log.d("위도경도", spot3.getLat() +" "+ spot3.getLon());
                        Marker marker = new Marker();
                        marker.setPosition(latLng);
                        marker.setIconTintColor(Color.RED);
                        marker.setCaptionText(spot3.getTitle());
                        marker.setCaptionAligns(Align.Top);
                        marker.setCaptionColor(Color.BLACK);
                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                        marker.setCaptionTextSize(14);
                        marker.setMap(naverMap2);
                        coordinates3.add(latLng);
                    } else{ // 네이버 검색 API 통한 위도 경도
                        Tm128 tm = new Tm128(Double.parseDouble(spot3.getLon()), Double.parseDouble(spot3.getLat()));
                        Log.d("위도경도", spot3.getLat() +" "+ spot3.getLon());
                        Marker marker = new Marker();
                        marker.setPosition(tm.toLatLng());
                        marker.setMap(naverMap2);
                        marker.setIconTintColor(Color.RED);
                        marker.setCaptionText(spot3.getTitle());
                        marker.setCaptionAligns(Align.Top);
                        marker.setCaptionColor(Color.BLACK);
                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                        marker.setCaptionTextSize(14);
                        coordinates3.add(tm.toLatLng());
                    }
                }

                pathCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 모든 경로의 좌표를 저장할 리스트
                        List<List<LatLng>> allPaths = new ArrayList<>();

                        for(int i = 0; i < mData3.size() - 1; i++){
                            TravelSpot3 currentSpot = mData3.get(i);
                            TravelSpot3 nextSpot = mData3.get(i + 1);

                            String startLat = currentSpot.getLat();
                            String startLon = currentSpot.getLon();
                            String nextLat = nextSpot.getLat();
                            String nextLon = nextSpot.getLon();
                            getDirectionPath(startLon, startLat, nextLon, nextLat, new SearchCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    // 응답을 파싱하여 좌표 리스트에 추가
                                    List<LatLng> pathCoordinates = parsePathCoordinatesFromJson(result);
                                    allPaths.add(pathCoordinates);

                                    // 모든 경로 정보를 저장한 후 처리할 작업 수행
                                    if (allPaths.size() == mData3.size() - 1) {
                                        // 모든 경로 정보가 저장되었으므로 여기에서 처리 작업을 수행
                                        // allPaths 리스트에는 모든 경로의 좌표가 저장되어 있음
                                        processAllPaths(allPaths);
                                    }

                                    // 모든 경로를 표시하기 위한 반복문
                                    for (List<LatLng> coordinates : allPaths) {
                                        // path 객체 생성
                                        PathOverlay path = new PathOverlay();

                                        // LatLng 배열을 PathOverlay 객체에 설정
                                        path.setCoords(coordinates);

                                        path.setPatternImage(OverlayImage.fromResource(R.drawable.ic_baseline_navigation_24));
                                        path.setPatternInterval(25);

                                        // 선의 스타일 및 이미지 설정
                                        path.setColor(Color.GREEN); // 초록색으로 설정
                                        path.setWidth(12);

                                        // 지도에 선 표시
                                        path.setMap(naverMap2);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // 실패 시 처리
                                }
                            });
                        }
                    }
                });

                break;
            case "spot4":
                String sname5 = db.dataDao().findNameByTitle("spot4");
                table_text.setText(sname5);

                TravelDao4 travelDao4 = db.table5();
                mData4 = travelDao4.findAll();

                CustomListView4 ListAdapter5 = new CustomListView4(mData4);
                scheduleView.setAdapter(ListAdapter5);
                ListAdapter5.notifyDataSetChanged();

                PathOverlay path4 = new PathOverlay();
                List<LatLng> coordinates4 = new ArrayList<>();

                for(TravelSpot4 spot4 : mData4){
                    StringTokenizer st = new StringTokenizer(spot4.getLat(), ".");
                    if(st.countTokens() == 2){ // 공공데이터로 파싱한 위도 경도
                        LatLng latLng = new LatLng(Double.parseDouble(spot4.getLat()), Double.parseDouble(spot4.getLon()));
                        Log.d("위도경도", spot4.getLat() +" "+ spot4.getLon());
                        Marker marker = new Marker();
                        marker.setPosition(latLng);
                        marker.setIconTintColor(Color.RED);
                        marker.setCaptionText(spot4.getTitle());
                        marker.setCaptionAligns(Align.Top);
                        marker.setCaptionColor(Color.BLACK);
                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                        marker.setCaptionTextSize(14);
                        marker.setMap(naverMap2);
                        coordinates4.add(latLng);
                    } else{ // 네이버 검색 API 통한 위도 경도
                        Tm128 tm = new Tm128(Double.parseDouble(spot4.getLon()), Double.parseDouble(spot4.getLat()));
                        Log.d("위도경도", spot4.getLat() +" "+ spot4.getLon());
                        Marker marker = new Marker();
                        marker.setPosition(tm.toLatLng());
                        marker.setMap(naverMap2);
                        marker.setIconTintColor(Color.RED);
                        marker.setCaptionText(spot4.getTitle());
                        marker.setCaptionAligns(Align.Top);
                        marker.setCaptionColor(Color.BLACK);
                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                        marker.setCaptionTextSize(14);
                        coordinates4.add(tm.toLatLng());
                    }
                }

                pathCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 모든 경로의 좌표를 저장할 리스트
                        List<List<LatLng>> allPaths = new ArrayList<>();

                        for(int i = 0; i < mData4.size() - 1; i++){
                            TravelSpot4 currentSpot = mData4.get(i);
                            TravelSpot4 nextSpot = mData4.get(i + 1);

                            String startLat = currentSpot.getLat();
                            String startLon = currentSpot.getLon();
                            String nextLat = nextSpot.getLat();
                            String nextLon = nextSpot.getLon();
                            getDirectionPath(startLon, startLat, nextLon, nextLat, new SearchCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    // 응답을 파싱하여 좌표 리스트에 추가
                                    List<LatLng> pathCoordinates = parsePathCoordinatesFromJson(result);
                                    allPaths.add(pathCoordinates);

                                    // 모든 경로 정보를 저장한 후 처리할 작업 수행
                                    if (allPaths.size() == mData4.size() - 1) {
                                        // 모든 경로 정보가 저장되었으므로 여기에서 처리 작업을 수행
                                        // allPaths 리스트에는 모든 경로의 좌표가 저장되어 있음
                                        processAllPaths(allPaths);
                                    }

                                    // 모든 경로를 표시하기 위한 반복문
                                    for (List<LatLng> coordinates : allPaths) {
                                        // path 객체 생성
                                        PathOverlay path = new PathOverlay();

                                        // LatLng 배열을 PathOverlay 객체에 설정
                                        path.setCoords(coordinates);

                                        path.setPatternImage(OverlayImage.fromResource(R.drawable.ic_baseline_navigation_24));
                                        path.setPatternInterval(25);

                                        // 선의 스타일 및 이미지 설정
                                        path.setColor(Color.GREEN); // 초록색으로 설정
                                        path.setWidth(12);

                                        // 지도에 선 표시
                                        path.setMap(naverMap2);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // 실패 시 처리
                                }
                            });
                        }
                    }
                });

                break;
        }


        LatLng initialPosition = new LatLng(35.2280, 128.6818);
        //CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
        //naverMap.moveCamera(cameraUpdate);//카메라 초기 위치 설정

        naverMap.setLocationSource(locationSource); // 위치추적 기능
        ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);


        UiSettings uiSettings = naverMap.getUiSettings(); // 현재 위치 버튼
        uiSettings.setLocationButtonEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setZoomControlEnabled(false);
        uiSettings.setScaleBarEnabled(false);


        CameraUpdate cameraUpdate1 = CameraUpdate.scrollAndZoomTo(initialPosition, 28).animate(CameraAnimation.Fly, 3000);
        naverMap.moveCamera(cameraUpdate1);

        naverMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                CameraUpdate cameraUpdate2 = CameraUpdate.scrollTo(new LatLng(latLng.latitude, latLng.longitude)).animate(CameraAnimation.Fly, 3000);
                naverMap.moveCamera(cameraUpdate2);

            }

        });  // 지도상 클릭한 위치로 카메라 이동하는 클릭리스너
    }

    public class CustomListView extends BaseAdapter{

        public CustomListView(List<TravelSpot> data) {
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_listview, parent, false);
            }

            TextView textView = convertView.findViewById(R.id.title);
            textView.setText(mData.get(position).getName());
            TextView textView2 = convertView.findViewById(R.id.address);
            textView2.setText(mData.get(position).getAddr());
            TextView number = convertView.findViewById(R.id.number);
            number.setText(Integer.toString(position+1));

            ImageView imageView = convertView.findViewById(R.id.bt_delete);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TravelSpot travelSpot = (TravelSpot) getItem(position);
                    String delete_data = travelSpot.getName();
                    Log.d("삭제할 장소", delete_data);
                    db.table1().deleteTravelSpotByName(delete_data);
                    mData.remove(position);
                    notifyDataSetChanged();

                    recreate();
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 클릭된 항목의 TravelSpot 객체 가져오기
                    TravelSpot travelSpot = (TravelSpot) getItem(position);

                    // 클릭된 항목의 위도와 경도 정보 가져오기
                    String latitude = travelSpot.getLat();
                    String longitude = travelSpot.getLon();

                    StringTokenizer st = new StringTokenizer(latitude, ".");
                    if(st.countTokens() == 2){
                        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), 16).animate(CameraAnimation.Fly, 3000);
                        naverMap2.moveCamera(cameraUpdate);
                    } else {
                        Tm128 tm = new Tm128(Double.parseDouble(longitude), Double.parseDouble(latitude));
                        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(tm.toLatLng(), 16).animate(CameraAnimation.Fly, 3000);
                        naverMap2.moveCamera(cameraUpdate);
                    }
                }
            });

            return convertView;
        }
    }

    public class CustomListView1 extends BaseAdapter{

        public CustomListView1(List<TravelSpot1> data) {
            mData1 = data;
        }

        @Override
        public int getCount() {
            return mData1.size();
        }

        @Override
        public Object getItem(int position) {
            return mData1.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_listview, parent, false);
            }

            TextView textView = convertView.findViewById(R.id.title);
            textView.setText(mData1.get(position).getTitle());
            TextView textView2 = convertView.findViewById(R.id.address);
            textView2.setText(mData1.get(position).getAddr());
            TextView number = convertView.findViewById(R.id.number);
            number.setText(Integer.toString(position+1));

            ImageView imageView = convertView.findViewById(R.id.bt_delete);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TravelSpot1 travelSpot1 = (TravelSpot1) getItem(position);
                    String delete_data = travelSpot1.getTitle();
                    Log.d("삭제할 장소", delete_data);
                    db.table2().deleteTravelSpotByName(delete_data);
                    mData1.remove(position);
                    notifyDataSetChanged();

                    recreate();
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 클릭된 항목의 TravelSpot 객체 가져오기
                    TravelSpot1 travelSpot1 = (TravelSpot1) getItem(position);

                    // 클릭된 항목의 위도와 경도 정보 가져오기
                    String latitude = travelSpot1.getLat();
                    String longitude = travelSpot1.getLon();

                    StringTokenizer st = new StringTokenizer(latitude, ".");
                    if(st.countTokens() == 2){
                        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), 16).animate(CameraAnimation.Fly, 3000);
                        naverMap2.moveCamera(cameraUpdate);
                    } else {
                        Tm128 tm = new Tm128(Double.parseDouble(longitude), Double.parseDouble(latitude));
                        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(tm.toLatLng(), 16).animate(CameraAnimation.Fly, 3000);
                        naverMap2.moveCamera(cameraUpdate);
                    }
                }
            });

            return convertView;
        }
    }

    public class CustomListView2 extends BaseAdapter{

        public CustomListView2(List<TravelSpot2> data) {
            mData2 = data;
        }

        @Override
        public int getCount() {
            return mData2.size();
        }

        @Override
        public Object getItem(int position) {
            return mData2.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_listview, parent, false);
            }

            TextView textView = convertView.findViewById(R.id.title);
            textView.setText(mData2.get(position).getTitle());
            TextView textView2 = convertView.findViewById(R.id.address);
            textView2.setText(mData2.get(position).getAddr());
            TextView number = convertView.findViewById(R.id.number);
            number.setText(Integer.toString(position+1));

            ImageView imageView = convertView.findViewById(R.id.bt_delete);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TravelSpot2 travelSpot2 = (TravelSpot2) getItem(position);
                    String delete_data = travelSpot2.getTitle();
                    Log.d("삭제할 장소", delete_data);
                    db.table3().deleteTravelSpotByName(delete_data);
                    mData2.remove(position);
                    notifyDataSetChanged();

                    recreate();
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 클릭된 항목의 TravelSpot 객체 가져오기
                    TravelSpot2 travelSpot2 = (TravelSpot2) getItem(position);

                    // 클릭된 항목의 위도와 경도 정보 가져오기
                    String latitude = travelSpot2.getLat();
                    String longitude = travelSpot2.getLon();

                    StringTokenizer st = new StringTokenizer(latitude, ".");
                    if(st.countTokens() == 2){
                        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), 16).animate(CameraAnimation.Fly, 3000);
                        naverMap2.moveCamera(cameraUpdate);
                    } else {
                        Tm128 tm = new Tm128(Double.parseDouble(longitude), Double.parseDouble(latitude));
                        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(tm.toLatLng(), 16).animate(CameraAnimation.Fly, 3000);
                        naverMap2.moveCamera(cameraUpdate);
                    }
                }
            });

            return convertView;
        }
    }

    public class CustomListView3 extends BaseAdapter{

        public CustomListView3(List<TravelSpot3> data) {
            mData3 = data;
        }

        @Override
        public int getCount() {
            return mData3.size();
        }

        @Override
        public Object getItem(int position) {
            return mData3.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_listview, parent, false);
            }

            TextView textView = convertView.findViewById(R.id.title);
            textView.setText(mData3.get(position).getTitle());
            TextView textView2 = convertView.findViewById(R.id.address);
            textView2.setText(mData3.get(position).getAddr());
            TextView number = convertView.findViewById(R.id.number);
            number.setText(Integer.toString(position+1));

            ImageView imageView = convertView.findViewById(R.id.bt_delete);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TravelSpot3 travelSpot3 = (TravelSpot3) getItem(position);
                    String delete_data = travelSpot3.getTitle();
                    Log.d("삭제할 장소", delete_data);
                    db.table4().deleteTravelSpotByName(delete_data);
                    mData3.remove(position);
                    notifyDataSetChanged();

                    recreate();
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 클릭된 항목의 TravelSpot 객체 가져오기
                    TravelSpot3 travelSpot3 = (TravelSpot3) getItem(position);

                    // 클릭된 항목의 위도와 경도 정보 가져오기
                    String latitude = travelSpot3.getLat();
                    String longitude = travelSpot3.getLon();

                    StringTokenizer st = new StringTokenizer(latitude, ".");
                    if(st.countTokens() == 2){
                        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), 16).animate(CameraAnimation.Fly, 3000);
                        naverMap2.moveCamera(cameraUpdate);
                    } else {
                        Tm128 tm = new Tm128(Double.parseDouble(longitude), Double.parseDouble(latitude));
                        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(tm.toLatLng(), 16).animate(CameraAnimation.Fly, 3000);
                        naverMap2.moveCamera(cameraUpdate);
                    }
                }
            });

            return convertView;
        }
    }

    public class CustomListView4 extends BaseAdapter{

        public CustomListView4(List<TravelSpot4> data) {
            mData4 = data;
        }

        @Override
        public int getCount() {
            return mData4.size();
        }

        @Override
        public Object getItem(int position) {
            return mData4.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_listview, parent, false);
            }

            TextView textView = convertView.findViewById(R.id.title);
            textView.setText(mData4.get(position).getTitle());
            TextView textView2 = convertView.findViewById(R.id.address);
            textView2.setText(mData4.get(position).getAddr());
            TextView number = convertView.findViewById(R.id.number);
            number.setText(Integer.toString(position+1));

            ImageView imageView = convertView.findViewById(R.id.bt_delete);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TravelSpot4 travelSpot4 = (TravelSpot4) getItem(position);
                    String delete_data = travelSpot4.getTitle();
                    Log.d("삭제할 장소", delete_data);
                    db.table5().deleteTravelSpotByName(delete_data);
                    mData4.remove(position);
                    notifyDataSetChanged();

                    recreate();
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 클릭된 항목의 TravelSpot 객체 가져오기
                    TravelSpot4 travelSpot4 = (TravelSpot4) getItem(position);

                    // 클릭된 항목의 위도와 경도 정보 가져오기
                    String latitude = travelSpot4.getLat();
                    String longitude = travelSpot4.getLon();

                    StringTokenizer st = new StringTokenizer(latitude, ".");
                    if(st.countTokens() == 2){
                        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), 16).animate(CameraAnimation.Fly, 3000);
                        naverMap2.moveCamera(cameraUpdate);
                    } else {
                        Tm128 tm = new Tm128(Double.parseDouble(longitude), Double.parseDouble(latitude));
                        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(tm.toLatLng(), 16).animate(CameraAnimation.Fly, 3000);
                        naverMap2.moveCamera(cameraUpdate);
                    }
                }
            });

            return convertView;
        }
    }

    public String data1 = "ok";
    /*void getDirectionpath(String startlat, String startlon, String goallat, String goallon, SearchCallback callback) {
        directionInterface directionInterface = directionApiClient.getInstance().create(com.example.this_is_changwon.directionInterface.class);
        Call<String> call = directionInterface.getPath(clientId, clientSecret, startlat + "," + startlon, goallat + "," +goallon);
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
    }*/

    /*void getDirectionpath(String startlat, String startlon, String goallat, String goallon, SearchCallback callback) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://naveropenapi.apigw-pub.fin-ntruss.com/map-direction-15/v1/driving?start="+startlat+","+startlon+"&goal="+goallat+","+goallon+"&option=trafast")
             .addConverterFactory(GsonConverterFactory.create())
             .build();

        directionInterface directionInterface = retrofit.create(com.example.this_is_changwon.directionInterface.class);
        Call<ResultPath> callgetPath = directionInterface.getPath(clientId, clientSecret,startlat+","+startlon, goallat+","+goallon);

        callgetPath.enqueue(new Callback<ResultPath>() {
            @Override
            public void onResponse(Call<ResultPath> call, Response<ResultPath> response) {
                List<Result_path> path_cords_list = response.body().getRoute().getTraoptimal();

                PathOverlay path = new PathOverlay();
                List<LatLng> path_container = new ArrayList<>();
                path_container.add(new LatLng(0.1, 0.1));
                for(Result_path path_cords : path_cords_list){
                    for(List<Double> path_cords_xy : path_cords.getPath()) {
                        path_container.add(new LatLng(path_cords_xy.get(1), path_cords_xy.get(0)));
                    }
                }
                path.setCoords(path_container.subList(1, path_container.size()));
                path.setColor(Color.RED);
                path.setMap(naverMap2);
            }

            @Override
            public void onFailure(Call<ResultPath> call, Throwable t) {
                Log.e(TAG, "API 호출 실패: " + t.getMessage());
            }
        });

    }

    public class ResultPath {
        private Result_trackoption route;
        private String message;
        private int code;

        public Result_trackoption getRoute() {
            return route;
        }
    }

    public class Result_trackoption {
        private List<Result_path> traoptimal;

        public List<Result_path> getTraoptimal() {
            return traoptimal;
        }
    }

    public class Result_path {
        private Result_distance summary;
        private List<List<Double>> path;

        public List<List<Double>> getPath() {
            return path;
        }
    }

    public class Result_distance {
        private int distance;
    }

    String getResult(String s) {
        data1 = s;
        Log.e(TAG, "지역변수 저장 성공? : " + data1);

        return data1;
    } */

    private void getDirectionPath(String startLat, String startLon, String goalLat, String goalLon, SearchCallback callback) {
        // Retrofit 초기화
        directionInterface apiService = directionApiClient.getDirectionInterface();

        // API 호출
        Call<ResponseBody> call = apiService.getPath(
                clientId, // 애플리케이션 등록 시 발급받은 client id 값
                clientSecret, // 애플리케이션 등록 시 발급받은 client secret값
                startLat + "," + startLon,
                goalLat + "," + goalLon,
                "trafast" // 옵션 값 (예: trafast, tashort, etc.)
        );

        // 비동기 호출
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int statusCode = response.code();

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String result = response.body().string(); // 응답을 문자열로 변환
                        callback.onSuccess(result); // 콜백 호출하여 결과값 전달
                        Log.d("위도와 경도[성공]", startLat + " " + startLon + " " + goalLat + " " + goalLon);
                        Log.e(TAG, "성공 : " + result);
                    } else {
                        Log.d("위도와 경도[실패]", startLat + " " + startLon + " " + goalLat + " " + goalLon);
                        throw new Exception("응답 실패");
                    }
                } catch (Exception e) {
                    callback.onFailure(e); // 콜백 호출하여 에러 전달
                    Log.e(TAG, "에러 : " + e.getMessage());

                    String errorMessage = response.message(); // 에러 메시지 가져오기
                    Log.d("에러 : " + statusCode, errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });

    }

    // JSON 응답에서 경로 좌표를 추출하여 리스트로 반환하는 함수
    private List<LatLng> parsePathCoordinatesFromJson(String json) {
        List<LatLng> pathCoordinates = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(json);
            JSONArray routes = jsonResponse.getJSONObject("route").getJSONArray("trafast");
            JSONObject firstRoute = routes.getJSONObject(0);
            JSONArray path = firstRoute.getJSONArray("path");

            for (int i = 0; i < path.length(); i++) {
                JSONArray coordinate = path.getJSONArray(i);
                double latitude = coordinate.getDouble(1); // 위도
                double longitude = coordinate.getDouble(0); // 경도
                LatLng latLng = new LatLng(latitude, longitude);
                pathCoordinates.add(latLng);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pathCoordinates;
    }

    // 모든 경로 정보를 처리하는 함수
    private void processAllPaths(List<List<LatLng>> allPaths) {
        // 여기에서 모든 경로 정보를 처리하는 작업을 수행
        // allPaths 리스트에는 모든 경로의 좌표 리스트가 저장되어 있음
    }

}
