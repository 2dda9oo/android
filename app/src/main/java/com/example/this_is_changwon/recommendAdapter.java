package com.example.this_is_changwon;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class recommendAdapter extends RecyclerView.Adapter<recommendAdapter.AdapterViewholder> {

    private List<recommendedSpot> recommendList;
    private FragmentActivity context;

    private Appdatabase db;


    public recommendAdapter(FragmentActivity context, List<recommendedSpot> recommendList){
        this.context = context;
        this.recommendList = recommendList;
    }

    @NonNull
    @Override
    public AdapterViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_item, parent, false);
        AdapterViewholder holder = new AdapterViewholder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewholder holder, int position){
        holder.recommended_name.setText(Integer.toString(position+1) + ". " + recommendList.get(position).getName());
        holder.recommended_addr.setText(recommendList.get(position).getAddress());
        holder.numberCounting.setText(Integer.toString(position+1));

        String imageUrl = recommendList.get(position).getImage();
        LoadImageTask loadImageTask = new LoadImageTask(holder.recommended_image);
        loadImageTask.execute(imageUrl);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recommendedSpot recommendedspot = recommendList.get(holder.getAdapterPosition());
                String click_name = recommendedspot.getName();
                String click_type = recommendedspot.getType();

                String url = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%B0%BD%EC%9B%90%20" + click_name;

                Dialog dl = new Dialog(context);
                dl.setContentView(R.layout.recommendeddialog);

                // UI 스레드의 핸들러를 얻습니다.
                Handler handler = new Handler(Looper.getMainLooper());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            org.jsoup.nodes.Document jsoupDoc = Jsoup.connect(url).get();

                            Elements element1 = jsoupDoc.select(".Fc1rA"); // 장소 이름이 포함된 엘리먼트 선택
                            String name = element1.text();

                            Elements element2 = jsoupDoc.select(".DJJvD"); // 장소 분류가 포함된 엘리먼트 선택
                            String classify = element2.text();

                            Elements element3 = jsoupDoc.select(".zPfVt"); // 장소 정보가 포함된 엘리먼트 선택
                            String info = element3.text();

                            Elements element4 = jsoupDoc.select(".y6tNq"); // 장소 설명이 포함된 엘리먼트 선택
                            String detail = element4.text();

                            // UI 스레드에서 다이얼로그 업데이트를 수행합니다.
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    TextView nameTextView = dl.findViewById(R.id.crolling_name);
                                    TextView typeTextView = dl.findViewById(R.id.crolling_type);
                                    TextView classifyTextView = dl.findViewById(R.id.crolling_classify);
                                    TextView infoTextView = dl.findViewById(R.id.crolling_info);
                                    TextView detailTextView = dl.findViewById(R.id.crolling_detail);

                                    // 다이얼로그의 각 TextView에 정보 설정
                                    nameTextView.setText(name);
                                    classifyTextView.setText(classify);
                                    infoTextView.setText(info);
                                    detailTextView.setText(detail);
                                    typeTextView.setText(click_type);

                                    // 다이얼로그 표시
                                    dl.show();
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });
    }

    @Override
    public  int getItemCount(){return recommendList.size();}


    public class AdapterViewholder extends RecyclerView.ViewHolder {

        TextView recommended_name, recommended_addr, numberCounting;
        ImageView recommended_image;

        public AdapterViewholder(@NonNull View itemView){
            super(itemView);

            recommended_name = itemView.findViewById(R.id.recommended_name);
            recommended_addr = itemView.findViewById(R.id.recommended_addr);
            recommended_image = itemView.findViewById(R.id.recommend_image);
            numberCounting = itemView.findViewById(R.id.number_count);
        }

    }

    //이미지 뷰에 사진 url을 연결하기 위한 메소드
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

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

