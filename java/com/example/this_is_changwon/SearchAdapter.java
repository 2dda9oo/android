package com.example.this_is_changwon;

import static android.service.controls.ControlsProviderService.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.AdapterViewholder> {

    private List<SearchData> dataList;
    private FragmentActivity context;

    private String clientId = "5MB5dzIWO9Iw2DT3rWKs";
    private String clientSecret = "4lzPNQjrlu";
    private Appdatabase db;

    String ImageUrl;
    ImageView imageview;

    public SearchAdapter(FragmentActivity context, List<SearchData> dataList){
        this.context = context;
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override  ///onCreateViewHolder에서는 어떤 레이아웃과 연결해야되는지 설정하고 view를 만들어준다.
    public AdapterViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_layout,parent,false);
        AdapterViewholder holder = new AdapterViewholder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewholder holder, int position){
        db = Appdatabase.getInstance(context);
        holder.result_name.setText(dataList.get(position).getName());
        holder.result_sclsNm.setText(dataList.get(position).getSclsCd());
        holder.result_ksicNm.setText(dataList.get(position).getKsicNm());
        holder.result_addr.setText(dataList.get(position).getAddr());

        getImageSearchResult("창원" + dataList.get(position).getName(), new SearchCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d("성공?: ", result);
                ImageUrl = ImageparseXML(result);
                Log.d("link : ", ImageUrl);
                if(ImageUrl.equals("")){
                    holder.imageView.setImageResource(R.drawable.nonplace);
                } else {
                    String imageUrl = ImageUrl;
                    new LoadImageTask(holder.imageView).execute(imageUrl);
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchData sdata = dataList.get(holder.getAdapterPosition());
                String click_name = sdata.getName();
                String click_lat = sdata.getLat();
                String click_lon = sdata.getLon();
                String click_addr = sdata.getAddr();

                Intent intent = new Intent(context, SearchResultActivity.class);
                intent.putExtra("name", click_name);
                intent.putExtra("lat", click_lat);
                intent.putExtra("lon", click_lon);
                intent.putExtra("addr", click_addr);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class AdapterViewholder extends RecyclerView.ViewHolder {

        TextView result_name, result_addr, result_sclsNm, result_ksicNm;
        ImageView imageView;

        public AdapterViewholder(@NonNull View itemView){
            super(itemView);

            result_name = itemView.findViewById(R.id.result_name);
            result_addr = itemView.findViewById(R.id.result_addr);
            result_sclsNm = itemView.findViewById(R.id.result_sclsMn);
            result_ksicNm = itemView.findViewById(R.id.result_ksicNm);
            imageView = itemView.findViewById(R.id.search_image);

        }
    }
    public String data1 = "ok";

    String getResult(String s) {
        data1 = s;
        Log.e(TAG, "지역변수 저장 성공? : " + data1);

        return data1;
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

    //이미지 뷰에 사진 url을 연결하기 위한 메소드
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;

        public LoadImageTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
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
            ImageView imageView = imageViewReference.get();
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }
}
