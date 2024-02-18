package com.example.this_is_changwon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.navigation.NavigationView;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.Tm128;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Align;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;
import com.naver.maps.map.widget.LocationButtonView;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okio.Utf8;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final String TAG = this.getClass().getSimpleName();

    private String clientId = "5MB5dzIWO9Iw2DT3rWKs";
    private String clientSecret = "4lzPNQjrlu";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private Geocoder geocoder;

    private Appdatabase db;


    TravelSpot1 ts1;
    TravelSpot2 ts2;
    TravelSpot3 ts3;
    TravelSpot4 ts4;


    Button foodButton, cafeButton, busButton, rootAdd, busFind, cycleFind, cycleButton;
    SearchView searchbar;
    ImageView imageView;
    LinearLayout info_Layout, root_view;
    TextView getMapInfoName, getMapInfoAddr, getCloseTime, getOpenState;
    Toolbar toolBar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView infoClose;


    static String selected_comb;
    String restaurant, search_data, cafe, busstop, stationcontent, data0, data2, ImageUrl, nubiza, arriveBus;
    StringTokenizer search_result, stfood, stcafe, stbusstop, ststation, stnubiza, cutting_stfood, cutting_stcafe, cutting_stbusstop, cutting_nubiza, starriebus, cutting_starrive;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        foodButton = (Button) findViewById(R.id.food_spot);
        cafeButton = (Button) findViewById(R.id.cafe_spot);
        busButton = (Button) findViewById(R.id.bus_spot);
        rootAdd = (Button) findViewById(R.id.root_add);
        busFind = (Button) findViewById(R.id.bus_search);
        cycleFind = (Button) findViewById(R.id.cycle_spot);
        cycleButton = (Button) findViewById(R.id.cycle_search);
        imageView = (ImageView) findViewById(R.id.search_image);
        infoClose = (ImageView) findViewById(R.id.info_close);

        searchbar = (SearchView) findViewById(R.id.search_bar);
        info_Layout = (LinearLayout) findViewById(R.id.info_layout);
        getMapInfoName = (TextView) findViewById(R.id.info_title);
        getMapInfoAddr = (TextView) findViewById(R.id.info_addr);
        getCloseTime = (TextView) findViewById(R.id.open_close_time);
        getOpenState = (TextView) findViewById(R.id.state_open);

        toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_dehaze_24);

        db = Appdatabase.getInstance(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView)findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.item_recommand:
                        LatLng currentPosition = getCurrentPosition(naverMap);
                        String latitude = String.valueOf(currentPosition.latitude);
                        String longitude = String.valueOf(currentPosition.longitude);

                        Intent intent = new Intent(MainActivity.this, commendActivity.class);
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                        startActivity(intent);
                        return true;
                    case R.id.item_info:
                        Intent intent2 = new Intent(MainActivity.this, rootManage.class);
                        startActivity(intent2);
                        return true;
                    case R.id.item_searchInfo:
                        db.searchDao().deleteAllTravelSpots();
                        Intent intent3 = new Intent(MainActivity.this, SearchActivity.class);
                        startActivity(intent3);
                        return true;

                }
                return false;
            }
        });



        Spinner combobox = (Spinner) findViewById(R.id.comboBox);
        ArrayAdapter comb_adapter = ArrayAdapter.createFromResource(this, R.array.local_name, android.R.layout.simple_spinner_item);
        comb_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        combobox.setAdapter(comb_adapter);
        combobox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String str = (String) combobox.getSelectedItem();
                selected_comb = str;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        }); //선택된 콤보박스의 항목을 selected_comb에 저장


        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.navermap);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.navermap, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                info_Layout.setVisibility(View.INVISIBLE);
                String spinner_selec = combobox.getSelectedItem().toString();
                if(spinner_selec.equals("지역구")){
                    getSearchResult("창원"+ s, new SearchCallback() {
                        @Override
                        public void onSuccess(String result) {
                            data0 = result;
                            Log.e(TAG, "data0 : " + data0);

                            search_data = parseXML(data0);
                            Log.e(TAG, "파싱 성공 : " + search_data);

                            if(search_data.equals("")){
                                Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                            }else{
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int count = 0;
                                        int num1 = 0;
                                        int num2 = 0;
                                        int num3 = 0;
                                        int num4 = 0;

                                        StringTokenizer stBuffer = new StringTokenizer(search_data,"*");
                                        String title[] = new String[stBuffer.countTokens()];
                                        String address[] = new String[stBuffer.countTokens()];
                                        String mapx[] = new String[stBuffer.countTokens()];
                                        String mapy[] = new String[stBuffer.countTokens()];
                                        while(stBuffer.hasMoreTokens()){
                                            String cutting = stBuffer.nextToken();
                                            search_result = new StringTokenizer(cutting,"@");
                                            while(search_result.hasMoreTokens()){
                                                if(count % 4 == 0){
                                                    title[num1] = search_result.nextToken();
                                                    num1++;
                                                    count++;
                                                } else if(count % 4 == 1){
                                                    address[num2] = search_result.nextToken();
                                                    num2++;
                                                    count++;
                                                }else if(count % 4 == 2){
                                                    mapx[num3] = search_result.nextToken();
                                                    mapx[num3] = mapx[num3].substring(0, 3) + "." + mapx[num3].substring(3);
                                                    Log.d("위도", "결과값: " + mapx[num3]);
                                                    num3++;
                                                    count++;
                                                }else if(count % 4 == 3){
                                                    mapy[num4] = search_result.nextToken();
                                                    mapy[num4] = mapy[num4].substring(0, 2) + "." + mapy[num4].substring(2);
                                                    Log.d("경도", "결과값: " + mapy[num4]);
                                                    num4++;
                                                    count++;
                                                }
                                            }
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                freemMarkers();
                                                freebusMarkers();
                                                freecycleMarkers();
                                                for(int i = 0; i < title.length; i++){
                                                    Marker marker = new Marker();
                                                    marker.setIconTintColor(Color.BLUE);
                                                    marker.setPosition(new LatLng(Double.parseDouble(mapy[i]), Double.parseDouble(mapx[i])));
                                                    marker.setMap(naverMap);
                                                    marker.setCaptionText(title[i]);
                                                    marker.setCaptionAligns(Align.Top);
                                                    marker.setCaptionColor(Color.BLACK);
                                                    marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                    marker.setCaptionTextSize(14);
                                                    marker.setHideCollidedSymbols(true);
                                                    mMarkers.add(marker);

                                                    int finalI = i;

                                                    marker.setOnClickListener(new Overlay.OnClickListener() {
                                                        private String close_time;
                                                        private String state_open;
                                                        @Override
                                                        public boolean onClick(@NonNull Overlay overlay) {
                                                            getMapInfoName.setText(title[finalI]);
                                                            getMapInfoAddr.setText(address[finalI]);
                                                            String place = URLEncoder.encode(title[finalI]);
                                                            String url = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%B0%BD%EC%9B%90%20"+place;

                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    org.jsoup.nodes.Document jsoupDoc = null;
                                                                    try {
                                                                        jsoupDoc = Jsoup.connect(url).get();
                                                                    } catch (IOException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    Elements elements = jsoupDoc.select(".place_section_content"); // 영업시간이 포함된 엘리먼트 선택
                                                                    Log.d("elements", "결과값: " + elements);
                                                                    Elements map_page = jsoupDoc.select("#_title");
                                                                    Log.d("지도 페이지: ", map_page.toString());
                                                                    String going_map = map_page.select("a").attr("href");;
                                                                    Log.d("지도 주소: ", going_map);

                                                                    Elements imgpage = jsoupDoc.select("div.K0PDV._div#ibu_1");
                                                                    Log.d("이미지 url 포함한 부분 : ", imgpage.toString());


                                                                    getImageSearchResult("창원" + title[finalI], new SearchCallback() {
                                                                        @Override
                                                                        public void onSuccess(String result) {
                                                                            Log.d("성공?: ", result);
                                                                            ImageUrl = ImageparseXML(result);
                                                                            Log.d("link : ", ImageUrl);
                                                                            if(ImageUrl.equals("")){
                                                                                imageView.setImageResource(R.drawable.nonplace);
                                                                            } else {
                                                                                String imageUrl = ImageUrl;
                                                                                new LoadImageTask().execute(imageUrl);
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onFailure(Exception e) {

                                                                        }
                                                                    });

                                                                    Elements open_state = elements.select(".A_cdD");
                                                                    Log.d("제발: ", open_state.toString());
                                                                    Elements time_close = elements.select(".U7pYf");
                                                                    Log.d("제발: ", time_close.toString());
                                                                    String openstate = open_state.select("em").text();
                                                                    Log.d("영업여부 : ", openstate);
                                                                    String closingTime = time_close.select("span.place_blind").text();
                                                                    Log.d("영업종료 시간은? : ", closingTime);
                                                                    close_time = closingTime;
                                                                    state_open = openstate;

                                                                    getCloseTime.setText(close_time);
                                                                    getOpenState.setText(state_open);

                                                                }
                                                            }).start();
                                                            getCloseTime.setText(close_time);
                                                            getOpenState.setText(state_open);
                                                            info_Layout.setVisibility(View.VISIBLE);

                                                            infoClose.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    info_Layout.setVisibility(View.INVISIBLE);
                                                                }
                                                            });

                                            /*
                                            new Thread(){
                                                @Override
                                                public void run() {
                                                    try{


                                                        //String openstate = time_open.attr(".place_blind");
                                                        //Log.d("영업상태: ", openstate);

                                                        //Elements placeThumbs = elements.select(".place_thumb");

                                                        //String thumbUrl = placeThumbs.get(0).attr("href");
                                                        //Log.d("제발: ", thumbUrl);

                                                        //Uri uri = Uri.parse(thumbUrl);
                                                        //Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                        //startActivity(intent);

                                                        //Elements placeThumbs = elements.select("._div");
                                                        //Log.d("placeThumbs", "결과값: " + placeThumbs);

                                                        //String thumbUrl = placeThumbs.get(0).attr("href")
                                                                //.replace("background-image:url(", "")
                                                                //.replace(")", "");

                                                        //Log.d("제발: ", thumbUrl);
                                                        String attr = "";
                                                        Log.d("test", "==============================");

                                                        org.jsoup.nodes.Document thumbDoc = Jsoup.connect(thumbUrl).get();
                                                        Log.d("맵 페이지: ", thumbDoc.toString());
                                                        Elements elements2 = thumbDoc.select(".photo_area");
                                                        Log.d("속성값1: ", elements2.text());
                                                        String imgSrc = elements2.attr("src");
                                                        Log.d("이미지: ", imgSrc);


                                                        if (attr != null) {
                                                           // Log.d("elements", "결과값: " + href);
                                                            //org.jsoup.nodes.Element element = elements.first(); // 첫 번째 이미지 선택
                                                            //String imageUrl = element.attr("url"); // 이미지 URL 추출
                                                            //Log.d("image", "imageUrl"+elements.text());

                                                           // URL url1 = new URL(imageUrl);
                                                            //HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
                                                            //connection.setDoInput(true);
                                                           // connection.connect();
                                                           // InputStream inputStream = connection.getInputStream();
                                                           // Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                                          //  imageView.setImageBitmap(bitmap);
                                                            // 이미지 로드 코드
                                                        } else {
                                                            Log.d("test", "================fail==============");
                                                            // 요소를 찾지 못했을 때 처리할 코드
                                                            imageView.setVisibility(View.INVISIBLE);
                                                        }



                                                    }catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }.start();
                                            */

                                                            rootAdd.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {

                                                                    Dialog dl = new Dialog(MainActivity.this);
                                                                    dl.setContentView(R.layout.activity_custom_dialog);
                                                                    List<Data> dataList = db.dataDao().getAll();

                                                                    LinearLayout layout = (LinearLayout) dl.findViewById(R.id.dynamic_space);
                                                                    LinearLayout layout_below = (LinearLayout) dl.findViewById(R.id.btn_space);
                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                            0, // width
                                                                            LinearLayout.LayoutParams.WRAP_CONTENT // height
                                                                    );
                                                                    params.weight = 1f;
                                                                    RadioGroup radioGroup = new RadioGroup(MainActivity.this);
                                                                    radioGroup.setOrientation(RadioGroup.VERTICAL);
                                                                    Button addbtn = new Button(MainActivity.this);
                                                                    addbtn.setText("일정 추가");
                                                                    addbtn.setLayoutParams(params);
                                                                    Button newbtn = new Button(MainActivity.this);
                                                                    newbtn.setText("새 일정");
                                                                    newbtn.setLayoutParams(params);
                                                                    layout_below.addView(addbtn);
                                                                    layout_below.addView(newbtn);

                                                                    for(Data data:dataList){
                                                                        RadioButton checkBox = new RadioButton(MainActivity.this);
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
                                                                                            TravelSpot ts0 = new TravelSpot(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table1().insert(ts0);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            Log.d("저장된 장소", ts0.getName());
                                                                                            break;
                                                                                        case "spot1":
                                                                                            ts1 = new TravelSpot1(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table2().insert1(ts1);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot2":
                                                                                            ts2 = new TravelSpot2(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table3().insert1(ts2);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot3":
                                                                                            ts3 = new TravelSpot3(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table4().insert1(ts3);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot4":
                                                                                            ts4 = new TravelSpot4(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
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
                                                                            Intent intent = new Intent(MainActivity.this, rootManage.class);
                                                                            startActivity(intent);
                                                                            dl.dismiss();

                                                                        }
                                                                    });


                                                                }
                                                            });

                                                            busFind.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    new Thread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, count = 0;
                                                                            busstop = getBus();
                                                                            stbusstop = new StringTokenizer(busstop, "*");

                                                                            String bus_citycode[] = new String[stbusstop.countTokens()];
                                                                            String busstop_mapx[] = new String[stbusstop.countTokens()];
                                                                            String busstop_mapy[] = new String[stbusstop.countTokens()];
                                                                            String busstopId[] = new String[stbusstop.countTokens()];
                                                                            String busstopNm[] = new String[stbusstop.countTokens()];

                                                                            while (stbusstop.hasMoreTokens()) {
                                                                                cutting_stbusstop = new StringTokenizer(stbusstop.nextToken(), "@");
                                                                                while (cutting_stbusstop.hasMoreTokens()) {
                                                                                    if (count % 5 == 0) {
                                                                                        bus_citycode[num1] = cutting_stbusstop.nextToken();
                                                                                        num1++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 1) {
                                                                                        busstop_mapx[num2] = cutting_stbusstop.nextToken();
                                                                                        num2++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 2) {
                                                                                        busstop_mapy[num3] = cutting_stbusstop.nextToken();
                                                                                        num3++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 3) {
                                                                                        busstopId[num4] = cutting_stbusstop.nextToken();
                                                                                        num4++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 4) {
                                                                                        busstopNm[num5] = cutting_stbusstop.nextToken();
                                                                                        num5++;
                                                                                        count++;
                                                                                    }
                                                                                }
                                                                            }
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    freebusMarkers();
                                                                                    for (int i = 0; i < busstopNm.length; i++) {
                                                                                        Marker marker = new Marker();
                                                                                        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_directions_bus_24));
                                                                                        marker.setIconTintColor(Color.BLUE);
                                                                                        marker.setPosition(new LatLng(Double.parseDouble(busstop_mapx[i]), Double.parseDouble(busstop_mapy[i])));
                                                                                        marker.setMap(naverMap);
                                                                                        marker.setCaptionText(busstopNm[i]);
                                                                                        marker.setCaptionAligns(Align.Top);
                                                                                        marker.setCaptionColor(Color.BLACK);
                                                                                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                                                        marker.setCaptionTextSize(14);
                                                                                        marker.setHideCollidedSymbols(true);
                                                                                        busMarkers.add(marker);

                                                                                        int finalI = i;

                                                                                        marker.setOnClickListener(new Overlay.OnClickListener() {
                                                                                            @Override
                                                                                            public boolean onClick(@NonNull Overlay overlay) {
                                                                                                Toast.makeText(getApplicationContext(), "버스 정보를 보여드립니다", Toast.LENGTH_LONG).show();
                                                                                                new Thread(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        stationcontent = getStationContent(bus_citycode[finalI], busstopId[finalI]);
                                                                                                        arriveBus = getArriveBus(bus_citycode[finalI], busstopId[finalI]);
                                                                                                        ststation = new StringTokenizer(stationcontent, "*");
                                                                                                        int num = 0;

                                                                                                        String busnumber[] = new String[ststation.countTokens()];
                                                                                                        while(ststation.hasMoreTokens()){
                                                                                                            busnumber[num] = ststation.nextToken();
                                                                                                            Log.d("버스 종류", busnumber[num]);
                                                                                                            num++;
                                                                                                        }

                                                                                                        int num1 = 0, num2 = 0, num3 = 0, count = 0;
                                                                                                        starriebus = new StringTokenizer(arriveBus, "*");

                                                                                                        String arrtime[] = new String[starriebus.countTokens()];
                                                                                                        String routeno[] = new String[starriebus.countTokens()];
                                                                                                        String routeid[] = new String[starriebus.countTokens()];
                                                                                                        while (starriebus.hasMoreTokens()) {
                                                                                                            cutting_starrive = new StringTokenizer(starriebus.nextToken(), "@");
                                                                                                            while (cutting_starrive.hasMoreTokens()) {
                                                                                                                if (count % 3 == 0) {
                                                                                                                    arrtime[num1] = cutting_starrive.nextToken();
                                                                                                                    num1++;
                                                                                                                    count++;
                                                                                                                } else if (count % 3 == 1) {
                                                                                                                    routeno[num2] = cutting_starrive.nextToken();
                                                                                                                    num2++;
                                                                                                                    count++;
                                                                                                                } else if (count % 3 == 2) {
                                                                                                                    routeid[num3] = cutting_starrive.nextToken();
                                                                                                                    num3++;
                                                                                                                    count++;
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        runOnUiThread(new Runnable() {
                                                                                                            @Override
                                                                                                            public void run() {
                                                                                                                Dialog dl = new Dialog(MainActivity.this);
                                                                                                                dl.setContentView(R.layout.bus_layout);
                                                                                                                LinearLayout verticalLayout = dl.findViewById(R.id.busnumber_space);
                                                                                                                for (String bus : busnumber) {
                                                                                                                    TextView textView = new TextView(MainActivity.this);
                                                                                                                    textView.setText(bus);
                                                                                                                    textView.setTextSize(16);
                                                                                                                    textView.setTextColor(Color.BLACK);
                                                                                                                    verticalLayout.addView(textView);

                                                                                                                    View separator = new View(MainActivity.this);
                                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                                    separator.setLayoutParams(params);
                                                                                                                    verticalLayout.addView(separator);
                                                                                                                }

                                                                                                                LinearLayout verticalLayout2 = dl.findViewById(R.id.arrive_space);

                                                                                                                for (int i = 0; i < arrtime.length - 1; i++) {
                                                                                                                    int minIndex = i;
                                                                                                                    for (int j = i + 1; j < arrtime.length; j++) {
                                                                                                                        int time1 = Integer.parseInt(arrtime[j]);
                                                                                                                        int time2 = Integer.parseInt(arrtime[minIndex]);
                                                                                                                        if (time1 < time2) {
                                                                                                                            minIndex = j;
                                                                                                                        }
                                                                                                                    }

                                                                                                                    // 최소값과 현재 위치의 값을 교환합니다.
                                                                                                                    int tempTime = Integer.parseInt(arrtime[i]);
                                                                                                                    arrtime[i] = arrtime[minIndex];
                                                                                                                    arrtime[minIndex] = Integer.toString(tempTime);

                                                                                                                    String tempNo = routeno[i];
                                                                                                                    routeno[i] = routeno[minIndex];
                                                                                                                    routeno[minIndex] = tempNo;

                                                                                                                    String tempId = routeid[i];
                                                                                                                    routeid[i] = routeid[minIndex];
                                                                                                                    routeid[minIndex] = tempId;
                                                                                                                }

                                                                                                                for (int i = 0; i < arrtime.length; i++) {
                                                                                                                    TextView timeTextView = new TextView(MainActivity.this);
                                                                                                                    int time = Integer.parseInt(arrtime[i]);
                                                                                                                    time = time/60;
                                                                                                                    String times = Integer.toString(time);
                                                                                                                    timeTextView.setText(times+"분");
                                                                                                                    timeTextView.setTextSize(16);
                                                                                                                    timeTextView.setTextColor(Color.BLACK);

                                                                                                                    TextView numberTextView = new TextView(MainActivity.this);
                                                                                                                    numberTextView.setText(routeno[i]);
                                                                                                                    numberTextView.setTextSize(16);
                                                                                                                    numberTextView.setTextColor(Color.BLACK);

                                                                                                                    TextView stTextView = new TextView(MainActivity.this);
                                                                                                                    stTextView.setText(routeid[i]);
                                                                                                                    stTextView.setTextSize(16);
                                                                                                                    stTextView.setTextColor(Color.BLACK);

                                                                                                                    LinearLayout horizontalLayout = new LinearLayout(MainActivity.this);
                                                                                                                    horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                                                                                    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

                                                                                                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                                                                                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                                                                                                    timeTextView.setLayoutParams(layoutParams);
                                                                                                                    numberTextView.setLayoutParams(layoutParams);
                                                                                                                    stTextView.setLayoutParams(layoutParams);

                                                                                                                    horizontalLayout.addView(timeTextView);
                                                                                                                    horizontalLayout.addView(numberTextView);
                                                                                                                    horizontalLayout.addView(stTextView);
                                                                                                                    verticalLayout2.addView(horizontalLayout);

                                                                                                                    View separator = new View(MainActivity.this);
                                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                                    separator.setLayoutParams(params);
                                                                                                                    verticalLayout2.addView(separator);
                                                                                                                }
                                                                                                                dl.show();
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                }).start();

                                                                                                return false;
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }).start();
                                                                }
                                                            });
                                                            return false;
                                                        }
                                                    });

                                                }
                                                LatLng newLatLng = new LatLng(Double.parseDouble(mapy[0]), Double.parseDouble(mapx[0]));
                                                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(newLatLng, 12).animate(CameraAnimation.Fly, 3000);
                                                naverMap.moveCamera(cameraUpdate);
                                            }
                                        });
                                    }
                                }).start();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                        }
                    });
                } else if(spinner_selec.equals("성산구")){
                    getSearchResult("창원시 성산구"+ s, new SearchCallback() {
                        @Override
                        public void onSuccess(String result) {
                            data0 = result;
                            Log.e(TAG, "data0 : " + data0);
                            search_data = parseXML(data0);
                            Log.e(TAG, "파싱 성공 : " + search_data);

                            if(search_data.equals("")){
                                Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                            }else{
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        int count = 0;
                                        int num1 = 0;
                                        int num2 = 0;
                                        int num3 = 0;
                                        int num4 = 0;

                                        StringTokenizer stBuffer = new StringTokenizer(search_data,"*");
                                        String title[] = new String[stBuffer.countTokens()];
                                        String address[] = new String[stBuffer.countTokens()];
                                        String mapx[] = new String[stBuffer.countTokens()];
                                        String mapy[] = new String[stBuffer.countTokens()];
                                        while(stBuffer.hasMoreTokens()){
                                            String cutting = stBuffer.nextToken();
                                            search_result = new StringTokenizer(cutting,"@");
                                            while(search_result.hasMoreTokens()){
                                                if(count % 4 == 0){
                                                    title[num1] = search_result.nextToken();
                                                    num1++;
                                                    count++;
                                                } else if(count % 4 == 1){
                                                    address[num2] = search_result.nextToken();
                                                    num2++;
                                                    count++;
                                                }else if(count % 4 == 2){
                                                    mapx[num3] = search_result.nextToken();
                                                    mapx[num3] = mapx[num3].substring(0, 3) + "." + mapx[num3].substring(3);
                                                    Log.d("위도", "결과값: " + mapx[num3]);
                                                    num3++;
                                                    count++;
                                                }else if(count % 4 == 3){
                                                    mapy[num4] = search_result.nextToken();
                                                    mapy[num4] = mapy[num4].substring(0, 2) + "." + mapy[num4].substring(2);
                                                    Log.d("경도", "결과값: " + mapy[num4]);
                                                    num4++;
                                                    count++;
                                                }
                                            }
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                freemMarkers();
                                                freebusMarkers();
                                                freecycleMarkers();
                                                for(int i = 0; i < title.length; i++){
                                                    Marker marker = new Marker();
                                                    marker.setIconTintColor(Color.BLUE);
                                                    marker.setPosition(new LatLng(Double.parseDouble(mapy[i]), Double.parseDouble(mapx[i])));
                                                    marker.setMap(naverMap);
                                                    marker.setCaptionText(title[i]);
                                                    marker.setCaptionAligns(Align.Top);
                                                    marker.setCaptionColor(Color.BLACK);
                                                    marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                    marker.setCaptionTextSize(14);
                                                    marker.setHideCollidedSymbols(true);
                                                    mMarkers.add(marker);

                                                    int finalI = i;

                                                    marker.setOnClickListener(new Overlay.OnClickListener() {
                                                        private String close_time;
                                                        private String state_open;
                                                        @Override
                                                        public boolean onClick(@NonNull Overlay overlay) {
                                                            getMapInfoName.setText(title[finalI]);
                                                            getMapInfoAddr.setText(address[finalI]);
                                                            String place = URLEncoder.encode(title[finalI]);
                                                            String url = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%B0%BD%EC%9B%90%20"+place;

                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    org.jsoup.nodes.Document jsoupDoc = null;
                                                                    try {
                                                                        jsoupDoc = Jsoup.connect(url).get();
                                                                    } catch (IOException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    Elements elements = jsoupDoc.select(".place_section_content"); // 영업시간이 포함된 엘리먼트 선택
                                                                    Log.d("elements", "결과값: " + elements);
                                                                    Elements map_page = jsoupDoc.select("#_title");
                                                                    Log.d("지도 페이지: ", map_page.toString());
                                                                    String going_map = map_page.select("a").attr("href");;
                                                                    Log.d("지도 주소: ", going_map);

                                                                    Elements imgpage = jsoupDoc.select("div.K0PDV._div#ibu_1");
                                                                    Log.d("이미지 url 포함한 부분 : ", imgpage.toString());


                                                                    getImageSearchResult("창원" + title[finalI], new SearchCallback() {
                                                                        @Override
                                                                        public void onSuccess(String result) {
                                                                            Log.d("성공?: ", result);
                                                                            ImageUrl = ImageparseXML(result);
                                                                            Log.d("link : ", ImageUrl);
                                                                            if(ImageUrl.equals("")){
                                                                                imageView.setImageResource(R.drawable.nonplace);
                                                                            } else {
                                                                                String imageUrl = ImageUrl;
                                                                                new LoadImageTask().execute(imageUrl);
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onFailure(Exception e) {

                                                                        }
                                                                    });

                                                                    Elements open_state = elements.select(".A_cdD");
                                                                    Log.d("제발: ", open_state.toString());
                                                                    Elements time_close = elements.select(".U7pYf");
                                                                    Log.d("제발: ", time_close.toString());
                                                                    String openstate = open_state.select("em").text();
                                                                    Log.d("영업여부 : ", openstate);
                                                                    String closingTime = time_close.select("span.place_blind").text();
                                                                    Log.d("영업종료 시간은? : ", closingTime);
                                                                    close_time = closingTime;
                                                                    state_open = openstate;

                                                                    getCloseTime.setText(close_time);
                                                                    getOpenState.setText(state_open);

                                                                }
                                                            }).start();
                                                            getCloseTime.setText(close_time);
                                                            getOpenState.setText(state_open);
                                                            info_Layout.setVisibility(View.VISIBLE);

                                                            infoClose.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    info_Layout.setVisibility(View.INVISIBLE);
                                                                }
                                                            });

                                                            rootAdd.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {

                                                                    Dialog dl = new Dialog(MainActivity.this);
                                                                    dl.setContentView(R.layout.activity_custom_dialog);
                                                                    List<Data> dataList = db.dataDao().getAll();

                                                                    LinearLayout layout = (LinearLayout) dl.findViewById(R.id.dynamic_space);
                                                                    LinearLayout layout_below = (LinearLayout) dl.findViewById(R.id.btn_space);
                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                            0, // width
                                                                            LinearLayout.LayoutParams.WRAP_CONTENT // height
                                                                    );
                                                                    params.weight = 1f;
                                                                    RadioGroup radioGroup = new RadioGroup(MainActivity.this);
                                                                    radioGroup.setOrientation(RadioGroup.VERTICAL);
                                                                    Button addbtn = new Button(MainActivity.this);
                                                                    addbtn.setText("일정 추가");
                                                                    addbtn.setLayoutParams(params);
                                                                    Button newbtn = new Button(MainActivity.this);
                                                                    newbtn.setText("새 일정");
                                                                    newbtn.setLayoutParams(params);
                                                                    layout_below.addView(addbtn);
                                                                    layout_below.addView(newbtn);

                                                                    for(Data data:dataList){
                                                                        RadioButton checkBox = new RadioButton(MainActivity.this);
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
                                                                                            TravelSpot ts0 = new TravelSpot(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table1().insert(ts0);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            Log.d("저장된 장소", ts0.getName());
                                                                                            break;
                                                                                        case "spot1":
                                                                                            ts1 = new TravelSpot1(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table2().insert1(ts1);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot2":
                                                                                            ts2 = new TravelSpot2(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table3().insert1(ts2);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot3":
                                                                                            ts3 = new TravelSpot3(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table4().insert1(ts3);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot4":
                                                                                            ts4 = new TravelSpot4(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
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
                                                                            Intent intent = new Intent(MainActivity.this, rootManage.class);
                                                                            startActivity(intent);
                                                                            dl.dismiss();

                                                                        }
                                                                    });


                                                                }
                                                            });

                                                            busFind.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    new Thread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, count = 0;
                                                                            busstop = getBus();
                                                                            stbusstop = new StringTokenizer(busstop, "*");

                                                                            String bus_citycode[] = new String[stbusstop.countTokens()];
                                                                            String busstop_mapx[] = new String[stbusstop.countTokens()];
                                                                            String busstop_mapy[] = new String[stbusstop.countTokens()];
                                                                            String busstopId[] = new String[stbusstop.countTokens()];
                                                                            String busstopNm[] = new String[stbusstop.countTokens()];

                                                                            while (stbusstop.hasMoreTokens()) {
                                                                                cutting_stbusstop = new StringTokenizer(stbusstop.nextToken(), "@");
                                                                                while (cutting_stbusstop.hasMoreTokens()) {
                                                                                    if (count % 5 == 0) {
                                                                                        bus_citycode[num1] = cutting_stbusstop.nextToken();
                                                                                        num1++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 1) {
                                                                                        busstop_mapx[num2] = cutting_stbusstop.nextToken();
                                                                                        num2++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 2) {
                                                                                        busstop_mapy[num3] = cutting_stbusstop.nextToken();
                                                                                        num3++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 3) {
                                                                                        busstopId[num4] = cutting_stbusstop.nextToken();
                                                                                        num4++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 4) {
                                                                                        busstopNm[num5] = cutting_stbusstop.nextToken();
                                                                                        num5++;
                                                                                        count++;
                                                                                    }
                                                                                }
                                                                            }
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    freebusMarkers();
                                                                                    for (int i = 0; i < busstopNm.length; i++) {
                                                                                        Marker marker = new Marker();
                                                                                        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_directions_bus_24));
                                                                                        marker.setIconTintColor(Color.BLUE);
                                                                                        marker.setPosition(new LatLng(Double.parseDouble(busstop_mapx[i]), Double.parseDouble(busstop_mapy[i])));
                                                                                        marker.setMap(naverMap);
                                                                                        marker.setCaptionText(busstopNm[i]);
                                                                                        marker.setCaptionAligns(Align.Top);
                                                                                        marker.setCaptionColor(Color.BLACK);
                                                                                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                                                        marker.setCaptionTextSize(14);
                                                                                        marker.setHideCollidedSymbols(true);
                                                                                        busMarkers.add(marker);

                                                                                        int finalI = i;

                                                                                        marker.setOnClickListener(new Overlay.OnClickListener() {
                                                                                            @Override
                                                                                            public boolean onClick(@NonNull Overlay overlay) {
                                                                                                Toast.makeText(getApplicationContext(), "버스 정보를 보여드립니다", Toast.LENGTH_LONG).show();
                                                                                                new Thread(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        stationcontent = getStationContent(bus_citycode[finalI], busstopId[finalI]);
                                                                                                        arriveBus = getArriveBus(bus_citycode[finalI], busstopId[finalI]);
                                                                                                        ststation = new StringTokenizer(stationcontent, "*");
                                                                                                        int num = 0;

                                                                                                        String busnumber[] = new String[ststation.countTokens()];
                                                                                                        while(ststation.hasMoreTokens()){
                                                                                                            busnumber[num] = ststation.nextToken();
                                                                                                            Log.d("버스 종류", busnumber[num]);
                                                                                                            num++;
                                                                                                        }

                                                                                                        int num1 = 0, num2 = 0, num3 = 0, count = 0;
                                                                                                        starriebus = new StringTokenizer(arriveBus, "*");

                                                                                                        String arrtime[] = new String[starriebus.countTokens()];
                                                                                                        String routeno[] = new String[starriebus.countTokens()];
                                                                                                        String routeid[] = new String[starriebus.countTokens()];
                                                                                                        while (starriebus.hasMoreTokens()) {
                                                                                                            cutting_starrive = new StringTokenizer(starriebus.nextToken(), "@");
                                                                                                            while (cutting_starrive.hasMoreTokens()) {
                                                                                                                if (count % 3 == 0) {
                                                                                                                    arrtime[num1] = cutting_starrive.nextToken();
                                                                                                                    num1++;
                                                                                                                    count++;
                                                                                                                } else if (count % 3 == 1) {
                                                                                                                    routeno[num2] = cutting_starrive.nextToken();
                                                                                                                    num2++;
                                                                                                                    count++;
                                                                                                                } else if (count % 3 == 2) {
                                                                                                                    routeid[num3] = cutting_starrive.nextToken();
                                                                                                                    num3++;
                                                                                                                    count++;
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        runOnUiThread(new Runnable() {
                                                                                                            @Override
                                                                                                            public void run() {
                                                                                                                Dialog dl = new Dialog(MainActivity.this);
                                                                                                                dl.setContentView(R.layout.bus_layout);
                                                                                                                LinearLayout verticalLayout = dl.findViewById(R.id.busnumber_space);
                                                                                                                for (String bus : busnumber) {
                                                                                                                    TextView textView = new TextView(MainActivity.this);
                                                                                                                    textView.setText(bus);
                                                                                                                    textView.setTextSize(16);
                                                                                                                    textView.setTextColor(Color.BLACK);
                                                                                                                    verticalLayout.addView(textView);

                                                                                                                    View separator = new View(MainActivity.this);
                                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                                    separator.setLayoutParams(params);
                                                                                                                    verticalLayout.addView(separator);
                                                                                                                }

                                                                                                                LinearLayout verticalLayout2 = dl.findViewById(R.id.arrive_space);

                                                                                                                for (int i = 0; i < arrtime.length - 1; i++) {
                                                                                                                    int minIndex = i;
                                                                                                                    for (int j = i + 1; j < arrtime.length; j++) {
                                                                                                                        int time1 = Integer.parseInt(arrtime[j]);
                                                                                                                        int time2 = Integer.parseInt(arrtime[minIndex]);
                                                                                                                        if (time1 < time2) {
                                                                                                                            minIndex = j;
                                                                                                                        }
                                                                                                                    }

                                                                                                                    // 최소값과 현재 위치의 값을 교환합니다.
                                                                                                                    int tempTime = Integer.parseInt(arrtime[i]);
                                                                                                                    arrtime[i] = arrtime[minIndex];
                                                                                                                    arrtime[minIndex] = Integer.toString(tempTime);

                                                                                                                    String tempNo = routeno[i];
                                                                                                                    routeno[i] = routeno[minIndex];
                                                                                                                    routeno[minIndex] = tempNo;

                                                                                                                    String tempId = routeid[i];
                                                                                                                    routeid[i] = routeid[minIndex];
                                                                                                                    routeid[minIndex] = tempId;
                                                                                                                }

                                                                                                                for (int i = 0; i < arrtime.length; i++) {
                                                                                                                    TextView timeTextView = new TextView(MainActivity.this);
                                                                                                                    int time = Integer.parseInt(arrtime[i]);
                                                                                                                    time = time/60;
                                                                                                                    String times = Integer.toString(time);
                                                                                                                    timeTextView.setText(times+"분");
                                                                                                                    timeTextView.setTextSize(16);
                                                                                                                    timeTextView.setTextColor(Color.BLACK);

                                                                                                                    TextView numberTextView = new TextView(MainActivity.this);
                                                                                                                    numberTextView.setText(routeno[i]);
                                                                                                                    numberTextView.setTextSize(16);
                                                                                                                    numberTextView.setTextColor(Color.BLACK);

                                                                                                                    TextView stTextView = new TextView(MainActivity.this);
                                                                                                                    stTextView.setText(routeid[i]);
                                                                                                                    stTextView.setTextSize(16);
                                                                                                                    stTextView.setTextColor(Color.BLACK);

                                                                                                                    LinearLayout horizontalLayout = new LinearLayout(MainActivity.this);
                                                                                                                    horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                                                                                    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

                                                                                                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                                                                                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                                                                                                    timeTextView.setLayoutParams(layoutParams);
                                                                                                                    numberTextView.setLayoutParams(layoutParams);
                                                                                                                    stTextView.setLayoutParams(layoutParams);

                                                                                                                    horizontalLayout.addView(timeTextView);
                                                                                                                    horizontalLayout.addView(numberTextView);
                                                                                                                    horizontalLayout.addView(stTextView);
                                                                                                                    verticalLayout2.addView(horizontalLayout);

                                                                                                                    View separator = new View(MainActivity.this);
                                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                                    separator.setLayoutParams(params);
                                                                                                                    verticalLayout2.addView(separator);
                                                                                                                }
                                                                                                                dl.show();
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                }).start();

                                                                                                return false;
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }).start();
                                                                }
                                                            });
                                                            return false;
                                                        }
                                                    });

                                                }
                                                LatLng newLatLng = new LatLng(Double.parseDouble(mapy[0]), Double.parseDouble(mapx[0]));
                                                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(newLatLng, 12).animate(CameraAnimation.Fly, 3000);
                                                naverMap.moveCamera(cameraUpdate);
                                            }
                                        });
                                    }
                                }).start();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                        }
                    });
                } else if(spinner_selec.equals("의창구")){
                    getSearchResult("창원시 의창구"+ s, new SearchCallback() {
                        @Override
                        public void onSuccess(String result) {
                            data0 = result;
                            Log.e(TAG, "data0 : " + data0);
                            search_data = parseXML(data0);
                            Log.e(TAG, "파싱 성공 : " + search_data);

                            if(search_data.equals("")){
                                Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                            }else{
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int count = 0;
                                        int num1 = 0;
                                        int num2 = 0;
                                        int num3 = 0;
                                        int num4 = 0;

                                        StringTokenizer stBuffer = new StringTokenizer(search_data,"*");
                                        String title[] = new String[stBuffer.countTokens()];
                                        String address[] = new String[stBuffer.countTokens()];
                                        String mapx[] = new String[stBuffer.countTokens()];
                                        String mapy[] = new String[stBuffer.countTokens()];
                                        while(stBuffer.hasMoreTokens()){
                                            String cutting = stBuffer.nextToken();
                                            search_result = new StringTokenizer(cutting,"@");
                                            while(search_result.hasMoreTokens()){
                                                if(count % 4 == 0){
                                                    title[num1] = search_result.nextToken();
                                                    num1++;
                                                    count++;
                                                } else if(count % 4 == 1){
                                                    address[num2] = search_result.nextToken();
                                                    num2++;
                                                    count++;
                                                }else if(count % 4 == 2){
                                                    mapx[num3] = search_result.nextToken();
                                                    mapx[num3] = mapx[num3].substring(0, 3) + "." + mapx[num3].substring(3);
                                                    Log.d("위도", "결과값: " + mapx[num3]);
                                                    num3++;
                                                    count++;
                                                }else if(count % 4 == 3){
                                                    mapy[num4] = search_result.nextToken();
                                                    mapy[num4] = mapy[num4].substring(0, 2) + "." + mapy[num4].substring(2);
                                                    Log.d("경도", "결과값: " + mapy[num4]);
                                                    num4++;
                                                    count++;
                                                }
                                            }
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                freemMarkers();
                                                freebusMarkers();
                                                freecycleMarkers();
                                                for(int i = 0; i < title.length; i++){
                                                    Marker marker = new Marker();
                                                    marker.setIconTintColor(Color.BLUE);
                                                    marker.setPosition(new LatLng(Double.parseDouble(mapy[i]), Double.parseDouble(mapx[i])));
                                                    marker.setMap(naverMap);
                                                    marker.setCaptionText(title[i]);
                                                    marker.setCaptionAligns(Align.Top);
                                                    marker.setCaptionColor(Color.BLACK);
                                                    marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                    marker.setCaptionTextSize(14);
                                                    marker.setHideCollidedSymbols(true);
                                                    mMarkers.add(marker);

                                                    int finalI = i;

                                                    marker.setOnClickListener(new Overlay.OnClickListener() {
                                                        private String close_time;
                                                        private String state_open;
                                                        @Override
                                                        public boolean onClick(@NonNull Overlay overlay) {
                                                            getMapInfoName.setText(title[finalI]);
                                                            getMapInfoAddr.setText(address[finalI]);
                                                            String place = URLEncoder.encode(title[finalI]);
                                                            String url = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%B0%BD%EC%9B%90%20"+place;

                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    org.jsoup.nodes.Document jsoupDoc = null;
                                                                    try {
                                                                        jsoupDoc = Jsoup.connect(url).get();
                                                                    } catch (IOException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    Elements elements = jsoupDoc.select(".place_section_content"); // 영업시간이 포함된 엘리먼트 선택
                                                                    Log.d("elements", "결과값: " + elements);
                                                                    Elements map_page = jsoupDoc.select("#_title");
                                                                    Log.d("지도 페이지: ", map_page.toString());
                                                                    String going_map = map_page.select("a").attr("href");;
                                                                    Log.d("지도 주소: ", going_map);

                                                                    Elements imgpage = jsoupDoc.select("div.K0PDV._div#ibu_1");
                                                                    Log.d("이미지 url 포함한 부분 : ", imgpage.toString());


                                                                    getImageSearchResult("창원" + title[finalI], new SearchCallback() {
                                                                        @Override
                                                                        public void onSuccess(String result) {
                                                                            Log.d("성공?: ", result);
                                                                            ImageUrl = ImageparseXML(result);
                                                                            Log.d("link : ", ImageUrl);
                                                                            if(ImageUrl.equals("")){
                                                                                imageView.setImageResource(R.drawable.nonplace);
                                                                            } else {
                                                                                String imageUrl = ImageUrl;
                                                                                new LoadImageTask().execute(imageUrl);
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onFailure(Exception e) {

                                                                        }
                                                                    });

                                                                    Elements open_state = elements.select(".A_cdD");
                                                                    Log.d("제발: ", open_state.toString());
                                                                    Elements time_close = elements.select(".U7pYf");
                                                                    Log.d("제발: ", time_close.toString());
                                                                    String openstate = open_state.select("em").text();
                                                                    Log.d("영업여부 : ", openstate);
                                                                    String closingTime = time_close.select("span.place_blind").text();
                                                                    Log.d("영업종료 시간은? : ", closingTime);
                                                                    close_time = closingTime;
                                                                    state_open = openstate;

                                                                    getCloseTime.setText(close_time);
                                                                    getOpenState.setText(state_open);

                                                                }
                                                            }).start();
                                                            getCloseTime.setText(close_time);
                                                            getOpenState.setText(state_open);
                                                            info_Layout.setVisibility(View.VISIBLE);

                                                            infoClose.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    info_Layout.setVisibility(View.INVISIBLE);
                                                                }
                                                            });

                                                            rootAdd.setOnClickListener(new View.OnClickListener() {


                                                                @Override
                                                                public void onClick(View view) {

                                                                    Dialog dl = new Dialog(MainActivity.this);
                                                                    dl.setContentView(R.layout.activity_custom_dialog);
                                                                    List<Data> dataList = db.dataDao().getAll();

                                                                    LinearLayout layout = (LinearLayout) dl.findViewById(R.id.dynamic_space);
                                                                    LinearLayout layout_below = (LinearLayout) dl.findViewById(R.id.btn_space);
                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                            0, // width
                                                                            LinearLayout.LayoutParams.WRAP_CONTENT // height
                                                                    );
                                                                    params.weight = 1f;
                                                                    RadioGroup radioGroup = new RadioGroup(MainActivity.this);
                                                                    radioGroup.setOrientation(RadioGroup.VERTICAL);
                                                                    Button addbtn = new Button(MainActivity.this);
                                                                    addbtn.setText("일정 추가");
                                                                    addbtn.setLayoutParams(params);
                                                                    Button newbtn = new Button(MainActivity.this);
                                                                    newbtn.setText("새 일정");
                                                                    newbtn.setLayoutParams(params);
                                                                    layout_below.addView(addbtn);
                                                                    layout_below.addView(newbtn);

                                                                    for(Data data:dataList){
                                                                        RadioButton checkBox = new RadioButton(MainActivity.this);
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
                                                                                            TravelSpot ts0 = new TravelSpot(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table1().insert(ts0);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            Log.d("저장된 장소", ts0.getName());
                                                                                            break;
                                                                                        case "spot1":
                                                                                            ts1 = new TravelSpot1(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table2().insert1(ts1);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot2":
                                                                                            ts2 = new TravelSpot2(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table3().insert1(ts2);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot3":
                                                                                            ts3 = new TravelSpot3(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table4().insert1(ts3);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot4":
                                                                                            ts4 = new TravelSpot4(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
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
                                                                            Intent intent = new Intent(MainActivity.this, rootManage.class);
                                                                            startActivity(intent);
                                                                            dl.dismiss();

                                                                        }
                                                                    });


                                                                }
                                                            });

                                                            busFind.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    new Thread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, count = 0;
                                                                            busstop = getBus();
                                                                            stbusstop = new StringTokenizer(busstop, "*");

                                                                            String bus_citycode[] = new String[stbusstop.countTokens()];
                                                                            String busstop_mapx[] = new String[stbusstop.countTokens()];
                                                                            String busstop_mapy[] = new String[stbusstop.countTokens()];
                                                                            String busstopId[] = new String[stbusstop.countTokens()];
                                                                            String busstopNm[] = new String[stbusstop.countTokens()];

                                                                            while (stbusstop.hasMoreTokens()) {
                                                                                cutting_stbusstop = new StringTokenizer(stbusstop.nextToken(), "@");
                                                                                while (cutting_stbusstop.hasMoreTokens()) {
                                                                                    if (count % 5 == 0) {
                                                                                        bus_citycode[num1] = cutting_stbusstop.nextToken();
                                                                                        num1++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 1) {
                                                                                        busstop_mapx[num2] = cutting_stbusstop.nextToken();
                                                                                        num2++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 2) {
                                                                                        busstop_mapy[num3] = cutting_stbusstop.nextToken();
                                                                                        num3++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 3) {
                                                                                        busstopId[num4] = cutting_stbusstop.nextToken();
                                                                                        num4++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 4) {
                                                                                        busstopNm[num5] = cutting_stbusstop.nextToken();
                                                                                        num5++;
                                                                                        count++;
                                                                                    }
                                                                                }
                                                                            }
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    freebusMarkers();
                                                                                    for (int i = 0; i < busstopNm.length; i++) {
                                                                                        Marker marker = new Marker();
                                                                                        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_directions_bus_24));
                                                                                        marker.setIconTintColor(Color.BLUE);
                                                                                        marker.setPosition(new LatLng(Double.parseDouble(busstop_mapx[i]), Double.parseDouble(busstop_mapy[i])));
                                                                                        marker.setMap(naverMap);
                                                                                        marker.setCaptionText(busstopNm[i]);
                                                                                        marker.setCaptionAligns(Align.Top);
                                                                                        marker.setCaptionColor(Color.BLACK);
                                                                                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                                                        marker.setCaptionTextSize(14);
                                                                                        marker.setHideCollidedSymbols(true);
                                                                                        busMarkers.add(marker);

                                                                                        int finalI = i;

                                                                                        marker.setOnClickListener(new Overlay.OnClickListener() {
                                                                                            @Override
                                                                                            public boolean onClick(@NonNull Overlay overlay) {
                                                                                                Toast.makeText(getApplicationContext(), "버스 정보를 보여드립니다", Toast.LENGTH_LONG).show();
                                                                                                new Thread(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        stationcontent = getStationContent(bus_citycode[finalI], busstopId[finalI]);
                                                                                                        arriveBus = getArriveBus(bus_citycode[finalI], busstopId[finalI]);
                                                                                                        ststation = new StringTokenizer(stationcontent, "*");
                                                                                                        int num = 0;

                                                                                                        String busnumber[] = new String[ststation.countTokens()];
                                                                                                        while(ststation.hasMoreTokens()){
                                                                                                            busnumber[num] = ststation.nextToken();
                                                                                                            Log.d("버스 종류", busnumber[num]);
                                                                                                            num++;
                                                                                                        }

                                                                                                        int num1 = 0, num2 = 0, num3 = 0, count = 0;
                                                                                                        starriebus = new StringTokenizer(arriveBus, "*");

                                                                                                        String arrtime[] = new String[starriebus.countTokens()];
                                                                                                        String routeno[] = new String[starriebus.countTokens()];
                                                                                                        String routeid[] = new String[starriebus.countTokens()];
                                                                                                        while (starriebus.hasMoreTokens()) {
                                                                                                            cutting_starrive = new StringTokenizer(starriebus.nextToken(), "@");
                                                                                                            while (cutting_starrive.hasMoreTokens()) {
                                                                                                                if (count % 3 == 0) {
                                                                                                                    arrtime[num1] = cutting_starrive.nextToken();
                                                                                                                    num1++;
                                                                                                                    count++;
                                                                                                                } else if (count % 3 == 1) {
                                                                                                                    routeno[num2] = cutting_starrive.nextToken();
                                                                                                                    num2++;
                                                                                                                    count++;
                                                                                                                } else if (count % 3 == 2) {
                                                                                                                    routeid[num3] = cutting_starrive.nextToken();
                                                                                                                    num3++;
                                                                                                                    count++;
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        runOnUiThread(new Runnable() {
                                                                                                            @Override
                                                                                                            public void run() {
                                                                                                                Dialog dl = new Dialog(MainActivity.this);
                                                                                                                dl.setContentView(R.layout.bus_layout);
                                                                                                                LinearLayout verticalLayout = dl.findViewById(R.id.busnumber_space);
                                                                                                                for (String bus : busnumber) {
                                                                                                                    TextView textView = new TextView(MainActivity.this);
                                                                                                                    textView.setText(bus);
                                                                                                                    textView.setTextSize(16);
                                                                                                                    textView.setTextColor(Color.BLACK);
                                                                                                                    verticalLayout.addView(textView);

                                                                                                                    View separator = new View(MainActivity.this);
                                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                                    separator.setLayoutParams(params);
                                                                                                                    verticalLayout.addView(separator);
                                                                                                                }

                                                                                                                LinearLayout verticalLayout2 = dl.findViewById(R.id.arrive_space);

                                                                                                                for (int i = 0; i < arrtime.length - 1; i++) {
                                                                                                                    int minIndex = i;
                                                                                                                    for (int j = i + 1; j < arrtime.length; j++) {
                                                                                                                        int time1 = Integer.parseInt(arrtime[j]);
                                                                                                                        int time2 = Integer.parseInt(arrtime[minIndex]);
                                                                                                                        if (time1 < time2) {
                                                                                                                            minIndex = j;
                                                                                                                        }
                                                                                                                    }

                                                                                                                    // 최소값과 현재 위치의 값을 교환합니다.
                                                                                                                    int tempTime = Integer.parseInt(arrtime[i]);
                                                                                                                    arrtime[i] = arrtime[minIndex];
                                                                                                                    arrtime[minIndex] = Integer.toString(tempTime);

                                                                                                                    String tempNo = routeno[i];
                                                                                                                    routeno[i] = routeno[minIndex];
                                                                                                                    routeno[minIndex] = tempNo;

                                                                                                                    String tempId = routeid[i];
                                                                                                                    routeid[i] = routeid[minIndex];
                                                                                                                    routeid[minIndex] = tempId;
                                                                                                                }

                                                                                                                for (int i = 0; i < arrtime.length; i++) {
                                                                                                                    TextView timeTextView = new TextView(MainActivity.this);
                                                                                                                    int time = Integer.parseInt(arrtime[i]);
                                                                                                                    time = time/60;
                                                                                                                    String times = Integer.toString(time);
                                                                                                                    timeTextView.setText(times+"분");
                                                                                                                    timeTextView.setTextSize(16);
                                                                                                                    timeTextView.setTextColor(Color.BLACK);

                                                                                                                    TextView numberTextView = new TextView(MainActivity.this);
                                                                                                                    numberTextView.setText(routeno[i]);
                                                                                                                    numberTextView.setTextSize(16);
                                                                                                                    numberTextView.setTextColor(Color.BLACK);

                                                                                                                    TextView stTextView = new TextView(MainActivity.this);
                                                                                                                    stTextView.setText(routeid[i]);
                                                                                                                    stTextView.setTextSize(16);
                                                                                                                    stTextView.setTextColor(Color.BLACK);

                                                                                                                    LinearLayout horizontalLayout = new LinearLayout(MainActivity.this);
                                                                                                                    horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                                                                                    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

                                                                                                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                                                                                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                                                                                                    timeTextView.setLayoutParams(layoutParams);
                                                                                                                    numberTextView.setLayoutParams(layoutParams);
                                                                                                                    stTextView.setLayoutParams(layoutParams);

                                                                                                                    horizontalLayout.addView(timeTextView);
                                                                                                                    horizontalLayout.addView(numberTextView);
                                                                                                                    horizontalLayout.addView(stTextView);
                                                                                                                    verticalLayout2.addView(horizontalLayout);

                                                                                                                    View separator = new View(MainActivity.this);
                                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                                    separator.setLayoutParams(params);
                                                                                                                    verticalLayout2.addView(separator);
                                                                                                                }
                                                                                                                dl.show();
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                }).start();

                                                                                                return false;
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }).start();
                                                                }
                                                            });
                                                            return false;
                                                        }
                                                    });

                                                }
                                                LatLng newLatLng = new LatLng(Double.parseDouble(mapy[0]), Double.parseDouble(mapx[0]));
                                                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(newLatLng, 12).animate(CameraAnimation.Fly, 3000);
                                                naverMap.moveCamera(cameraUpdate);
                                            }
                                        });
                                    }
                                }).start();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                        }
                    });
                } else if(spinner_selec.equals("마산회원구")){
                    getSearchResult("창원시 마산회원구"+ s, new SearchCallback() {
                        @Override
                        public void onSuccess(String result) {
                            data0 = result;
                            Log.e(TAG, "data0 : " + data0);
                            search_data = parseXML(data0);
                            Log.e(TAG, "파싱 성공 : " + search_data);

                            if(search_data.equals("")){
                                Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                            }else{
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int count = 0;
                                        int num1 = 0;
                                        int num2 = 0;
                                        int num3 = 0;
                                        int num4 = 0;

                                        StringTokenizer stBuffer = new StringTokenizer(search_data,"*");
                                        String title[] = new String[stBuffer.countTokens()];
                                        String address[] = new String[stBuffer.countTokens()];
                                        String mapx[] = new String[stBuffer.countTokens()];
                                        String mapy[] = new String[stBuffer.countTokens()];
                                        while(stBuffer.hasMoreTokens()){
                                            String cutting = stBuffer.nextToken();
                                            search_result = new StringTokenizer(cutting,"@");
                                            while(search_result.hasMoreTokens()){
                                                if(count % 4 == 0){
                                                    title[num1] = search_result.nextToken();
                                                    num1++;
                                                    count++;
                                                } else if(count % 4 == 1){
                                                    address[num2] = search_result.nextToken();
                                                    num2++;
                                                    count++;
                                                }else if(count % 4 == 2){
                                                    mapx[num3] = search_result.nextToken();
                                                    mapx[num3] = mapx[num3].substring(0, 3) + "." + mapx[num3].substring(3);
                                                    Log.d("위도", "결과값: " + mapx[num3]);
                                                    num3++;
                                                    count++;
                                                }else if(count % 4 == 3){
                                                    mapy[num4] = search_result.nextToken();
                                                    mapy[num4] = mapy[num4].substring(0, 2) + "." + mapy[num4].substring(2);
                                                    Log.d("경도", "결과값: " + mapy[num4]);
                                                    num4++;
                                                    count++;
                                                }
                                            }
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                freemMarkers();
                                                freebusMarkers();
                                                freecycleMarkers();
                                                for(int i = 0; i < title.length; i++){
                                                    Marker marker = new Marker();
                                                    marker.setIconTintColor(Color.BLUE);
                                                    marker.setPosition(new LatLng(Double.parseDouble(mapy[i]), Double.parseDouble(mapx[i])));
                                                    marker.setMap(naverMap);
                                                    marker.setCaptionText(title[i]);
                                                    marker.setCaptionAligns(Align.Top);
                                                    marker.setCaptionColor(Color.BLACK);
                                                    marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                    marker.setCaptionTextSize(14);
                                                    marker.setHideCollidedSymbols(true);
                                                    mMarkers.add(marker);

                                                    int finalI = i;

                                                    marker.setOnClickListener(new Overlay.OnClickListener() {
                                                        private String close_time;
                                                        private String state_open;
                                                        @Override
                                                        public boolean onClick(@NonNull Overlay overlay) {
                                                            getMapInfoName.setText(title[finalI]);
                                                            getMapInfoAddr.setText(address[finalI]);
                                                            String place = URLEncoder.encode(title[finalI]);
                                                            String url = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%B0%BD%EC%9B%90%20"+place;

                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    org.jsoup.nodes.Document jsoupDoc = null;
                                                                    try {
                                                                        jsoupDoc = Jsoup.connect(url).get();
                                                                    } catch (IOException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    Elements elements = jsoupDoc.select(".place_section_content"); // 영업시간이 포함된 엘리먼트 선택
                                                                    Log.d("elements", "결과값: " + elements);
                                                                    Elements map_page = jsoupDoc.select("#_title");
                                                                    Log.d("지도 페이지: ", map_page.toString());
                                                                    String going_map = map_page.select("a").attr("href");;
                                                                    Log.d("지도 주소: ", going_map);

                                                                    Elements imgpage = jsoupDoc.select("div.K0PDV._div#ibu_1");
                                                                    Log.d("이미지 url 포함한 부분 : ", imgpage.toString());


                                                                    getImageSearchResult("창원" + title[finalI], new SearchCallback() {
                                                                        @Override
                                                                        public void onSuccess(String result) {
                                                                            Log.d("성공?: ", result);
                                                                            ImageUrl = ImageparseXML(result);
                                                                            Log.d("link : ", ImageUrl);
                                                                            if(ImageUrl.equals("")){
                                                                                imageView.setImageResource(R.drawable.nonplace);
                                                                            } else {
                                                                                String imageUrl = ImageUrl;
                                                                                new LoadImageTask().execute(imageUrl);
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onFailure(Exception e) {

                                                                        }
                                                                    });

                                                                    Elements open_state = elements.select(".A_cdD");
                                                                    Log.d("제발: ", open_state.toString());
                                                                    Elements time_close = elements.select(".U7pYf");
                                                                    Log.d("제발: ", time_close.toString());
                                                                    String openstate = open_state.select("em").text();
                                                                    Log.d("영업여부 : ", openstate);
                                                                    String closingTime = time_close.select("span.place_blind").text();
                                                                    Log.d("영업종료 시간은? : ", closingTime);
                                                                    close_time = closingTime;
                                                                    state_open = openstate;

                                                                    getCloseTime.setText(close_time);
                                                                    getOpenState.setText(state_open);

                                                                }
                                                            }).start();
                                                            getCloseTime.setText(close_time);
                                                            getOpenState.setText(state_open);
                                                            info_Layout.setVisibility(View.VISIBLE);

                                                            infoClose.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    info_Layout.setVisibility(View.INVISIBLE);
                                                                }
                                                            });

                                                            rootAdd.setOnClickListener(new View.OnClickListener() {


                                                                @Override
                                                                public void onClick(View view) {

                                                                    Dialog dl = new Dialog(MainActivity.this);
                                                                    dl.setContentView(R.layout.activity_custom_dialog);
                                                                    List<Data> dataList = db.dataDao().getAll();

                                                                    LinearLayout layout = (LinearLayout) dl.findViewById(R.id.dynamic_space);
                                                                    LinearLayout layout_below = (LinearLayout) dl.findViewById(R.id.btn_space);
                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                            0, // width
                                                                            LinearLayout.LayoutParams.WRAP_CONTENT // height
                                                                    );
                                                                    params.weight = 1f;
                                                                    RadioGroup radioGroup = new RadioGroup(MainActivity.this);
                                                                    radioGroup.setOrientation(RadioGroup.VERTICAL);
                                                                    Button addbtn = new Button(MainActivity.this);
                                                                    addbtn.setText("일정 추가");
                                                                    addbtn.setLayoutParams(params);
                                                                    Button newbtn = new Button(MainActivity.this);
                                                                    newbtn.setText("새 일정");
                                                                    newbtn.setLayoutParams(params);
                                                                    layout_below.addView(addbtn);
                                                                    layout_below.addView(newbtn);

                                                                    for(Data data:dataList){
                                                                        RadioButton checkBox = new RadioButton(MainActivity.this);
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
                                                                                            TravelSpot ts0 = new TravelSpot(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table1().insert(ts0);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            Log.d("저장된 장소", ts0.getName());
                                                                                            break;
                                                                                        case "spot1":
                                                                                            ts1 = new TravelSpot1(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table2().insert1(ts1);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot2":
                                                                                            ts2 = new TravelSpot2(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table3().insert1(ts2);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot3":
                                                                                            ts3 = new TravelSpot3(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table4().insert1(ts3);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot4":
                                                                                            ts4 = new TravelSpot4(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
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
                                                                            Intent intent = new Intent(MainActivity.this, rootManage.class);
                                                                            startActivity(intent);
                                                                            dl.dismiss();

                                                                        }
                                                                    });


                                                                }
                                                            });

                                                            busFind.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    new Thread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, count = 0;
                                                                            busstop = getBus();
                                                                            stbusstop = new StringTokenizer(busstop, "*");

                                                                            String bus_citycode[] = new String[stbusstop.countTokens()];
                                                                            String busstop_mapx[] = new String[stbusstop.countTokens()];
                                                                            String busstop_mapy[] = new String[stbusstop.countTokens()];
                                                                            String busstopId[] = new String[stbusstop.countTokens()];
                                                                            String busstopNm[] = new String[stbusstop.countTokens()];

                                                                            while (stbusstop.hasMoreTokens()) {
                                                                                cutting_stbusstop = new StringTokenizer(stbusstop.nextToken(), "@");
                                                                                while (cutting_stbusstop.hasMoreTokens()) {
                                                                                    if (count % 5 == 0) {
                                                                                        bus_citycode[num1] = cutting_stbusstop.nextToken();
                                                                                        num1++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 1) {
                                                                                        busstop_mapx[num2] = cutting_stbusstop.nextToken();
                                                                                        num2++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 2) {
                                                                                        busstop_mapy[num3] = cutting_stbusstop.nextToken();
                                                                                        num3++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 3) {
                                                                                        busstopId[num4] = cutting_stbusstop.nextToken();
                                                                                        num4++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 4) {
                                                                                        busstopNm[num5] = cutting_stbusstop.nextToken();
                                                                                        num5++;
                                                                                        count++;
                                                                                    }
                                                                                }
                                                                            }
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    freebusMarkers();
                                                                                    for (int i = 0; i < busstopNm.length; i++) {
                                                                                        Marker marker = new Marker();
                                                                                        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_directions_bus_24));
                                                                                        marker.setIconTintColor(Color.BLUE);
                                                                                        marker.setPosition(new LatLng(Double.parseDouble(busstop_mapx[i]), Double.parseDouble(busstop_mapy[i])));
                                                                                        marker.setMap(naverMap);
                                                                                        marker.setCaptionText(busstopNm[i]);
                                                                                        marker.setCaptionAligns(Align.Top);
                                                                                        marker.setCaptionColor(Color.BLACK);
                                                                                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                                                        marker.setCaptionTextSize(14);
                                                                                        marker.setHideCollidedSymbols(true);
                                                                                        busMarkers.add(marker);

                                                                                        int finalI = i;

                                                                                        marker.setOnClickListener(new Overlay.OnClickListener() {
                                                                                            @Override
                                                                                            public boolean onClick(@NonNull Overlay overlay) {
                                                                                                Toast.makeText(getApplicationContext(), "버스 정보를 보여드립니다", Toast.LENGTH_LONG).show();
                                                                                                new Thread(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        stationcontent = getStationContent(bus_citycode[finalI], busstopId[finalI]);
                                                                                                        arriveBus = getArriveBus(bus_citycode[finalI], busstopId[finalI]);
                                                                                                        ststation = new StringTokenizer(stationcontent, "*");
                                                                                                        int num = 0;

                                                                                                        String busnumber[] = new String[ststation.countTokens()];
                                                                                                        while(ststation.hasMoreTokens()){
                                                                                                            busnumber[num] = ststation.nextToken();
                                                                                                            Log.d("버스 종류", busnumber[num]);
                                                                                                            num++;
                                                                                                        }

                                                                                                        int num1 = 0, num2 = 0, num3 = 0, count = 0;
                                                                                                        starriebus = new StringTokenizer(arriveBus, "*");

                                                                                                        String arrtime[] = new String[starriebus.countTokens()];
                                                                                                        String routeno[] = new String[starriebus.countTokens()];
                                                                                                        String routeid[] = new String[starriebus.countTokens()];
                                                                                                        while (starriebus.hasMoreTokens()) {
                                                                                                            cutting_starrive = new StringTokenizer(starriebus.nextToken(), "@");
                                                                                                            while (cutting_starrive.hasMoreTokens()) {
                                                                                                                if (count % 3 == 0) {
                                                                                                                    arrtime[num1] = cutting_starrive.nextToken();
                                                                                                                    num1++;
                                                                                                                    count++;
                                                                                                                } else if (count % 3 == 1) {
                                                                                                                    routeno[num2] = cutting_starrive.nextToken();
                                                                                                                    num2++;
                                                                                                                    count++;
                                                                                                                } else if (count % 3 == 2) {
                                                                                                                    routeid[num3] = cutting_starrive.nextToken();
                                                                                                                    num3++;
                                                                                                                    count++;
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        runOnUiThread(new Runnable() {
                                                                                                            @Override
                                                                                                            public void run() {
                                                                                                                Dialog dl = new Dialog(MainActivity.this);
                                                                                                                dl.setContentView(R.layout.bus_layout);
                                                                                                                LinearLayout verticalLayout = dl.findViewById(R.id.busnumber_space);
                                                                                                                for (String bus : busnumber) {
                                                                                                                    TextView textView = new TextView(MainActivity.this);
                                                                                                                    textView.setText(bus);
                                                                                                                    textView.setTextSize(16);
                                                                                                                    textView.setTextColor(Color.BLACK);
                                                                                                                    verticalLayout.addView(textView);

                                                                                                                    View separator = new View(MainActivity.this);
                                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                                    separator.setLayoutParams(params);
                                                                                                                    verticalLayout.addView(separator);
                                                                                                                }

                                                                                                                LinearLayout verticalLayout2 = dl.findViewById(R.id.arrive_space);

                                                                                                                for (int i = 0; i < arrtime.length - 1; i++) {
                                                                                                                    int minIndex = i;
                                                                                                                    for (int j = i + 1; j < arrtime.length; j++) {
                                                                                                                        int time1 = Integer.parseInt(arrtime[j]);
                                                                                                                        int time2 = Integer.parseInt(arrtime[minIndex]);
                                                                                                                        if (time1 < time2) {
                                                                                                                            minIndex = j;
                                                                                                                        }
                                                                                                                    }

                                                                                                                    // 최소값과 현재 위치의 값을 교환합니다.
                                                                                                                    int tempTime = Integer.parseInt(arrtime[i]);
                                                                                                                    arrtime[i] = arrtime[minIndex];
                                                                                                                    arrtime[minIndex] = Integer.toString(tempTime);

                                                                                                                    String tempNo = routeno[i];
                                                                                                                    routeno[i] = routeno[minIndex];
                                                                                                                    routeno[minIndex] = tempNo;

                                                                                                                    String tempId = routeid[i];
                                                                                                                    routeid[i] = routeid[minIndex];
                                                                                                                    routeid[minIndex] = tempId;
                                                                                                                }

                                                                                                                for (int i = 0; i < arrtime.length; i++) {
                                                                                                                    TextView timeTextView = new TextView(MainActivity.this);
                                                                                                                    int time = Integer.parseInt(arrtime[i]);
                                                                                                                    time = time/60;
                                                                                                                    String times = Integer.toString(time);
                                                                                                                    timeTextView.setText(times+"분");
                                                                                                                    timeTextView.setTextSize(16);
                                                                                                                    timeTextView.setTextColor(Color.BLACK);

                                                                                                                    TextView numberTextView = new TextView(MainActivity.this);
                                                                                                                    numberTextView.setText(routeno[i]);
                                                                                                                    numberTextView.setTextSize(16);
                                                                                                                    numberTextView.setTextColor(Color.BLACK);

                                                                                                                    TextView stTextView = new TextView(MainActivity.this);
                                                                                                                    stTextView.setText(routeid[i]);
                                                                                                                    stTextView.setTextSize(16);
                                                                                                                    stTextView.setTextColor(Color.BLACK);

                                                                                                                    LinearLayout horizontalLayout = new LinearLayout(MainActivity.this);
                                                                                                                    horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                                                                                    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

                                                                                                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                                                                                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                                                                                                    timeTextView.setLayoutParams(layoutParams);
                                                                                                                    numberTextView.setLayoutParams(layoutParams);
                                                                                                                    stTextView.setLayoutParams(layoutParams);

                                                                                                                    horizontalLayout.addView(timeTextView);
                                                                                                                    horizontalLayout.addView(numberTextView);
                                                                                                                    horizontalLayout.addView(stTextView);
                                                                                                                    verticalLayout2.addView(horizontalLayout);

                                                                                                                    View separator = new View(MainActivity.this);
                                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                                    separator.setLayoutParams(params);
                                                                                                                    verticalLayout2.addView(separator);
                                                                                                                }
                                                                                                                dl.show();
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                }).start();

                                                                                                return false;
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }).start();
                                                                }
                                                            });
                                                            return false;
                                                        }
                                                    });

                                                }
                                                LatLng newLatLng = new LatLng(Double.parseDouble(mapy[0]), Double.parseDouble(mapx[0]));
                                                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(newLatLng, 12).animate(CameraAnimation.Fly, 3000);
                                                naverMap.moveCamera(cameraUpdate);
                                            }
                                        });
                                    }
                                }).start();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                        }
                    });
                } else if(spinner_selec.equals("마산합포구")){
                    getSearchResult("창원시 마산합포구"+ s, new SearchCallback() {
                        @Override
                        public void onSuccess(String result) {
                            data0 = result;
                            Log.e(TAG, "data0 : " + data0);
                            search_data = parseXML(data0);
                            Log.e(TAG, "파싱 성공 : " + search_data);

                            if(search_data.equals("")){
                                Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                            }else{
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int count = 0;
                                        int num1 = 0;
                                        int num2 = 0;
                                        int num3 = 0;
                                        int num4 = 0;

                                        StringTokenizer stBuffer = new StringTokenizer(search_data,"*");
                                        String title[] = new String[stBuffer.countTokens()];
                                        String address[] = new String[stBuffer.countTokens()];
                                        String mapx[] = new String[stBuffer.countTokens()];
                                        String mapy[] = new String[stBuffer.countTokens()];
                                        while(stBuffer.hasMoreTokens()){
                                            String cutting = stBuffer.nextToken();
                                            search_result = new StringTokenizer(cutting,"@");
                                            while(search_result.hasMoreTokens()){
                                                if(count % 4 == 0){
                                                    title[num1] = search_result.nextToken();
                                                    num1++;
                                                    count++;
                                                } else if(count % 4 == 1){
                                                    address[num2] = search_result.nextToken();
                                                    num2++;
                                                    count++;
                                                }else if(count % 4 == 2){
                                                    mapx[num3] = search_result.nextToken();
                                                    mapx[num3] = mapx[num3].substring(0, 3) + "." + mapx[num3].substring(3);
                                                    Log.d("위도", "결과값: " + mapx[num3]);
                                                    num3++;
                                                    count++;
                                                }else if(count % 4 == 3){
                                                    mapy[num4] = search_result.nextToken();
                                                    mapy[num4] = mapy[num4].substring(0, 2) + "." + mapy[num4].substring(2);
                                                    Log.d("경도", "결과값: " + mapy[num4]);
                                                    num4++;
                                                    count++;
                                                }
                                            }
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                freemMarkers();
                                                freebusMarkers();
                                                freecycleMarkers();
                                                for(int i = 0; i < title.length; i++){
                                                    Marker marker = new Marker();
                                                    marker.setIconTintColor(Color.BLUE);
                                                    marker.setPosition(new LatLng(Double.parseDouble(mapy[i]), Double.parseDouble(mapx[i])));
                                                    marker.setMap(naverMap);
                                                    marker.setCaptionText(title[i]);
                                                    marker.setCaptionAligns(Align.Top);
                                                    marker.setCaptionColor(Color.BLACK);
                                                    marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                    marker.setCaptionTextSize(14);
                                                    marker.setHideCollidedSymbols(true);
                                                    mMarkers.add(marker);

                                                    int finalI = i;

                                                    marker.setOnClickListener(new Overlay.OnClickListener() {
                                                        private String close_time;
                                                        private String state_open;
                                                        @Override
                                                        public boolean onClick(@NonNull Overlay overlay) {
                                                            getMapInfoName.setText(title[finalI]);
                                                            getMapInfoAddr.setText(address[finalI]);
                                                            String place = URLEncoder.encode(title[finalI]);
                                                            String url = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%B0%BD%EC%9B%90%20"+place;

                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    org.jsoup.nodes.Document jsoupDoc = null;
                                                                    try {
                                                                        jsoupDoc = Jsoup.connect(url).get();
                                                                    } catch (IOException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    Elements elements = jsoupDoc.select(".place_section_content"); // 영업시간이 포함된 엘리먼트 선택
                                                                    Log.d("elements", "결과값: " + elements);
                                                                    Elements map_page = jsoupDoc.select("#_title");
                                                                    Log.d("지도 페이지: ", map_page.toString());
                                                                    String going_map = map_page.select("a").attr("href");;
                                                                    Log.d("지도 주소: ", going_map);

                                                                    Elements imgpage = jsoupDoc.select("div.K0PDV._div#ibu_1");
                                                                    Log.d("이미지 url 포함한 부분 : ", imgpage.toString());


                                                                    getImageSearchResult("창원" + title[finalI], new SearchCallback() {
                                                                        @Override
                                                                        public void onSuccess(String result) {
                                                                            Log.d("성공?: ", result);
                                                                            ImageUrl = ImageparseXML(result);
                                                                            Log.d("link : ", ImageUrl);
                                                                            if(ImageUrl.equals("")){
                                                                                imageView.setImageResource(R.drawable.nonplace);
                                                                            } else {
                                                                                String imageUrl = ImageUrl;
                                                                                new LoadImageTask().execute(imageUrl);
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onFailure(Exception e) {

                                                                        }
                                                                    });

                                                                    Elements open_state = elements.select(".A_cdD");
                                                                    Log.d("제발: ", open_state.toString());
                                                                    Elements time_close = elements.select(".U7pYf");
                                                                    Log.d("제발: ", time_close.toString());
                                                                    String openstate = open_state.select("em").text();
                                                                    Log.d("영업여부 : ", openstate);
                                                                    String closingTime = time_close.select("span.place_blind").text();
                                                                    Log.d("영업종료 시간은? : ", closingTime);
                                                                    close_time = closingTime;
                                                                    state_open = openstate;

                                                                    getCloseTime.setText(close_time);
                                                                    getOpenState.setText(state_open);

                                                                }
                                                            }).start();
                                                            getCloseTime.setText(close_time);
                                                            getOpenState.setText(state_open);
                                                            info_Layout.setVisibility(View.VISIBLE);

                                                            infoClose.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    info_Layout.setVisibility(View.INVISIBLE);
                                                                }
                                                            });

                                                            rootAdd.setOnClickListener(new View.OnClickListener() {


                                                                @Override
                                                                public void onClick(View view) {

                                                                    Dialog dl = new Dialog(MainActivity.this);
                                                                    dl.setContentView(R.layout.activity_custom_dialog);
                                                                    List<Data> dataList = db.dataDao().getAll();

                                                                    LinearLayout layout = (LinearLayout) dl.findViewById(R.id.dynamic_space);
                                                                    LinearLayout layout_below = (LinearLayout) dl.findViewById(R.id.btn_space);
                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                            0, // width
                                                                            LinearLayout.LayoutParams.WRAP_CONTENT // height
                                                                    );
                                                                    params.weight = 1f;
                                                                    RadioGroup radioGroup = new RadioGroup(MainActivity.this);
                                                                    radioGroup.setOrientation(RadioGroup.VERTICAL);
                                                                    Button addbtn = new Button(MainActivity.this);
                                                                    addbtn.setText("일정 추가");
                                                                    addbtn.setLayoutParams(params);
                                                                    Button newbtn = new Button(MainActivity.this);
                                                                    newbtn.setText("새 일정");
                                                                    newbtn.setLayoutParams(params);
                                                                    layout_below.addView(addbtn);
                                                                    layout_below.addView(newbtn);

                                                                    for(Data data:dataList){
                                                                        RadioButton checkBox = new RadioButton(MainActivity.this);
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
                                                                                            TravelSpot ts0 = new TravelSpot(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table1().insert(ts0);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            Log.d("저장된 장소", ts0.getName());
                                                                                            break;
                                                                                        case "spot1":
                                                                                            ts1 = new TravelSpot1(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table2().insert1(ts1);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot2":
                                                                                            ts2 = new TravelSpot2(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table3().insert1(ts2);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot3":
                                                                                            ts3 = new TravelSpot3(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table4().insert1(ts3);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot4":
                                                                                            ts4 = new TravelSpot4(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
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
                                                                            Intent intent = new Intent(MainActivity.this, rootManage.class);
                                                                            startActivity(intent);
                                                                            dl.dismiss();

                                                                        }
                                                                    });


                                                                }
                                                            });

                                                            busFind.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    new Thread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, count = 0;
                                                                            busstop = getBus();
                                                                            stbusstop = new StringTokenizer(busstop, "*");

                                                                            String bus_citycode[] = new String[stbusstop.countTokens()];
                                                                            String busstop_mapx[] = new String[stbusstop.countTokens()];
                                                                            String busstop_mapy[] = new String[stbusstop.countTokens()];
                                                                            String busstopId[] = new String[stbusstop.countTokens()];
                                                                            String busstopNm[] = new String[stbusstop.countTokens()];

                                                                            while (stbusstop.hasMoreTokens()) {
                                                                                cutting_stbusstop = new StringTokenizer(stbusstop.nextToken(), "@");
                                                                                while (cutting_stbusstop.hasMoreTokens()) {
                                                                                    if (count % 5 == 0) {
                                                                                        bus_citycode[num1] = cutting_stbusstop.nextToken();
                                                                                        num1++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 1) {
                                                                                        busstop_mapx[num2] = cutting_stbusstop.nextToken();
                                                                                        num2++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 2) {
                                                                                        busstop_mapy[num3] = cutting_stbusstop.nextToken();
                                                                                        num3++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 3) {
                                                                                        busstopId[num4] = cutting_stbusstop.nextToken();
                                                                                        num4++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 4) {
                                                                                        busstopNm[num5] = cutting_stbusstop.nextToken();
                                                                                        num5++;
                                                                                        count++;
                                                                                    }
                                                                                }
                                                                            }
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    freebusMarkers();
                                                                                    for (int i = 0; i < busstopNm.length; i++) {
                                                                                        Marker marker = new Marker();
                                                                                        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_directions_bus_24));
                                                                                        marker.setIconTintColor(Color.BLUE);
                                                                                        marker.setPosition(new LatLng(Double.parseDouble(busstop_mapx[i]), Double.parseDouble(busstop_mapy[i])));
                                                                                        marker.setMap(naverMap);
                                                                                        marker.setCaptionText(busstopNm[i]);
                                                                                        marker.setCaptionAligns(Align.Top);
                                                                                        marker.setCaptionColor(Color.BLACK);
                                                                                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                                                        marker.setCaptionTextSize(14);
                                                                                        marker.setHideCollidedSymbols(true);
                                                                                        busMarkers.add(marker);

                                                                                        int finalI = i;

                                                                                        marker.setOnClickListener(new Overlay.OnClickListener() {
                                                                                            @Override
                                                                                            public boolean onClick(@NonNull Overlay overlay) {
                                                                                                Toast.makeText(getApplicationContext(), "버스 정보를 보여드립니다", Toast.LENGTH_LONG).show();
                                                                                                new Thread(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        stationcontent = getStationContent(bus_citycode[finalI], busstopId[finalI]);
                                                                                                        arriveBus = getArriveBus(bus_citycode[finalI], busstopId[finalI]);
                                                                                                        ststation = new StringTokenizer(stationcontent, "*");
                                                                                                        int num = 0;

                                                                                                        String busnumber[] = new String[ststation.countTokens()];
                                                                                                        while(ststation.hasMoreTokens()){
                                                                                                            busnumber[num] = ststation.nextToken();
                                                                                                            Log.d("버스 종류", busnumber[num]);
                                                                                                            num++;
                                                                                                        }

                                                                                                        int num1 = 0, num2 = 0, num3 = 0, count = 0;
                                                                                                        starriebus = new StringTokenizer(arriveBus, "*");

                                                                                                        String arrtime[] = new String[starriebus.countTokens()];
                                                                                                        String routeno[] = new String[starriebus.countTokens()];
                                                                                                        String routeid[] = new String[starriebus.countTokens()];
                                                                                                        while (starriebus.hasMoreTokens()) {
                                                                                                            cutting_starrive = new StringTokenizer(starriebus.nextToken(), "@");
                                                                                                            while (cutting_starrive.hasMoreTokens()) {
                                                                                                                if (count % 3 == 0) {
                                                                                                                    arrtime[num1] = cutting_starrive.nextToken();
                                                                                                                    num1++;
                                                                                                                    count++;
                                                                                                                } else if (count % 3 == 1) {
                                                                                                                    routeno[num2] = cutting_starrive.nextToken();
                                                                                                                    num2++;
                                                                                                                    count++;
                                                                                                                } else if (count % 3 == 2) {
                                                                                                                    routeid[num3] = cutting_starrive.nextToken();
                                                                                                                    num3++;
                                                                                                                    count++;
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        runOnUiThread(new Runnable() {
                                                                                                            @Override
                                                                                                            public void run() {
                                                                                                                Dialog dl = new Dialog(MainActivity.this);
                                                                                                                dl.setContentView(R.layout.bus_layout);
                                                                                                                LinearLayout verticalLayout = dl.findViewById(R.id.busnumber_space);
                                                                                                                for (String bus : busnumber) {
                                                                                                                    TextView textView = new TextView(MainActivity.this);
                                                                                                                    textView.setText(bus);
                                                                                                                    textView.setTextSize(16);
                                                                                                                    textView.setTextColor(Color.BLACK);
                                                                                                                    verticalLayout.addView(textView);

                                                                                                                    View separator = new View(MainActivity.this);
                                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                                    separator.setLayoutParams(params);
                                                                                                                    verticalLayout.addView(separator);
                                                                                                                }

                                                                                                                LinearLayout verticalLayout2 = dl.findViewById(R.id.arrive_space);

                                                                                                                for (int i = 0; i < arrtime.length - 1; i++) {
                                                                                                                    int minIndex = i;
                                                                                                                    for (int j = i + 1; j < arrtime.length; j++) {
                                                                                                                        int time1 = Integer.parseInt(arrtime[j]);
                                                                                                                        int time2 = Integer.parseInt(arrtime[minIndex]);
                                                                                                                        if (time1 < time2) {
                                                                                                                            minIndex = j;
                                                                                                                        }
                                                                                                                    }

                                                                                                                    // 최소값과 현재 위치의 값을 교환합니다.
                                                                                                                    int tempTime = Integer.parseInt(arrtime[i]);
                                                                                                                    arrtime[i] = arrtime[minIndex];
                                                                                                                    arrtime[minIndex] = Integer.toString(tempTime);

                                                                                                                    String tempNo = routeno[i];
                                                                                                                    routeno[i] = routeno[minIndex];
                                                                                                                    routeno[minIndex] = tempNo;

                                                                                                                    String tempId = routeid[i];
                                                                                                                    routeid[i] = routeid[minIndex];
                                                                                                                    routeid[minIndex] = tempId;
                                                                                                                }

                                                                                                                for (int i = 0; i < arrtime.length; i++) {
                                                                                                                    TextView timeTextView = new TextView(MainActivity.this);
                                                                                                                    int time = Integer.parseInt(arrtime[i]);
                                                                                                                    time = time/60;
                                                                                                                    String times = Integer.toString(time);
                                                                                                                    timeTextView.setText(times+"분");
                                                                                                                    timeTextView.setTextSize(16);
                                                                                                                    timeTextView.setTextColor(Color.BLACK);

                                                                                                                    TextView numberTextView = new TextView(MainActivity.this);
                                                                                                                    numberTextView.setText(routeno[i]);
                                                                                                                    numberTextView.setTextSize(16);
                                                                                                                    numberTextView.setTextColor(Color.BLACK);

                                                                                                                    TextView stTextView = new TextView(MainActivity.this);
                                                                                                                    stTextView.setText(routeid[i]);
                                                                                                                    stTextView.setTextSize(16);
                                                                                                                    stTextView.setTextColor(Color.BLACK);

                                                                                                                    LinearLayout horizontalLayout = new LinearLayout(MainActivity.this);
                                                                                                                    horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                                                                                    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

                                                                                                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                                                                                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                                                                                                    timeTextView.setLayoutParams(layoutParams);
                                                                                                                    numberTextView.setLayoutParams(layoutParams);
                                                                                                                    stTextView.setLayoutParams(layoutParams);

                                                                                                                    horizontalLayout.addView(timeTextView);
                                                                                                                    horizontalLayout.addView(numberTextView);
                                                                                                                    horizontalLayout.addView(stTextView);
                                                                                                                    verticalLayout2.addView(horizontalLayout);

                                                                                                                    View separator = new View(MainActivity.this);
                                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                                    separator.setLayoutParams(params);
                                                                                                                    verticalLayout2.addView(separator);
                                                                                                                }
                                                                                                                dl.show();
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                }).start();

                                                                                                return false;
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }).start();
                                                                }
                                                            });
                                                            return false;
                                                        }
                                                    });

                                                }
                                                LatLng newLatLng = new LatLng(Double.parseDouble(mapy[0]), Double.parseDouble(mapx[0]));
                                                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(newLatLng, 12).animate(CameraAnimation.Fly, 3000);
                                                naverMap.moveCamera(cameraUpdate);
                                            }
                                        });
                                    }
                                }).start();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                        }
                    });
                } else if(spinner_selec.equals("진해구")){
                    getSearchResult("창원시 진해구"+ s, new SearchCallback() {
                        @Override
                        public void onSuccess(String result) {
                            data0 = result;
                            Log.e(TAG, "data0 : " + data0);
                            search_data = parseXML(data0);
                            Log.e(TAG, "파싱 성공 : " + search_data);

                            if(search_data.equals("")){
                                Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                            }else{
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int count = 0;
                                        int num1 = 0;
                                        int num2 = 0;
                                        int num3 = 0;
                                        int num4 = 0;

                                        StringTokenizer stBuffer = new StringTokenizer(search_data,"*");
                                        String title[] = new String[stBuffer.countTokens()];
                                        String address[] = new String[stBuffer.countTokens()];
                                        String mapx[] = new String[stBuffer.countTokens()];
                                        String mapy[] = new String[stBuffer.countTokens()];
                                        while(stBuffer.hasMoreTokens()){
                                            String cutting = stBuffer.nextToken();
                                            search_result = new StringTokenizer(cutting,"@");
                                            while(search_result.hasMoreTokens()){
                                                if(count % 4 == 0){
                                                    title[num1] = search_result.nextToken();
                                                    num1++;
                                                    count++;
                                                } else if(count % 4 == 1){
                                                    address[num2] = search_result.nextToken();
                                                    num2++;
                                                    count++;
                                                }else if(count % 4 == 2){
                                                    mapx[num3] = search_result.nextToken();
                                                    mapx[num3] = mapx[num3].substring(0, 3) + "." + mapx[num3].substring(3);
                                                    Log.d("위도", "결과값: " + mapx[num3]);
                                                    num3++;
                                                    count++;
                                                }else if(count % 4 == 3){
                                                    mapy[num4] = search_result.nextToken();
                                                    mapy[num4] = mapy[num4].substring(0, 2) + "." + mapy[num4].substring(2);
                                                    Log.d("경도", "결과값: " + mapy[num4]);
                                                    num4++;
                                                    count++;
                                                }
                                            }
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                freemMarkers();
                                                freebusMarkers();
                                                freecycleMarkers();
                                                for(int i = 0; i < title.length; i++){
                                                    Marker marker = new Marker();
                                                    marker.setIconTintColor(Color.BLUE);
                                                    marker.setPosition(new LatLng(Double.parseDouble(mapy[i]), Double.parseDouble(mapx[i])));
                                                    marker.setMap(naverMap);
                                                    marker.setCaptionText(title[i]);
                                                    marker.setCaptionAligns(Align.Top);
                                                    marker.setCaptionColor(Color.BLACK);
                                                    marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                    marker.setCaptionTextSize(14);
                                                    marker.setHideCollidedSymbols(true);
                                                    mMarkers.add(marker);

                                                    int finalI = i;

                                                    marker.setOnClickListener(new Overlay.OnClickListener() {
                                                        private String close_time;
                                                        private String state_open;
                                                        @Override
                                                        public boolean onClick(@NonNull Overlay overlay) {
                                                            getMapInfoName.setText(title[finalI]);
                                                            getMapInfoAddr.setText(address[finalI]);
                                                            String place = URLEncoder.encode(title[finalI]);
                                                            String url = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%B0%BD%EC%9B%90%20"+place;

                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    org.jsoup.nodes.Document jsoupDoc = null;
                                                                    try {
                                                                        jsoupDoc = Jsoup.connect(url).get();
                                                                    } catch (IOException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    Elements elements = jsoupDoc.select(".place_section_content"); // 영업시간이 포함된 엘리먼트 선택
                                                                    Log.d("elements", "결과값: " + elements);
                                                                    Elements map_page = jsoupDoc.select("#_title");
                                                                    Log.d("지도 페이지: ", map_page.toString());
                                                                    String going_map = map_page.select("a").attr("href");;
                                                                    Log.d("지도 주소: ", going_map);

                                                                    Elements imgpage = jsoupDoc.select("div.K0PDV._div#ibu_1");
                                                                    Log.d("이미지 url 포함한 부분 : ", imgpage.toString());


                                                                    getImageSearchResult("창원" + title[finalI], new SearchCallback() {
                                                                        @Override
                                                                        public void onSuccess(String result) {
                                                                            Log.d("성공?: ", result);
                                                                            ImageUrl = ImageparseXML(result);
                                                                            Log.d("link : ", ImageUrl);
                                                                            if(ImageUrl.equals("")){
                                                                                imageView.setImageResource(R.drawable.nonplace);
                                                                            } else {
                                                                                String imageUrl = ImageUrl;
                                                                                new LoadImageTask().execute(imageUrl);
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onFailure(Exception e) {

                                                                        }
                                                                    });

                                                                    Elements open_state = elements.select(".A_cdD");
                                                                    Log.d("제발: ", open_state.toString());
                                                                    Elements time_close = elements.select(".U7pYf");
                                                                    Log.d("제발: ", time_close.toString());
                                                                    String openstate = open_state.select("em").text();
                                                                    Log.d("영업여부 : ", openstate);
                                                                    String closingTime = time_close.select("span.place_blind").text();
                                                                    Log.d("영업종료 시간은? : ", closingTime);
                                                                    close_time = closingTime;
                                                                    state_open = openstate;

                                                                    getCloseTime.setText(close_time);
                                                                    getOpenState.setText(state_open);

                                                                }
                                                            }).start();
                                                            getCloseTime.setText(close_time);
                                                            getOpenState.setText(state_open);
                                                            info_Layout.setVisibility(View.VISIBLE);

                                                            infoClose.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    info_Layout.setVisibility(View.INVISIBLE);
                                                                }
                                                            });

                                                            rootAdd.setOnClickListener(new View.OnClickListener() {

                                                                @Override
                                                                public void onClick(View view) {

                                                                    Dialog dl = new Dialog(MainActivity.this);
                                                                    dl.setContentView(R.layout.activity_custom_dialog);
                                                                    List<Data> dataList = db.dataDao().getAll();

                                                                    LinearLayout layout = (LinearLayout) dl.findViewById(R.id.dynamic_space);
                                                                    LinearLayout layout_below = (LinearLayout) dl.findViewById(R.id.btn_space);
                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                            0, // width
                                                                            LinearLayout.LayoutParams.WRAP_CONTENT // height
                                                                    );
                                                                    params.weight = 1f;
                                                                    RadioGroup radioGroup = new RadioGroup(MainActivity.this);
                                                                    radioGroup.setOrientation(RadioGroup.VERTICAL);
                                                                    Button addbtn = new Button(MainActivity.this);
                                                                    addbtn.setText("일정 추가");
                                                                    addbtn.setLayoutParams(params);
                                                                    Button newbtn = new Button(MainActivity.this);
                                                                    newbtn.setText("새 일정");
                                                                    newbtn.setLayoutParams(params);
                                                                    layout_below.addView(addbtn);
                                                                    layout_below.addView(newbtn);

                                                                    for(Data data:dataList){
                                                                        RadioButton checkBox = new RadioButton(MainActivity.this);
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
                                                                                            TravelSpot ts0 = new TravelSpot(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table1().insert(ts0);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            Log.d("저장된 장소", ts0.getName());
                                                                                            break;
                                                                                        case "spot1":
                                                                                            ts1 = new TravelSpot1(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table2().insert1(ts1);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot2":
                                                                                            ts2 = new TravelSpot2(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table3().insert1(ts2);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot3":
                                                                                            ts3 = new TravelSpot3(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
                                                                                            db.table4().insert1(ts3);
                                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                                            dl.dismiss();
                                                                                            break;
                                                                                        case "spot4":
                                                                                            ts4 = new TravelSpot4(title[finalI], address[finalI], mapx[finalI], mapy[finalI]);
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
                                                                            Intent intent = new Intent(MainActivity.this, rootManage.class);
                                                                            startActivity(intent);
                                                                            dl.dismiss();

                                                                        }
                                                                    });


                                                                }
                                                            });

                                                            busFind.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    new Thread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, count = 0;
                                                                            busstop = getBus();
                                                                            stbusstop = new StringTokenizer(busstop, "*");

                                                                            String bus_citycode[] = new String[stbusstop.countTokens()];
                                                                            String busstop_mapx[] = new String[stbusstop.countTokens()];
                                                                            String busstop_mapy[] = new String[stbusstop.countTokens()];
                                                                            String busstopId[] = new String[stbusstop.countTokens()];
                                                                            String busstopNm[] = new String[stbusstop.countTokens()];

                                                                            while (stbusstop.hasMoreTokens()) {
                                                                                cutting_stbusstop = new StringTokenizer(stbusstop.nextToken(), "@");
                                                                                while (cutting_stbusstop.hasMoreTokens()) {
                                                                                    if (count % 5 == 0) {
                                                                                        bus_citycode[num1] = cutting_stbusstop.nextToken();
                                                                                        num1++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 1) {
                                                                                        busstop_mapx[num2] = cutting_stbusstop.nextToken();
                                                                                        num2++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 2) {
                                                                                        busstop_mapy[num3] = cutting_stbusstop.nextToken();
                                                                                        num3++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 3) {
                                                                                        busstopId[num4] = cutting_stbusstop.nextToken();
                                                                                        num4++;
                                                                                        count++;
                                                                                    } else if (count % 5 == 4) {
                                                                                        busstopNm[num5] = cutting_stbusstop.nextToken();
                                                                                        num5++;
                                                                                        count++;
                                                                                    }
                                                                                }
                                                                            }
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    freebusMarkers();
                                                                                    for (int i = 0; i < busstopNm.length; i++) {
                                                                                        Marker marker = new Marker();
                                                                                        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_directions_bus_24));
                                                                                        marker.setIconTintColor(Color.BLUE);
                                                                                        marker.setPosition(new LatLng(Double.parseDouble(busstop_mapx[i]), Double.parseDouble(busstop_mapy[i])));
                                                                                        marker.setMap(naverMap);
                                                                                        marker.setCaptionText(busstopNm[i]);
                                                                                        marker.setCaptionAligns(Align.Top);
                                                                                        marker.setCaptionColor(Color.BLACK);
                                                                                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                                                        marker.setCaptionTextSize(14);
                                                                                        marker.setHideCollidedSymbols(true);
                                                                                        busMarkers.add(marker);

                                                                                        int finalI = i;

                                                                                        marker.setOnClickListener(new Overlay.OnClickListener() {
                                                                                            @Override
                                                                                            public boolean onClick(@NonNull Overlay overlay) {
                                                                                                Toast.makeText(getApplicationContext(), "버스 정보를 보여드립니다", Toast.LENGTH_LONG).show();
                                                                                                new Thread(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        stationcontent = getStationContent(bus_citycode[finalI], busstopId[finalI]);
                                                                                                        arriveBus = getArriveBus(bus_citycode[finalI], busstopId[finalI]);
                                                                                                        ststation = new StringTokenizer(stationcontent, "*");
                                                                                                        int num = 0;

                                                                                                        String busnumber[] = new String[ststation.countTokens()];
                                                                                                        while(ststation.hasMoreTokens()){
                                                                                                            busnumber[num] = ststation.nextToken();
                                                                                                            Log.d("버스 종류", busnumber[num]);
                                                                                                            num++;
                                                                                                        }

                                                                                                        int num1 = 0, num2 = 0, num3 = 0, count = 0;
                                                                                                        starriebus = new StringTokenizer(arriveBus, "*");

                                                                                                        String arrtime[] = new String[starriebus.countTokens()];
                                                                                                        String routeno[] = new String[starriebus.countTokens()];
                                                                                                        String routeid[] = new String[starriebus.countTokens()];
                                                                                                        while (starriebus.hasMoreTokens()) {
                                                                                                            cutting_starrive = new StringTokenizer(starriebus.nextToken(), "@");
                                                                                                            while (cutting_starrive.hasMoreTokens()) {
                                                                                                                if (count % 3 == 0) {
                                                                                                                    arrtime[num1] = cutting_starrive.nextToken();
                                                                                                                    num1++;
                                                                                                                    count++;
                                                                                                                } else if (count % 3 == 1) {
                                                                                                                    routeno[num2] = cutting_starrive.nextToken();
                                                                                                                    num2++;
                                                                                                                    count++;
                                                                                                                } else if (count % 3 == 2) {
                                                                                                                    routeid[num3] = cutting_starrive.nextToken();
                                                                                                                    num3++;
                                                                                                                    count++;
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        runOnUiThread(new Runnable() {
                                                                                                            @Override
                                                                                                            public void run() {
                                                                                                                Dialog dl = new Dialog(MainActivity.this);
                                                                                                                dl.setContentView(R.layout.bus_layout);
                                                                                                                LinearLayout verticalLayout = dl.findViewById(R.id.busnumber_space);
                                                                                                                for (String bus : busnumber) {
                                                                                                                    TextView textView = new TextView(MainActivity.this);
                                                                                                                    textView.setText(bus);
                                                                                                                    textView.setTextSize(16);
                                                                                                                    textView.setTextColor(Color.BLACK);
                                                                                                                    verticalLayout.addView(textView);

                                                                                                                    View separator = new View(MainActivity.this);
                                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                                    separator.setLayoutParams(params);
                                                                                                                    verticalLayout.addView(separator);
                                                                                                                }

                                                                                                                LinearLayout verticalLayout2 = dl.findViewById(R.id.arrive_space);

                                                                                                                for (int i = 0; i < arrtime.length - 1; i++) {
                                                                                                                    int minIndex = i;
                                                                                                                    for (int j = i + 1; j < arrtime.length; j++) {
                                                                                                                        int time1 = Integer.parseInt(arrtime[j]);
                                                                                                                        int time2 = Integer.parseInt(arrtime[minIndex]);
                                                                                                                        if (time1 < time2) {
                                                                                                                            minIndex = j;
                                                                                                                        }
                                                                                                                    }

                                                                                                                    // 최소값과 현재 위치의 값을 교환합니다.
                                                                                                                    int tempTime = Integer.parseInt(arrtime[i]);
                                                                                                                    arrtime[i] = arrtime[minIndex];
                                                                                                                    arrtime[minIndex] = Integer.toString(tempTime);

                                                                                                                    String tempNo = routeno[i];
                                                                                                                    routeno[i] = routeno[minIndex];
                                                                                                                    routeno[minIndex] = tempNo;

                                                                                                                    String tempId = routeid[i];
                                                                                                                    routeid[i] = routeid[minIndex];
                                                                                                                    routeid[minIndex] = tempId;
                                                                                                                }

                                                                                                                for (int i = 0; i < arrtime.length; i++) {
                                                                                                                    TextView timeTextView = new TextView(MainActivity.this);
                                                                                                                    int time = Integer.parseInt(arrtime[i]);
                                                                                                                    time = time/60;
                                                                                                                    String times = Integer.toString(time);
                                                                                                                    timeTextView.setText(times+"분");
                                                                                                                    timeTextView.setTextSize(16);
                                                                                                                    timeTextView.setTextColor(Color.BLACK);

                                                                                                                    TextView numberTextView = new TextView(MainActivity.this);
                                                                                                                    numberTextView.setText(routeno[i]);
                                                                                                                    numberTextView.setTextSize(16);
                                                                                                                    numberTextView.setTextColor(Color.BLACK);

                                                                                                                    TextView stTextView = new TextView(MainActivity.this);
                                                                                                                    stTextView.setText(routeid[i]);
                                                                                                                    stTextView.setTextSize(16);
                                                                                                                    stTextView.setTextColor(Color.BLACK);

                                                                                                                    LinearLayout horizontalLayout = new LinearLayout(MainActivity.this);
                                                                                                                    horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                                                                                    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

                                                                                                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                                                                                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                                                                                                    timeTextView.setLayoutParams(layoutParams);
                                                                                                                    numberTextView.setLayoutParams(layoutParams);
                                                                                                                    stTextView.setLayoutParams(layoutParams);

                                                                                                                    horizontalLayout.addView(timeTextView);
                                                                                                                    horizontalLayout.addView(numberTextView);
                                                                                                                    horizontalLayout.addView(stTextView);
                                                                                                                    verticalLayout2.addView(horizontalLayout);

                                                                                                                    View separator = new View(MainActivity.this);
                                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                                    separator.setLayoutParams(params);
                                                                                                                    verticalLayout2.addView(separator);
                                                                                                                }
                                                                                                                dl.show();
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                }).start();

                                                                                                return false;
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }).start();
                                                                }
                                                            });
                                                            return false;
                                                        }
                                                    });

                                                }
                                                LatLng newLatLng = new LatLng(Double.parseDouble(mapy[0]), Double.parseDouble(mapx[0]));
                                                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(newLatLng, 12).animate(CameraAnimation.Fly, 3000);
                                                naverMap.moveCamera(cameraUpdate);
                                            }
                                        });
                                    }
                                }).start();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"검색 결과 없음", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        foodButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                freemMarkers();
                freebusMarkers();
                freecycleMarkers();
                info_Layout.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "200m 내의 음식점을 찾습니다", Toast.LENGTH_LONG).show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        restaurant = getFood();
                        int num1 = 0;
                        int num2 = 0;
                        int num3 = 0;
                        int num4 = 0;
                        int num = 0;

                        stfood = new StringTokenizer(restaurant, "*");

                        String resNm[] = new String[stfood.countTokens()];
                        String resAddr[] = new String[stfood.countTokens()];
                        String res_mapx[] = new String[stfood.countTokens()];
                        String res_mapy[] = new String[stfood.countTokens()];

                        while (stfood.hasMoreTokens()) {
                            cutting_stfood = new StringTokenizer(stfood.nextToken(), "@");
                            while (cutting_stfood.hasMoreTokens()) {
                                if (num % 4 == 0) {
                                    resNm[num1] = cutting_stfood.nextToken();
                                    num1++;
                                    num++;
                                } else if (num % 4 == 1) {
                                    resAddr[num2] = cutting_stfood.nextToken();
                                    num2++;
                                    num++;
                                } else if (num % 4 == 2) {
                                    res_mapx[num3] = cutting_stfood.nextToken();
                                    num3++;
                                    num++;
                                }else if (num % 4 == 3) {
                                    res_mapy[num4] = cutting_stfood.nextToken();
                                    num4++;
                                    num++;
                                }
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "음식점을 찾았습니다", Toast.LENGTH_SHORT).show();
                                for (int i = 0; i < resNm.length; i++) {
                                    Marker marker = new Marker();
                                    marker.setIconTintColor(Color.BLUE);
                                    marker.setPosition(new LatLng(Double.parseDouble(res_mapy[i]), Double.parseDouble(res_mapx[i])));
                                    marker.setMap(naverMap);
                                    marker.setCaptionText(resNm[i]);
                                    marker.setCaptionAligns(Align.Top);
                                    marker.setCaptionColor(Color.BLACK);
                                    marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                    marker.setCaptionTextSize(14);
                                    marker.setHideCollidedSymbols(true);
                                    mMarkers.add(marker);

                                    int finalI = i;


                                    marker.setOnClickListener(new Overlay.OnClickListener() {
                                        private String close_time;
                                        private String state_open;
                                        @Override
                                        public boolean onClick(@NonNull Overlay overlay) {
                                            getMapInfoName.setText(resNm[finalI]);
                                            getMapInfoAddr.setText(resAddr[finalI]);
                                            String place = URLEncoder.encode(resNm[finalI]);
                                            String url = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%B0%BD%EC%9B%90%20"+place;

                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    org.jsoup.nodes.Document jsoupDoc = null;
                                                    try {
                                                        jsoupDoc = Jsoup.connect(url).get();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    Elements elements = jsoupDoc.select(".place_section_content"); // 영업시간이 포함된 엘리먼트 선택
                                                    Log.d("elements", "결과값: " + elements.text());
                                                    Elements map_page = jsoupDoc.select("#_title");
                                                    Log.d("지도 페이지: ", map_page.toString());
                                                    String going_map = map_page.select("a").attr("href");;
                                                    Log.d("지도 주소: ", going_map);

                                                    Elements imgpage = jsoupDoc.select("div.K0PDV._div#ibu_1");
                                                    Log.d("이미지 url 포함한 부분 : ", imgpage.toString());


                                                    getImageSearchResult("창원" + resNm[finalI], new SearchCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            Log.d("성공?: ", result);
                                                            ImageUrl = ImageparseXML(result);
                                                            Log.d("link : ", ImageUrl);
                                                            if(ImageUrl.equals("")){
                                                                imageView.setImageResource(R.drawable.nonfood);
                                                            } else {
                                                                String imageUrl = ImageUrl;
                                                                new LoadImageTask().execute(imageUrl);
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Exception e) {

                                                        }
                                                    });

                                                    Elements open_state = elements.select(".A_cdD");
                                                    Log.d("제발: ", open_state.toString());
                                                    Elements time_close = elements.select(".U7pYf");
                                                    Log.d("제발: ", time_close.toString());
                                                    String openstate = open_state.select("em").text();
                                                    Log.d("영업여부 : ", openstate);
                                                    String closingTime = time_close.select("span.place_blind").text();
                                                    Log.d("영업종료 시간은? : ", closingTime);
                                                    close_time = closingTime;
                                                    state_open = openstate;

                                                    getCloseTime.setText(close_time);
                                                    getOpenState.setText(state_open);

                                                }
                                            }).start();

                                            info_Layout.setVisibility(View.VISIBLE);

                                            infoClose.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    info_Layout.setVisibility(View.INVISIBLE);
                                                }
                                            });

                                            rootAdd.setOnClickListener(new View.OnClickListener() {


                                                @Override
                                                public void onClick(View view) {

                                                    Dialog dl = new Dialog(MainActivity.this);
                                                    dl.setContentView(R.layout.activity_custom_dialog);
                                                    List<Data> dataList = db.dataDao().getAll();

                                                    LinearLayout layout = (LinearLayout) dl.findViewById(R.id.dynamic_space);
                                                    LinearLayout layout_below = (LinearLayout) dl.findViewById(R.id.btn_space);
                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                            0, // width
                                                            LinearLayout.LayoutParams.WRAP_CONTENT // height
                                                    );
                                                    params.weight = 1f;
                                                    RadioGroup radioGroup = new RadioGroup(MainActivity.this);
                                                    radioGroup.setOrientation(RadioGroup.VERTICAL);
                                                    Button addbtn = new Button(MainActivity.this);
                                                    addbtn.setText("일정 추가");
                                                    addbtn.setLayoutParams(params);
                                                    Button newbtn = new Button(MainActivity.this);
                                                    newbtn.setText("새 일정");
                                                    newbtn.setLayoutParams(params);
                                                    layout_below.addView(addbtn);
                                                    layout_below.addView(newbtn);

                                                    for(Data data:dataList){
                                                        RadioButton checkBox = new RadioButton(MainActivity.this);
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
                                                                            TravelSpot ts0 = new TravelSpot(resNm[finalI], resAddr[finalI], res_mapx[finalI], res_mapy[finalI]);
                                                                            db.table1().insert(ts0);
                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                            dl.dismiss();
                                                                            Log.d("저장된 장소", ts0.getName());
                                                                            break;
                                                                        case "spot1":
                                                                            ts1 = new TravelSpot1(resNm[finalI], resAddr[finalI], res_mapx[finalI], res_mapy[finalI]);
                                                                            db.table2().insert1(ts1);
                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                            dl.dismiss();
                                                                            break;
                                                                        case "spot2":
                                                                            ts2 = new TravelSpot2(resNm[finalI], resAddr[finalI], res_mapx[finalI], res_mapy[finalI]);
                                                                            db.table3().insert1(ts2);
                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                            dl.dismiss();
                                                                            break;
                                                                        case "spot3":
                                                                            ts3 = new TravelSpot3(resNm[finalI], resAddr[finalI], res_mapx[finalI], res_mapy[finalI]);
                                                                            db.table4().insert1(ts3);
                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                            dl.dismiss();
                                                                            break;
                                                                        case "spot4":
                                                                            ts4 = new TravelSpot4(resNm[finalI], resAddr[finalI], res_mapx[finalI], res_mapy[finalI]);
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
                                                            Intent intent = new Intent(MainActivity.this, rootManage.class);
                                                            startActivity(intent);
                                                            dl.dismiss();

                                                        }
                                                    });


                                                }
                                            });

                                            busFind.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, count = 0;
                                                            busstop = getBus();
                                                            stbusstop = new StringTokenizer(busstop, "*");

                                                            String bus_citycode[] = new String[stbusstop.countTokens()];
                                                            String busstop_mapx[] = new String[stbusstop.countTokens()];
                                                            String busstop_mapy[] = new String[stbusstop.countTokens()];
                                                            String busstopId[] = new String[stbusstop.countTokens()];
                                                            String busstopNm[] = new String[stbusstop.countTokens()];

                                                            while (stbusstop.hasMoreTokens()) {
                                                                cutting_stbusstop = new StringTokenizer(stbusstop.nextToken(), "@");
                                                                while (cutting_stbusstop.hasMoreTokens()) {
                                                                    if (count % 5 == 0) {
                                                                        bus_citycode[num1] = cutting_stbusstop.nextToken();
                                                                        num1++;
                                                                        count++;
                                                                    } else if (count % 5 == 1) {
                                                                        busstop_mapx[num2] = cutting_stbusstop.nextToken();
                                                                        num2++;
                                                                        count++;
                                                                    } else if (count % 5 == 2) {
                                                                        busstop_mapy[num3] = cutting_stbusstop.nextToken();
                                                                        num3++;
                                                                        count++;
                                                                    } else if (count % 5 == 3) {
                                                                        busstopId[num4] = cutting_stbusstop.nextToken();
                                                                        num4++;
                                                                        count++;
                                                                    } else if (count % 5 == 4) {
                                                                        busstopNm[num5] = cutting_stbusstop.nextToken();
                                                                        num5++;
                                                                        count++;
                                                                    }
                                                                }
                                                            }
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    freebusMarkers();
                                                                    for (int i = 0; i < busstopNm.length; i++) {
                                                                        Marker marker = new Marker();
                                                                        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_directions_bus_24));
                                                                        marker.setIconTintColor(Color.BLUE);
                                                                        marker.setPosition(new LatLng(Double.parseDouble(busstop_mapx[i]), Double.parseDouble(busstop_mapy[i])));
                                                                        marker.setMap(naverMap);
                                                                        marker.setCaptionText(busstopNm[i]);
                                                                        marker.setCaptionAligns(Align.Top);
                                                                        marker.setCaptionColor(Color.BLACK);
                                                                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                                        marker.setCaptionTextSize(14);
                                                                        marker.setHideCollidedSymbols(true);
                                                                        busMarkers.add(marker);

                                                                        int finalI = i;

                                                                        marker.setOnClickListener(new Overlay.OnClickListener() {
                                                                            @Override
                                                                            public boolean onClick(@NonNull Overlay overlay) {
                                                                                Toast.makeText(getApplicationContext(), "버스 정보를 보여드립니다", Toast.LENGTH_LONG).show();
                                                                                new Thread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        stationcontent = getStationContent(bus_citycode[finalI], busstopId[finalI]);
                                                                                        arriveBus = getArriveBus(bus_citycode[finalI], busstopId[finalI]);
                                                                                        ststation = new StringTokenizer(stationcontent, "*");
                                                                                        int num = 0;

                                                                                        String busnumber[] = new String[ststation.countTokens()];
                                                                                        while(ststation.hasMoreTokens()){
                                                                                            busnumber[num] = ststation.nextToken();
                                                                                            Log.d("버스 종류", busnumber[num]);
                                                                                            num++;
                                                                                        }

                                                                                        int num1 = 0, num2 = 0, num3 = 0, count = 0;
                                                                                        starriebus = new StringTokenizer(arriveBus, "*");

                                                                                        String arrtime[] = new String[starriebus.countTokens()];
                                                                                        String routeno[] = new String[starriebus.countTokens()];
                                                                                        String routeid[] = new String[starriebus.countTokens()];
                                                                                        while (starriebus.hasMoreTokens()) {
                                                                                            cutting_starrive = new StringTokenizer(starriebus.nextToken(), "@");
                                                                                            while (cutting_starrive.hasMoreTokens()) {
                                                                                                if (count % 3 == 0) {
                                                                                                    arrtime[num1] = cutting_starrive.nextToken();
                                                                                                    num1++;
                                                                                                    count++;
                                                                                                } else if (count % 3 == 1) {
                                                                                                    routeno[num2] = cutting_starrive.nextToken();
                                                                                                    num2++;
                                                                                                    count++;
                                                                                                } else if (count % 3 == 2) {
                                                                                                    routeid[num3] = cutting_starrive.nextToken();
                                                                                                    num3++;
                                                                                                    count++;
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        runOnUiThread(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                Dialog dl = new Dialog(MainActivity.this);
                                                                                                dl.setContentView(R.layout.bus_layout);
                                                                                                LinearLayout verticalLayout = dl.findViewById(R.id.busnumber_space);
                                                                                                for (String bus : busnumber) {
                                                                                                    TextView textView = new TextView(MainActivity.this);
                                                                                                    textView.setText(bus);
                                                                                                    textView.setTextSize(16);
                                                                                                    textView.setTextColor(Color.BLACK);
                                                                                                    verticalLayout.addView(textView);

                                                                                                    View separator = new View(MainActivity.this);
                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                    separator.setLayoutParams(params);
                                                                                                    verticalLayout.addView(separator);
                                                                                                }

                                                                                                LinearLayout verticalLayout2 = dl.findViewById(R.id.arrive_space);

                                                                                                for (int i = 0; i < arrtime.length - 1; i++) {
                                                                                                    int minIndex = i;
                                                                                                    for (int j = i + 1; j < arrtime.length; j++) {
                                                                                                        int time1 = Integer.parseInt(arrtime[j]);
                                                                                                        int time2 = Integer.parseInt(arrtime[minIndex]);
                                                                                                        if (time1 < time2) {
                                                                                                            minIndex = j;
                                                                                                        }
                                                                                                    }

                                                                                                    // 최소값과 현재 위치의 값을 교환합니다.
                                                                                                    int tempTime = Integer.parseInt(arrtime[i]);
                                                                                                    arrtime[i] = arrtime[minIndex];
                                                                                                    arrtime[minIndex] = Integer.toString(tempTime);

                                                                                                    String tempNo = routeno[i];
                                                                                                    routeno[i] = routeno[minIndex];
                                                                                                    routeno[minIndex] = tempNo;

                                                                                                    String tempId = routeid[i];
                                                                                                    routeid[i] = routeid[minIndex];
                                                                                                    routeid[minIndex] = tempId;
                                                                                                }

                                                                                                for (int i = 0; i < arrtime.length; i++) {
                                                                                                    TextView timeTextView = new TextView(MainActivity.this);
                                                                                                    int time = Integer.parseInt(arrtime[i]);
                                                                                                    time = time/60;
                                                                                                    String times = Integer.toString(time);
                                                                                                    timeTextView.setText(times+"분");
                                                                                                    timeTextView.setTextSize(16);
                                                                                                    timeTextView.setTextColor(Color.BLACK);

                                                                                                    TextView numberTextView = new TextView(MainActivity.this);
                                                                                                    numberTextView.setText(routeno[i]);
                                                                                                    numberTextView.setTextSize(16);
                                                                                                    numberTextView.setTextColor(Color.BLACK);

                                                                                                    TextView stTextView = new TextView(MainActivity.this);
                                                                                                    stTextView.setText(routeid[i]);
                                                                                                    stTextView.setTextSize(16);
                                                                                                    stTextView.setTextColor(Color.BLACK);

                                                                                                    LinearLayout horizontalLayout = new LinearLayout(MainActivity.this);
                                                                                                    horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                                                                    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

                                                                                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                                                                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                                                                                    timeTextView.setLayoutParams(layoutParams);
                                                                                                    numberTextView.setLayoutParams(layoutParams);
                                                                                                    stTextView.setLayoutParams(layoutParams);

                                                                                                    horizontalLayout.addView(timeTextView);
                                                                                                    horizontalLayout.addView(numberTextView);
                                                                                                    horizontalLayout.addView(stTextView);
                                                                                                    verticalLayout2.addView(horizontalLayout);

                                                                                                    View separator = new View(MainActivity.this);
                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                    separator.setLayoutParams(params);
                                                                                                    verticalLayout2.addView(separator);
                                                                                                }
                                                                                                dl.show();
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }).start();

                                                                                return false;
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }).start();
                                                }
                                            });

                                            cycleButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            int num1 = 0, num2 = 0, num3 = 0, count = 0;
                                                            nubiza= getNubiza();
                                                            stnubiza = new StringTokenizer(nubiza, "*");

                                                            String nubiza_station[] = new String[stnubiza.countTokens()];
                                                            String nubiza_mapx[] = new String[stnubiza.countTokens()];
                                                            String nubiza_mapy[] = new String[stnubiza.countTokens()];


                                                            while (stnubiza.hasMoreTokens()) {
                                                                cutting_nubiza = new StringTokenizer(stnubiza.nextToken(), "@");
                                                                while (cutting_nubiza.hasMoreTokens()) {
                                                                    if (count % 3 == 0) {
                                                                        nubiza_station[num1] = cutting_nubiza.nextToken();
                                                                        num1++;
                                                                        count++;
                                                                    } else if (count % 3 == 1) {
                                                                        nubiza_mapx[num2] = cutting_nubiza.nextToken();
                                                                        num2++;
                                                                        count++;
                                                                    } else if (count % 3 == 2) {
                                                                        nubiza_mapy[num3] = cutting_nubiza.nextToken();
                                                                        num3++;
                                                                        count++;
                                                                    }
                                                                }
                                                            }
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    freecycleMarkers();

                                                                    markersPosition = new Vector<LatLng>();
                                                                    for (int i = 0; i < nubiza_station.length; i++) {
                                                                        markersPosition.add(new LatLng(Double.parseDouble(nubiza_mapx[i]), Double.parseDouble(nubiza_mapy[i])));

                                                                    }

                                                                    LatLng currentPosition = getCurrentPosition(naverMap);
                                                                    for (int i = 0; i < markersPosition.size(); i++) {
                                                                        LatLng markerPosition = markersPosition.get(i);
                                                                        if (!withinSightMarker(currentPosition, markerPosition))
                                                                            continue;
                                                                        Marker marker = new Marker();
                                                                        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_directions_bike_24));
                                                                        marker.setIconTintColor(Color.GREEN);
                                                                        marker.setPosition(markerPosition);
                                                                        marker.setMap(naverMap);
                                                                        marker.setCaptionText(nubiza_station[i]);
                                                                        marker.setCaptionAligns(Align.Top);
                                                                        marker.setCaptionColor(Color.BLACK);
                                                                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                                        marker.setCaptionTextSize(14);
                                                                        marker.setHideCollidedSymbols(true);
                                                                        cycleMarkers.add(marker);

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }).start();
                                                }
                                            });

                                            return false;
                                        }
                                    });
                                }
                                LatLng currentPosition = getCurrentPosition(naverMap);
                                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(new LatLng(currentPosition.latitude, currentPosition.longitude), 16).animate(CameraAnimation.Fly, 3000);
                                naverMap.moveCamera(cameraUpdate);
                            }
                        });
                    }
                }).start();
            }
        });


        cafeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "200m 내의 카페를 찾습니다", Toast.LENGTH_LONG).show();


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int num1 = 0, num2 = 0, num3 = 0, num4 = 0, count = 0;
                        cafe = getCafe();
                        stcafe = new StringTokenizer(cafe, "*");

                        String cafeNm[] = new String[stcafe.countTokens()];
                        String cafeAddr[] = new String[stcafe.countTokens()];
                        String cafe_mapx[] = new String[stcafe.countTokens()];
                        String cafe_mapy[] = new String[stcafe.countTokens()];

                        while (stcafe.hasMoreTokens()) {
                            cutting_stcafe = new StringTokenizer(stcafe.nextToken(), "@");
                            while (cutting_stcafe.hasMoreTokens()) {
                                if (count % 4 == 0) {
                                    cafeNm[num1] = cutting_stcafe.nextToken();
                                    num1++;
                                    count++;
                                } else if (count % 4 == 1) {
                                    cafeAddr[num2] = cutting_stcafe.nextToken();
                                    num2++;
                                    count++;
                                } else if (count % 4 == 2) {
                                    cafe_mapx[num3] = cutting_stcafe.nextToken();
                                    num3++;
                                    count++;
                                }else if (count % 4 == 3) {
                                    cafe_mapy[num4] = cutting_stcafe.nextToken();
                                    num4++;
                                    count++;
                                }
                            }

                        }
                        runOnUiThread(new Runnable() {
                            int num = 0;

                            @Override
                            public void run() {
                                freemMarkers();
                                freebusMarkers();
                                freecycleMarkers();
                                for (int i = 0; i < cafeNm.length; i++) {
                                    num++;

                                    //Log.d("위도경도", cafe_mapx[i] + " " + cafe_mapy[i] + " " + cafeNm[i]);
                                    Marker marker = new Marker();
                                    marker.setIconTintColor(Color.BLUE);

                                    marker.setPosition(new LatLng(Double.parseDouble(cafe_mapy[i]), Double.parseDouble(cafe_mapx[i])));
                                    marker.setMap(naverMap);
                                    marker.setCaptionText(cafeNm[i]);
                                    marker.setCaptionAligns(Align.Top);
                                    marker.setCaptionColor(Color.BLACK);
                                    marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                    marker.setCaptionTextSize(14);
                                    marker.setHideCollidedSymbols(true);
                                    mMarkers.add(marker);
                                    Log.d("위도경도", cafe_mapx[i] + " " + cafe_mapy[i] + " " + cafeNm[i]);

                                    int finalI = i;


                                    marker.setOnClickListener(new Overlay.OnClickListener() {
                                        private String close_time;
                                        private String state_open;
                                        @Override
                                        public boolean onClick(@NonNull Overlay overlay) {
                                            getMapInfoName.setText(cafeNm[finalI]);
                                            getMapInfoAddr.setText(cafeAddr[finalI]);
                                            String place = URLEncoder.encode(cafeNm[finalI]);
                                            String url = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%B0%BD%EC%9B%90%20"+place;

                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    org.jsoup.nodes.Document jsoupDoc = null;
                                                    try {
                                                        jsoupDoc = Jsoup.connect(url).get();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    Elements elements = jsoupDoc.select(".place_section_content"); // 영업시간이 포함된 엘리먼트 선택
                                                    Log.d("elements", "결과값: " + elements);
                                                    Elements map_page = jsoupDoc.select("#_title");
                                                    Log.d("지도 페이지: ", map_page.toString());
                                                    String going_map = map_page.select("a").attr("href");;
                                                    Log.d("지도 주소: ", going_map);

                                                    Elements imgpage = jsoupDoc.select("div.K0PDV._div#ibu_1");
                                                    Log.d("이미지 url 포함한 부분 : ", imgpage.toString());


                                                    getImageSearchResult("창원" + cafeNm[finalI], new SearchCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            Log.d("성공?: ", result);
                                                            ImageUrl = ImageparseXML(result);
                                                            Log.d("link : ", ImageUrl);
                                                            if(ImageUrl.equals("")){
                                                                imageView.setImageResource(R.drawable.noncafe);
                                                            } else {
                                                                String imageUrl = ImageUrl;
                                                                new LoadImageTask().execute(imageUrl);
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Exception e) {

                                                        }
                                                    });

                                                    Elements open_state = elements.select(".A_cdD");
                                                    Log.d("제발: ", open_state.toString());
                                                    Elements time_close = elements.select(".U7pYf");
                                                    Log.d("제발: ", time_close.toString());
                                                    String openstate = open_state.select("em").text();
                                                    Log.d("영업여부 : ", openstate);
                                                    String closingTime = time_close.select("span.place_blind").text();
                                                    Log.d("영업종료 시간은? : ", closingTime);
                                                    close_time = closingTime;
                                                    state_open = openstate;

                                                    getCloseTime.setText(close_time);
                                                    getOpenState.setText(state_open);

                                                }
                                            }).start();

                                            info_Layout.setVisibility(View.VISIBLE);

                                            infoClose.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    info_Layout.setVisibility(View.INVISIBLE);
                                                }
                                            });

                                            rootAdd.setOnClickListener(new View.OnClickListener() {


                                                @Override
                                                public void onClick(View view) {

                                                    Dialog dl = new Dialog(MainActivity.this);
                                                    dl.setContentView(R.layout.activity_custom_dialog);
                                                    List<Data> dataList = db.dataDao().getAll();

                                                    LinearLayout layout = (LinearLayout) dl.findViewById(R.id.dynamic_space);
                                                    LinearLayout layout_below = (LinearLayout) dl.findViewById(R.id.btn_space);
                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                            0, // width
                                                            LinearLayout.LayoutParams.WRAP_CONTENT // height
                                                    );
                                                    params.weight = 1f;
                                                    RadioGroup radioGroup = new RadioGroup(MainActivity.this);
                                                    radioGroup.setOrientation(RadioGroup.VERTICAL);
                                                    Button addbtn = new Button(MainActivity.this);
                                                    addbtn.setText("일정 추가");
                                                    addbtn.setLayoutParams(params);
                                                    Button newbtn = new Button(MainActivity.this);
                                                    newbtn.setText("새 일정");
                                                    newbtn.setLayoutParams(params);
                                                    layout_below.addView(addbtn);
                                                    layout_below.addView(newbtn);

                                                    for(Data data:dataList){
                                                        RadioButton checkBox = new RadioButton(MainActivity.this);
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
                                                                            TravelSpot ts0 = new TravelSpot(cafeNm[finalI], cafeAddr[finalI], cafe_mapx[finalI], cafe_mapy[finalI]);
                                                                            db.table1().insert(ts0);
                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                            dl.dismiss();
                                                                            Log.d("저장된 장소", ts0.getName());
                                                                            break;
                                                                        case "spot1":
                                                                            ts1 = new TravelSpot1(cafeNm[finalI], cafeAddr[finalI], cafe_mapx[finalI], cafe_mapy[finalI]);
                                                                            db.table2().insert1(ts1);
                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                            dl.dismiss();
                                                                            break;
                                                                        case "spot2":
                                                                            ts2 = new TravelSpot2(cafeNm[finalI], cafeAddr[finalI], cafe_mapx[finalI], cafe_mapy[finalI]);
                                                                            db.table3().insert1(ts2);
                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                            dl.dismiss();
                                                                            break;
                                                                        case "spot3":
                                                                            ts3 = new TravelSpot3(cafeNm[finalI], cafeAddr[finalI], cafe_mapx[finalI], cafe_mapy[finalI]);
                                                                            db.table4().insert1(ts3);
                                                                            Toast.makeText(getApplicationContext(),click_name+"에 추가되었습니다", Toast.LENGTH_LONG).show();
                                                                            dl.dismiss();
                                                                            break;
                                                                        case "spot4":
                                                                            ts4 = new TravelSpot4(cafeNm[finalI], cafeAddr[finalI], cafe_mapx[finalI], cafe_mapy[finalI]);
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
                                                            Intent intent = new Intent(MainActivity.this, rootManage.class);
                                                            startActivity(intent);
                                                            dl.dismiss();

                                                        }
                                                    });


                                                }
                                            });

                                            busFind.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, count = 0;
                                                            busstop = getBus();
                                                            stbusstop = new StringTokenizer(busstop, "*");

                                                            String bus_citycode[] = new String[stbusstop.countTokens()];
                                                            String busstop_mapx[] = new String[stbusstop.countTokens()];
                                                            String busstop_mapy[] = new String[stbusstop.countTokens()];
                                                            String busstopId[] = new String[stbusstop.countTokens()];
                                                            String busstopNm[] = new String[stbusstop.countTokens()];

                                                            while (stbusstop.hasMoreTokens()) {
                                                                cutting_stbusstop = new StringTokenizer(stbusstop.nextToken(), "@");
                                                                while (cutting_stbusstop.hasMoreTokens()) {
                                                                    if (count % 5 == 0) {
                                                                        bus_citycode[num1] = cutting_stbusstop.nextToken();
                                                                        num1++;
                                                                        count++;
                                                                    } else if (count % 5 == 1) {
                                                                        busstop_mapx[num2] = cutting_stbusstop.nextToken();
                                                                        num2++;
                                                                        count++;
                                                                    } else if (count % 5 == 2) {
                                                                        busstop_mapy[num3] = cutting_stbusstop.nextToken();
                                                                        num3++;
                                                                        count++;
                                                                    } else if (count % 5 == 3) {
                                                                        busstopId[num4] = cutting_stbusstop.nextToken();
                                                                        num4++;
                                                                        count++;
                                                                    } else if (count % 5 == 4) {
                                                                        busstopNm[num5] = cutting_stbusstop.nextToken();
                                                                        num5++;
                                                                        count++;
                                                                    }
                                                                }
                                                            }
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    freebusMarkers();
                                                                    for (int i = 0; i < busstopNm.length; i++) {
                                                                        Marker marker = new Marker();
                                                                        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_directions_bus_24));
                                                                        marker.setIconTintColor(Color.BLUE);
                                                                        marker.setPosition(new LatLng(Double.parseDouble(busstop_mapx[i]), Double.parseDouble(busstop_mapy[i])));
                                                                        marker.setMap(naverMap);
                                                                        marker.setCaptionText(busstopNm[i]);
                                                                        marker.setCaptionAligns(Align.Top);
                                                                        marker.setCaptionColor(Color.BLACK);
                                                                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                                        marker.setCaptionTextSize(14);
                                                                        marker.setHideCollidedSymbols(true);
                                                                        busMarkers.add(marker);

                                                                        int finalI = i;

                                                                        marker.setOnClickListener(new Overlay.OnClickListener() {
                                                                            @Override
                                                                            public boolean onClick(@NonNull Overlay overlay) {
                                                                                Toast.makeText(getApplicationContext(), "버스 정보를 보여드립니다", Toast.LENGTH_LONG).show();
                                                                                new Thread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        stationcontent = getStationContent(bus_citycode[finalI], busstopId[finalI]);
                                                                                        arriveBus = getArriveBus(bus_citycode[finalI], busstopId[finalI]);
                                                                                        ststation = new StringTokenizer(stationcontent, "*");
                                                                                        int num = 0;

                                                                                        String busnumber[] = new String[ststation.countTokens()];
                                                                                        while(ststation.hasMoreTokens()){
                                                                                            busnumber[num] = ststation.nextToken();
                                                                                            Log.d("버스 종류", busnumber[num]);
                                                                                            num++;
                                                                                        }

                                                                                        int num1 = 0, num2 = 0, num3 = 0, count = 0;
                                                                                        starriebus = new StringTokenizer(arriveBus, "*");

                                                                                        String arrtime[] = new String[starriebus.countTokens()];
                                                                                        String routeno[] = new String[starriebus.countTokens()];
                                                                                        String routeid[] = new String[starriebus.countTokens()];
                                                                                        while (starriebus.hasMoreTokens()) {
                                                                                            cutting_starrive = new StringTokenizer(starriebus.nextToken(), "@");
                                                                                            while (cutting_starrive.hasMoreTokens()) {
                                                                                                if (count % 3 == 0) {
                                                                                                    arrtime[num1] = cutting_starrive.nextToken();
                                                                                                    num1++;
                                                                                                    count++;
                                                                                                } else if (count % 3 == 1) {
                                                                                                    routeno[num2] = cutting_starrive.nextToken();
                                                                                                    num2++;
                                                                                                    count++;
                                                                                                } else if (count % 3 == 2) {
                                                                                                    routeid[num3] = cutting_starrive.nextToken();
                                                                                                    num3++;
                                                                                                    count++;
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        runOnUiThread(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                Dialog dl = new Dialog(MainActivity.this);
                                                                                                dl.setContentView(R.layout.bus_layout);
                                                                                                LinearLayout verticalLayout = dl.findViewById(R.id.busnumber_space);
                                                                                                for (String bus : busnumber) {
                                                                                                    TextView textView = new TextView(MainActivity.this);
                                                                                                    textView.setText(bus);
                                                                                                    textView.setTextSize(16);
                                                                                                    textView.setTextColor(Color.BLACK);
                                                                                                    verticalLayout.addView(textView);

                                                                                                    View separator = new View(MainActivity.this);
                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                    separator.setLayoutParams(params);
                                                                                                    verticalLayout.addView(separator);
                                                                                                }

                                                                                                LinearLayout verticalLayout2 = dl.findViewById(R.id.arrive_space);

                                                                                                for (int i = 0; i < arrtime.length - 1; i++) {
                                                                                                    int minIndex = i;
                                                                                                    for (int j = i + 1; j < arrtime.length; j++) {
                                                                                                        int time1 = Integer.parseInt(arrtime[j]);
                                                                                                        int time2 = Integer.parseInt(arrtime[minIndex]);
                                                                                                        if (time1 < time2) {
                                                                                                            minIndex = j;
                                                                                                        }
                                                                                                    }

                                                                                                    // 최소값과 현재 위치의 값을 교환합니다.
                                                                                                    int tempTime = Integer.parseInt(arrtime[i]);
                                                                                                    arrtime[i] = arrtime[minIndex];
                                                                                                    arrtime[minIndex] = Integer.toString(tempTime);

                                                                                                    String tempNo = routeno[i];
                                                                                                    routeno[i] = routeno[minIndex];
                                                                                                    routeno[minIndex] = tempNo;

                                                                                                    String tempId = routeid[i];
                                                                                                    routeid[i] = routeid[minIndex];
                                                                                                    routeid[minIndex] = tempId;
                                                                                                }

                                                                                                for (int i = 0; i < arrtime.length; i++) {
                                                                                                    TextView timeTextView = new TextView(MainActivity.this);
                                                                                                    int time = Integer.parseInt(arrtime[i]);
                                                                                                    time = time/60;
                                                                                                    String times = Integer.toString(time);
                                                                                                    timeTextView.setText(times+"분");
                                                                                                    timeTextView.setTextSize(16);
                                                                                                    timeTextView.setTextColor(Color.BLACK);

                                                                                                    TextView numberTextView = new TextView(MainActivity.this);
                                                                                                    numberTextView.setText(routeno[i]);
                                                                                                    numberTextView.setTextSize(16);
                                                                                                    numberTextView.setTextColor(Color.BLACK);

                                                                                                    TextView stTextView = new TextView(MainActivity.this);
                                                                                                    stTextView.setText(routeid[i]);
                                                                                                    stTextView.setTextSize(16);
                                                                                                    stTextView.setTextColor(Color.BLACK);

                                                                                                    LinearLayout horizontalLayout = new LinearLayout(MainActivity.this);
                                                                                                    horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                                                                    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

                                                                                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                                                                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                                                                                    timeTextView.setLayoutParams(layoutParams);
                                                                                                    numberTextView.setLayoutParams(layoutParams);
                                                                                                    stTextView.setLayoutParams(layoutParams);

                                                                                                    horizontalLayout.addView(timeTextView);
                                                                                                    horizontalLayout.addView(numberTextView);
                                                                                                    horizontalLayout.addView(stTextView);
                                                                                                    verticalLayout2.addView(horizontalLayout);

                                                                                                    View separator = new View(MainActivity.this);
                                                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                                                    separator.setLayoutParams(params);
                                                                                                    verticalLayout2.addView(separator);
                                                                                                }
                                                                                                dl.show();
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }).start();

                                                                                return false;
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }).start();
                                                }
                                            });

                                            cycleButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            int num1 = 0, num2 = 0, num3 = 0, count = 0;
                                                            nubiza= getNubiza();
                                                            stnubiza = new StringTokenizer(nubiza, "*");

                                                            String nubiza_station[] = new String[stnubiza.countTokens()];
                                                            String nubiza_mapx[] = new String[stnubiza.countTokens()];
                                                            String nubiza_mapy[] = new String[stnubiza.countTokens()];


                                                            while (stnubiza.hasMoreTokens()) {
                                                                cutting_nubiza = new StringTokenizer(stnubiza.nextToken(), "@");
                                                                while (cutting_nubiza.hasMoreTokens()) {
                                                                    if (count % 3 == 0) {
                                                                        nubiza_station[num1] = cutting_nubiza.nextToken();
                                                                        num1++;
                                                                        count++;
                                                                    } else if (count % 3 == 1) {
                                                                        nubiza_mapx[num2] = cutting_nubiza.nextToken();
                                                                        num2++;
                                                                        count++;
                                                                    } else if (count % 3 == 2) {
                                                                        nubiza_mapy[num3] = cutting_nubiza.nextToken();
                                                                        num3++;
                                                                        count++;
                                                                    }
                                                                }
                                                            }
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    freecycleMarkers();

                                                                    markersPosition = new Vector<LatLng>();
                                                                    for (int i = 0; i < nubiza_station.length; i++) {
                                                                        markersPosition.add(new LatLng(Double.parseDouble(nubiza_mapx[i]), Double.parseDouble(nubiza_mapy[i])));

                                                                    }

                                                                    LatLng currentPosition = getCurrentPosition(naverMap);
                                                                    for (int i = 0; i < markersPosition.size(); i++) {
                                                                        LatLng markerPosition = markersPosition.get(i);
                                                                        if (!withinSightMarker(currentPosition, markerPosition))
                                                                            continue;
                                                                        Marker marker = new Marker();
                                                                        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_directions_bike_24));
                                                                        marker.setIconTintColor(Color.GREEN);
                                                                        marker.setPosition(markerPosition);
                                                                        marker.setMap(naverMap);
                                                                        marker.setCaptionText(nubiza_station[i]);
                                                                        marker.setCaptionAligns(Align.Top);
                                                                        marker.setCaptionColor(Color.BLACK);
                                                                        marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                                                        marker.setCaptionTextSize(14);
                                                                        marker.setHideCollidedSymbols(true);
                                                                        cycleMarkers.add(marker);

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }).start();
                                                }
                                            });

                                            return false;
                                        }
                                    });
                                }
                                LatLng currentPosition = getCurrentPosition(naverMap);
                                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(new LatLng(currentPosition.latitude, currentPosition.longitude), 16).animate(CameraAnimation.Fly, 3000);
                                naverMap.moveCamera(cameraUpdate);
                            }
                        });

                    }
                }).start();
            }
        });

        busButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "500m 내의 버스정류소를 찾습니다", Toast.LENGTH_LONG).show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, count = 0;
                        busstop = getBus();
                        stbusstop = new StringTokenizer(busstop, "*");

                        String bus_citycode[] = new String[stbusstop.countTokens()];
                        String busstop_mapx[] = new String[stbusstop.countTokens()];
                        String busstop_mapy[] = new String[stbusstop.countTokens()];
                        String busstopId[] = new String[stbusstop.countTokens()];
                        String busstopNm[] = new String[stbusstop.countTokens()];

                        while (stbusstop.hasMoreTokens()) {
                            cutting_stbusstop = new StringTokenizer(stbusstop.nextToken(), "@");
                            while (cutting_stbusstop.hasMoreTokens()) {
                                if (count % 5 == 0) {
                                    bus_citycode[num1] = cutting_stbusstop.nextToken();
                                    num1++;
                                    count++;
                                } else if (count % 5 == 1) {
                                    busstop_mapx[num2] = cutting_stbusstop.nextToken();
                                    num2++;
                                    count++;
                                } else if (count % 5 == 2) {
                                    busstop_mapy[num3] = cutting_stbusstop.nextToken();
                                    num3++;
                                    count++;
                                } else if (count % 5 == 3) {
                                    busstopId[num4] = cutting_stbusstop.nextToken();
                                    num4++;
                                    count++;
                                } else if (count % 5 == 4) {
                                    busstopNm[num5] = cutting_stbusstop.nextToken();
                                    num5++;
                                    count++;
                                }
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                freecycleMarkers();
                                freemMarkers();
                                for (int i = 0; i < busstopNm.length; i++) {
                                    Marker marker = new Marker();
                                    marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_directions_bus_24));
                                    marker.setIconTintColor(Color.BLUE);
                                    Log.d("위도경도", busstop_mapx[i] + " " + busstop_mapy[i] + " " + busstopNm[i]);
                                    marker.setPosition(new LatLng(Double.parseDouble(busstop_mapx[i]), Double.parseDouble(busstop_mapy[i])));
                                    marker.setMap(naverMap);
                                    marker.setCaptionText(busstopNm[i]);
                                    marker.setCaptionAligns(Align.Top);
                                    marker.setCaptionColor(Color.BLACK);
                                    marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                    marker.setCaptionTextSize(14);
                                    marker.setHideCollidedSymbols(true);
                                    mMarkers.add(marker);

                                    int finalI = i;

                                        marker.setOnClickListener(new Overlay.OnClickListener() {
                                            @Override
                                            public boolean onClick(@NonNull Overlay overlay) {
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        stationcontent = getStationContent(bus_citycode[finalI], busstopId[finalI]);
                                                        arriveBus = getArriveBus(bus_citycode[finalI], busstopId[finalI]);
                                                        ststation = new StringTokenizer(stationcontent, "*");
                                                        int num = 0;

                                                        String busnumber[] = new String[ststation.countTokens()];
                                                        while(ststation.hasMoreTokens()){
                                                            busnumber[num] = ststation.nextToken();
                                                            Log.d("버스 종류", busnumber[num]);
                                                            num++;
                                                        }

                                                        int num1 = 0, num2 = 0, num3 = 0, count = 0;
                                                        starriebus = new StringTokenizer(arriveBus, "*");

                                                        String arrtime[] = new String[starriebus.countTokens()];
                                                        String routeno[] = new String[starriebus.countTokens()];
                                                        String routeid[] = new String[starriebus.countTokens()];
                                                        while (starriebus.hasMoreTokens()) {
                                                            cutting_starrive = new StringTokenizer(starriebus.nextToken(), "@");
                                                            while (cutting_starrive.hasMoreTokens()) {
                                                                if (count % 3 == 0) {
                                                                    arrtime[num1] = cutting_starrive.nextToken();
                                                                    num1++;
                                                                    count++;
                                                                } else if (count % 3 == 1) {
                                                                    routeno[num2] = cutting_starrive.nextToken();
                                                                    num2++;
                                                                    count++;
                                                                } else if (count % 3 == 2) {
                                                                    routeid[num3] = cutting_starrive.nextToken();
                                                                    num3++;
                                                                    count++;
                                                                }
                                                            }
                                                        }
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Dialog dl = new Dialog(MainActivity.this);
                                                                dl.setContentView(R.layout.bus_layout);
                                                                LinearLayout verticalLayout = dl.findViewById(R.id.busnumber_space);
                                                                for (String bus : busnumber) {
                                                                    TextView textView = new TextView(MainActivity.this);
                                                                    textView.setText(bus);
                                                                    textView.setTextSize(16);
                                                                    textView.setTextColor(Color.BLACK);
                                                                    verticalLayout.addView(textView);

                                                                    View separator = new View(MainActivity.this);
                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                    separator.setLayoutParams(params);
                                                                    verticalLayout.addView(separator);
                                                                }

                                                                LinearLayout verticalLayout2 = dl.findViewById(R.id.arrive_space);

                                                                for (int i = 0; i < arrtime.length - 1; i++) {
                                                                    int minIndex = i;
                                                                    for (int j = i + 1; j < arrtime.length; j++) {
                                                                        int time1 = Integer.parseInt(arrtime[j]);
                                                                        int time2 = Integer.parseInt(arrtime[minIndex]);
                                                                        if (time1 < time2) {
                                                                            minIndex = j;
                                                                        }
                                                                    }

                                                                    // 최소값과 현재 위치의 값을 교환합니다.
                                                                    int tempTime = Integer.parseInt(arrtime[i]);
                                                                    arrtime[i] = arrtime[minIndex];
                                                                    arrtime[minIndex] = Integer.toString(tempTime);

                                                                    String tempNo = routeno[i];
                                                                    routeno[i] = routeno[minIndex];
                                                                    routeno[minIndex] = tempNo;

                                                                    String tempId = routeid[i];
                                                                    routeid[i] = routeid[minIndex];
                                                                    routeid[minIndex] = tempId;
                                                                }

                                                                for (int i = 0; i < arrtime.length; i++) {
                                                                    TextView timeTextView = new TextView(MainActivity.this);
                                                                    int time = Integer.parseInt(arrtime[i]);
                                                                    time = time/60;
                                                                    String times = Integer.toString(time);
                                                                    timeTextView.setText(times+"분");
                                                                    timeTextView.setTextSize(16);
                                                                    timeTextView.setTextColor(Color.BLACK);

                                                                    TextView numberTextView = new TextView(MainActivity.this);
                                                                    numberTextView.setText(routeno[i]);
                                                                    numberTextView.setTextSize(16);
                                                                    numberTextView.setTextColor(Color.BLACK);

                                                                    TextView stTextView = new TextView(MainActivity.this);
                                                                    stTextView.setText(routeid[i]);
                                                                    stTextView.setTextSize(16);
                                                                    stTextView.setTextColor(Color.BLACK);

                                                                    LinearLayout horizontalLayout = new LinearLayout(MainActivity.this);
                                                                    horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                                    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

                                                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                                                    timeTextView.setLayoutParams(layoutParams);
                                                                    numberTextView.setLayoutParams(layoutParams);
                                                                    stTextView.setLayoutParams(layoutParams);

                                                                    horizontalLayout.addView(timeTextView);
                                                                    horizontalLayout.addView(numberTextView);
                                                                    horizontalLayout.addView(stTextView);
                                                                    verticalLayout2.addView(horizontalLayout);

                                                                    View separator = new View(MainActivity.this);
                                                                    separator.setBackgroundColor(Color.GRAY);
                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                                                    separator.setLayoutParams(params);
                                                                    verticalLayout2.addView(separator);
                                                                }
                                                                dl.show();
                                                            }
                                                        });
                                                    }
                                                }).start();

                                                return false;
                                            }
                                        });
                                }
                                LatLng currentPosition = getCurrentPosition(naverMap);
                                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(new LatLng(currentPosition.latitude, currentPosition.longitude), 16).animate(CameraAnimation.Fly, 3000);
                                naverMap.moveCamera(cameraUpdate);
                            }
                        });
                    }
                }).start();

            }
        });

        cycleFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "1km 내의 누비자 정류소를 찾습니다", Toast.LENGTH_LONG).show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int num1 = 0, num2 = 0, num3 = 0, count = 0;
                        nubiza= getNubiza();
                        stnubiza = new StringTokenizer(nubiza, "*");

                        String nubiza_station[] = new String[stnubiza.countTokens()];
                        String nubiza_mapx[] = new String[stnubiza.countTokens()];
                        String nubiza_mapy[] = new String[stnubiza.countTokens()];


                        while (stnubiza.hasMoreTokens()) {
                            cutting_nubiza = new StringTokenizer(stnubiza.nextToken(), "@");
                            while (cutting_nubiza.hasMoreTokens()) {
                                if (count % 3 == 0) {
                                    nubiza_station[num1] = cutting_nubiza.nextToken();
                                    num1++;
                                    count++;
                                } else if (count % 3 == 1) {
                                    nubiza_mapx[num2] = cutting_nubiza.nextToken();
                                    num2++;
                                    count++;
                                } else if (count % 3 == 2) {
                                    nubiza_mapy[num3] = cutting_nubiza.nextToken();
                                    num3++;
                                    count++;
                                }
                            }
                        }
                        Log.d("위도경도", nubiza_station[0] + " " + nubiza_mapx[0] + " " + nubiza_mapy[0]);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                freebusMarkers();
                                freemMarkers();

                                markersPosition = new Vector<LatLng>();
                                for (int i = 0; i < nubiza_station.length; i++) {
                                    markersPosition.add(new LatLng(Double.parseDouble(nubiza_mapx[i]), Double.parseDouble(nubiza_mapy[i])));

                                }

                                LatLng currentPosition = getCurrentPosition(naverMap);
                                for (int i = 0; i < markersPosition.size(); i++) {
                                    LatLng markerPosition = markersPosition.get(i);
                                    if (!withinSightMarker(currentPosition, markerPosition))
                                        continue;
                                    Marker marker = new Marker();
                                    marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_directions_bike_24));
                                    marker.setIconTintColor(Color.GREEN);
                                    marker.setPosition(markerPosition);
                                    marker.setMap(naverMap);
                                    marker.setCaptionText(nubiza_station[i]);
                                    marker.setCaptionAligns(Align.Top);
                                    marker.setCaptionColor(Color.BLACK);
                                    marker.setCaptionHaloColor(Color.rgb(255, 255, 255));
                                    marker.setCaptionTextSize(14);
                                    marker.setHideCollidedSymbols(true);
                                    mMarkers.add(marker);

                                    /*marker.setOnClickListener(new Overlay.OnClickListener() {
                                        @Override
                                        public boolean onClick(@NonNull Overlay overlay) {
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {


                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                        }
                                                    });
                                                }
                                            }).start();

                                            return false;
                                        }
                                    });*/
                                }
                                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(new LatLng(currentPosition.latitude, currentPosition.longitude), 16).animate(CameraAnimation.Fly, 3000);
                                naverMap.moveCamera(cameraUpdate);
                            }
                        });
                    }
                }).start();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // 왼쪽 상단 버튼 눌렀을 때
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() { //뒤로가기 했을 때
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /* String setSearchResultData(String s) {
        StringBuffer buffer = new StringBuffer();
        InputStream inputStream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new InputStreamReader(inputStream, "UTF-8"));

            String tag;

            xmlPullParser.next();
            int eventType = xmlPullParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        tag = xmlPullParser.getName();
                        if (tag.equals("item")) ;

                        else if (tag.equals(("title"))) {
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if (tag.equals("address")) {
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if (tag.equals("mapx")) {
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        }else if (tag.equals("mapy")) {
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xmlPullParser.getName();
                        if (tag.equals("item")) buffer.append("*");
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    } */

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


    String ImageparseXML(String xmlString) {
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
                    String link = element.getElementsByTagName("link").item(0).getTextContent();
                    buffer.append(link);
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

    void getImageSearchResult(String input, SearchCallback callback) {
        ImageApiInterface imageApiInterface = ImageApiClient.getInstance().create(ImageApiInterface.class);
        Call<String> call = imageApiInterface.getImageSearchResult(clientId, clientSecret,"image.xml", input,1, 1, "date");
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


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) {
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
                return;
            } else {
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        LatLng initialPosition = new LatLng(35.2280, 128.6818);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
        naverMap.moveCamera(cameraUpdate);//카메라 초기 위치 설정

        naverMap.setLocationSource(locationSource); // 위치추적 기능
        ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);


        UiSettings uiSettings = naverMap.getUiSettings(); // 현재 위치 버튼
        uiSettings.setLocationButtonEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setZoomControlEnabled(false);
        uiSettings.setScaleBarEnabled(false);

        LocationButtonView locationButtonView = findViewById(R.id.location_button);
        locationButtonView.setMap(naverMap);


        CameraUpdate cameraUpdate1 = CameraUpdate.scrollAndZoomTo(initialPosition, 18).animate(CameraAnimation.Fly, 3000);
        naverMap.moveCamera(cameraUpdate1);

        naverMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                CameraUpdate cameraUpdate2 = CameraUpdate.scrollTo(new LatLng(latLng.latitude, latLng.longitude)).animate(CameraAnimation.Fly);
                naverMap.moveCamera(cameraUpdate2);

            }

        });  // 지도상 클릭한 위치로 카메라 이동하는 클릭리스너
    }

    public LatLng getCurrentPosition(NaverMap naverMap) {
        CameraPosition cameraPosition = naverMap.getCameraPosition();
        return new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }


    private Vector<LatLng> markersPosition;
    private Vector<Marker> mMarkers;
    private Vector<Marker> busMarkers;
    private Vector<Marker> cycleMarkers;
    List<LatLng> duplicatedMarkerPositions = new ArrayList<>();
    List<String> duplicatedMarkerNames = new ArrayList<>();
    List<Marker> markers = new ArrayList<>();

    private void freemMarkers() {
        if (mMarkers == null) {
            mMarkers = new Vector<Marker>();
            return;
        }
        for (Marker mMarker : mMarkers) {
            mMarker.setMap(null);
        }
        mMarkers = new Vector<Marker>();
    } //지도상에 표시되는 마커들을 지도에서 삭제하는 함수

    private void freebusMarkers() {
        if (busMarkers == null) {
            busMarkers = new Vector<Marker>();
            return;
        }
        for (Marker busMarker : busMarkers) {
            busMarker.setMap(null);
        }
        busMarkers = new Vector<Marker>();
    } //버스 검색 결과의 마커들을 지도에서 삭제하는 함수

    private void freecycleMarkers() {
        if (cycleMarkers == null) {
            cycleMarkers = new Vector<Marker>();
            return;
        }
        for (Marker cycleMarker : cycleMarkers) {
            cycleMarker.setMap(null);
        }
        cycleMarkers = new Vector<Marker>();
    } //누비자 검색 결과의 마커들을 지도에서 삭제하는 함수

    public final static double REFERLAT = 1.0 / 111.0;
    public final static double REFERLNG = 1.0 / 88.74;

    // 검색할 반경 지정
    public boolean withinSightMarker(LatLng currentPosition, LatLng markerPosition) {
        boolean withinSiMaLat = Math.abs(currentPosition.latitude - markerPosition.latitude) <= REFERLAT;
        boolean withinSiMaLng = Math.abs(currentPosition.longitude - markerPosition.longitude) <= REFERLNG;
        return withinSiMaLat && withinSiMaLng;
    }


    private void verifyMarker(Marker marker){
        for(Marker onMapMarker : mMarkers){
            LatLng onMapMarkerPosition = onMapMarker.getPosition();
            LatLng markerPosition = marker.getPosition();
            if(onMapMarkerPosition.latitude == markerPosition.latitude && onMapMarkerPosition.longitude == markerPosition.longitude){
                marker.setMap(null);
                markers.add(marker);
                return;
            } else {
                mMarkers.add(marker);
            }
        }
    }

    String getFood() {    // 음식 버튼 클릭할 때 얻어올 반경에 따른 검색 정보
        StringBuffer buffer = new StringBuffer();
        LatLng currentPosition = getCurrentPosition(naverMap);
        String lat = URLEncoder.encode(Double.toString(currentPosition.latitude));
        String lon = URLEncoder.encode(Double.toString(currentPosition.longitude));

        for(int i = 1; i < 8; i++){
            String queryUrl = "https://apis.data.go.kr/B553077/api/open/sdsc2/storeListInRadius?serviceKey=JxPb3zcF6wDWwkLFct1EFT5xiiKWL5mS3vdQM%2F8kQXCqv%2BSW2ntspIlw1fad%2B5f%2FC8SRGRr2wqw68Vd6zWG3AQ%3D%3D&pageNo=1&numOfRows=20&radius=200&cx=" + lon + "&cy=" + lat + "&indsLclsCd=I2&indsMclsCd=I20"+String.valueOf(i)+"&type=xml";
            if(i==7) queryUrl = "https://apis.data.go.kr/B553077/api/open/sdsc2/storeListInRadius?serviceKey=JxPb3zcF6wDWwkLFct1EFT5xiiKWL5mS3vdQM%2F8kQXCqv%2BSW2ntspIlw1fad%2B5f%2FC8SRGRr2wqw68Vd6zWG3AQ%3D%3D&pageNo=1&numOfRows=20&radius=200&cx=" + lon + "&cy=" + lat + "&indsLclsCd=I2&indsMclsCd=I210&type=xml";
            try {
                URL url = new URL(queryUrl);
                InputStream inputStream = url.openStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xmlPullParser = factory.newPullParser();
                xmlPullParser.setInput(new InputStreamReader(inputStream, "UTF-8"));

                String tag;

                xmlPullParser.next();
                int eventType = xmlPullParser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            tag = xmlPullParser.getName();
                            if (tag.equals("item")) ;
                            else if (tag.equals(("bizesNm"))) {
                                xmlPullParser.next();
                                buffer.append(xmlPullParser.getText());
                                buffer.append("@");
                            } else if (tag.equals("lnoAdr")) {
                                xmlPullParser.next();
                                buffer.append(xmlPullParser.getText());
                                buffer.append("@");
                            } else if (tag.equals("lon")) {
                                xmlPullParser.next();
                                buffer.append(xmlPullParser.getText());
                                buffer.append("@");
                            }else if (tag.equals("lat")) {
                                xmlPullParser.next();
                                buffer.append(xmlPullParser.getText());
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            tag = xmlPullParser.getName();
                            if (tag.equals("item")) buffer.append("*");
                            break;
                    }
                    eventType = xmlPullParser.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return buffer.toString();
    }

    String getCafe(){   // 카페 선택
        StringBuffer buffer = new StringBuffer();
        LatLng currentPosition = getCurrentPosition(naverMap);
        String lat = URLEncoder.encode(Double.toString(currentPosition.latitude));
        String lon = URLEncoder.encode(Double.toString(currentPosition.longitude));

        String queryUrl = "https://apis.data.go.kr/B553077/api/open/sdsc2/storeListInRadius?serviceKey=JxPb3zcF6wDWwkLFct1EFT5xiiKWL5mS3vdQM%2F8kQXCqv%2BSW2ntspIlw1fad%2B5f%2FC8SRGRr2wqw68Vd6zWG3AQ%3D%3D&pageNo=1&numOfRows=50&radius=200&cx="+lon+"&cy="+lat+"&indsLclsCd=I2&indsMclsCd=I212&type=xml";
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

                        else if(tag.equals(("bizesNm"))){
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if (tag.equals("lnoAdr")){
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

    String getBus(){   // 버스정류장 선택
        StringBuffer buffer = new StringBuffer();
        LatLng currentPosition = getCurrentPosition(naverMap);
        String lat = URLEncoder.encode(Double.toString(currentPosition.latitude));
        String lon = URLEncoder.encode(Double.toString(currentPosition.longitude));

        String queryUrl = "https://apis.data.go.kr/1613000/BusSttnInfoInqireService/getCrdntPrxmtSttnList?serviceKey=JxPb3zcF6wDWwkLFct1EFT5xiiKWL5mS3vdQM%2F8kQXCqv%2BSW2ntspIlw1fad%2B5f%2FC8SRGRr2wqw68Vd6zWG3AQ%3D%3D&pageNo=1&numOfRows=50&_type=xml&gpsLati="+ lat + "&gpsLong=" + lon;
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

                        else if(tag.equals(("citycode"))){
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        }
                        else if(tag.equals(("gpslati"))){
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if (tag.equals("gpslong")) {
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if(tag.equals("nodeid")){
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if(tag.equals("nodenm")){
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
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

    String getArriveBus(String cityNumber, String routeId){   // 곧 도착할 버스 목록
        StringBuffer buffer = new StringBuffer();
        String queryUrl = "https://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList?serviceKey=JxPb3zcF6wDWwkLFct1EFT5xiiKWL5mS3vdQM%2F8kQXCqv%2BSW2ntspIlw1fad%2B5f%2FC8SRGRr2wqw68Vd6zWG3AQ%3D%3D&pageNo=1&numOfRows=20&_type=xml&cityCode=" + cityNumber + "&nodeId=" + routeId;
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

                        else if(tag.equals(("arrtime"))){
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        }
                        else if(tag.equals(("routeno"))){
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
                        } else if (tag.equals("routetp")) {
                            xmlPullParser.next();
                            buffer.append(xmlPullParser.getText());
                            buffer.append("@");
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

    String getStationContent(String citycode, String busstopId){   // 버스정류장 선택
        StringBuffer buffer = new StringBuffer();
        String queryUrl = "https://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnThrghRouteList?serviceKey=JxPb3zcF6wDWwkLFct1EFT5xiiKWL5mS3vdQM%2F8kQXCqv%2BSW2ntspIlw1fad%2B5f%2FC8SRGRr2wqw68Vd6zWG3AQ%3D%3D&pageNo=1&numOfRows=20&_type=xml&cityCode="+ citycode +"&nodeid=" + busstopId;
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

                        else if(tag.equals(("routeno"))){
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

    String getNubiza(){
        StringBuffer buffer = new StringBuffer();
        String queryUrl = "https://api.odcloud.kr/api/15000545/v1/uddi:lgt2dy2p-wwh7-jrxr-o85n-fxxskri7gpjs_201912181628?page=1&perPage=283&returnType=XML&serviceKey=JxPb3zcF6wDWwkLFct1EFT5xiiKWL5mS3vdQM%2F8kQXCqv%2BSW2ntspIlw1fad%2B5f%2FC8SRGRr2wqw68Vd6zWG3AQ%3D%3D";

        try {
            URL url = new URL(queryUrl);
            InputStream inputStream = url.openStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new InputStreamReader(inputStream, "UTF-8"));

            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xmlPullParser.getName();
                if (eventType == XmlPullParser.START_TAG && tagName.equals("item")) {
                    String terminalName = "";
                    String latitude = "";
                    String longitude = "";

                    while (eventType != XmlPullParser.END_TAG || !tagName.equals("item")) {
                        if (eventType == XmlPullParser.START_TAG) {
                            String attributeName = xmlPullParser.getAttributeValue(null, "name");
                            if (attributeName != null) {
                                if (attributeName.equals("터미널명")) {
                                    xmlPullParser.next();
                                    terminalName = xmlPullParser.getText();
                                } else if (attributeName.equals("위도")) {
                                    xmlPullParser.next();
                                    latitude = xmlPullParser.getText();
                                } else if (attributeName.equals("경도")) {
                                    xmlPullParser.next();
                                    longitude = xmlPullParser.getText();
                                }
                            }
                        }

                        eventType = xmlPullParser.next();
                        tagName = xmlPullParser.getName();
                    }

                    buffer.append(terminalName);
                    buffer.append("@");
                    buffer.append(latitude);
                    buffer.append("@");
                    buffer.append(longitude);
                    buffer.append("*");
                }

                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }

    //이미지 뷰에 사진 url을 연결하기 위한 메소드
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }
}
