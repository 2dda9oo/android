package com.example.this_is_changwon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.Tm128;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Align;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.List;
import java.util.StringTokenizer;

public class SearchResultActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private FusedLocationSource locationSource;
    private NaverMap naverMap3;
    private Geocoder geocoder;

    private Appdatabase db;
    TravelSpot1 ts1;
    TravelSpot2 ts2;
    TravelSpot3 ts3;
    TravelSpot4 ts4;

    String result_lat, result_lon;
    Button schedule;
    WebView webView;

    TextView spotName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        spotName = findViewById(R.id.spot_name);
        webView = findViewById(R.id.webview);
        schedule = findViewById(R.id.plus_schedule);

        db = Appdatabase.getInstance(this);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.navermap3);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.navermap3, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        Intent intent = getIntent();
        String spotname = intent.getStringExtra("name");
        String spotaddr = intent.getStringExtra("addr");
        String spot_mapy = intent.getStringExtra("lat");
        String spot_mapx = intent.getStringExtra("lon");
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://search.naver.com/search.naver?query="+ spotaddr + spotname);

        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dl = new Dialog(SearchResultActivity.this);
                dl.setContentView(R.layout.activity_custom_dialog);
                List<Data> dataList = db.dataDao().getAll();

                LinearLayout layout = (LinearLayout) dl.findViewById(R.id.dynamic_space);
                LinearLayout layout_below = (LinearLayout) dl.findViewById(R.id.btn_space);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, // width
                        LinearLayout.LayoutParams.WRAP_CONTENT // height
                );
                params.weight = 1f;
                RadioGroup radioGroup = new RadioGroup(SearchResultActivity.this);
                radioGroup.setOrientation(RadioGroup.VERTICAL);
                Button addbtn = new Button(SearchResultActivity.this);
                addbtn.setText("일정 추가");
                addbtn.setLayoutParams(params);
                Button newbtn = new Button(SearchResultActivity.this);
                newbtn.setText("새 일정");
                newbtn.setLayoutParams(params);
                layout_below.addView(addbtn);
                layout_below.addView(newbtn);

                for(Data data:dataList){
                    RadioButton checkBox = new RadioButton(SearchResultActivity.this);
                    checkBox.setId(data.getId());
                    checkBox.setText(data.getName());
                    radioGroup.addView(checkBox);
                }
                layout.addView(radioGroup);
                dl.show();

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        Log.d("라디오버튼 ID", String.valueOf(i));
                        String click_title = db.dataDao().getTitleById(i);
                        String click_name = db.dataDao().findById(i);
                        Log.d("일정 테이블", click_title);
                        addbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                switch (click_title){
                                    case "spot0":
                                        TravelSpot ts0 = new TravelSpot(spotname, spotaddr, spot_mapx, spot_mapy);
                                        db.table1().insert(ts0);
                                        Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                        dl.dismiss();
                                        Log.d("저장된 장소", ts0.getName());
                                        break;
                                    case "spot1":
                                        ts1 = new TravelSpot1(spotname, spotaddr, spot_mapx, spot_mapy);
                                        db.table2().insert1(ts1);
                                        Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                        dl.dismiss();
                                        break;
                                    case "spot2":
                                        ts2 = new TravelSpot2(spotname, spotaddr, spot_mapx, spot_mapy);
                                        db.table3().insert1(ts2);
                                        Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                        dl.dismiss();
                                        break;
                                    case "spot3":
                                        ts3 = new TravelSpot3(spotname, spotaddr, spot_mapx, spot_mapy);
                                        db.table4().insert1(ts3);
                                        Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                        dl.dismiss();
                                        break;
                                    case "spot4":
                                        ts4 = new TravelSpot4(spotname, spotaddr, spot_mapx, spot_mapy);
                                        db.table5().insert1(ts4);
                                        Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                        dl.dismiss();
                                        break;
                                }

                            }
                        });
                    }
                });
                newbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(SearchResultActivity.this, rootManage.class);
                        startActivity(intent);
                        dl.dismiss();

                    }
                });
            }
        });
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) {
                naverMap3.setLocationTrackingMode(LocationTrackingMode.None);
                return;
            } else {
                naverMap3.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public void onMapReady(@NonNull NaverMap naverMap3) {
        this.naverMap3 = naverMap3;

        Intent intent = getIntent();
        String spotname = intent.getStringExtra("name");
        result_lat = intent.getStringExtra("lat");
        result_lon = intent.getStringExtra("lon");

        spotName.setText(spotname); //클릭한 장소의 이름 가져오기

        StringTokenizer st = new StringTokenizer(result_lat, ".");
        if(st.countTokens() == 2){
            Marker marker = new Marker();
            LatLng latLng = new LatLng(Double.parseDouble(result_lat), Double.parseDouble(result_lon));
            marker.setPosition(latLng);
            marker.setIconTintColor(Color.GREEN);
            marker.setCaptionText(spotname);
            marker.setCaptionColor(Color.BLACK);
            marker.setCaptionAligns(Align.Top);
            marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
            marker.setCaptionTextSize(14);
            marker.setMap(naverMap3);

            LatLng initialPosition = new LatLng(Double.parseDouble(result_lat), Double.parseDouble(result_lon));
            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
            naverMap3.moveCamera(cameraUpdate);//카메라 초기 위치 설정
        } else {
            LatLng latLng = new LatLng(Double.parseDouble(result_lat), Double.parseDouble(result_lon));
            Log.d("위도경도", result_lon +" "+ result_lat);
            Marker marker = new Marker();
            marker.setPosition(latLng);
            marker.setIconTintColor(Color.GREEN);
            marker.setCaptionText(spotname);
            marker.setCaptionColor(Color.BLACK);
            marker.setCaptionAligns(Align.Top);
            marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
            marker.setCaptionTextSize(14);
            marker.setMap(naverMap3);

            LatLng initialPosition = new LatLng(Double.parseDouble(result_lat), Double.parseDouble(result_lon));
            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
            naverMap3.moveCamera(cameraUpdate);//카메라 초기 위치 설정
        }





        UiSettings uiSettings = naverMap3.getUiSettings(); // 현재 위치 버튼
        uiSettings.setLocationButtonEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setZoomControlEnabled(false);
        uiSettings.setScaleBarEnabled(false);


        naverMap3.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                CameraUpdate cameraUpdate2 = CameraUpdate.scrollTo(new LatLng(latLng.latitude, latLng.longitude)).animate(CameraAnimation.Fly);
                naverMap3.moveCamera(cameraUpdate2);

            }
        });  // 지도상 클릭한 위치로 카메라 이동하는 클릭리스너

    }

}