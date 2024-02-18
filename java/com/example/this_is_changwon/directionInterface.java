package com.example.this_is_changwon;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface directionInterface {
    @GET("v1/driving")
    Call<ResponseBody> getPath(
            @Header("X-NCP-APIGW-API-KEY-ID") String id,
            @Header("X-NCP-APIGW-API-KEY") String pw,
            @Query("start") String start,
            @Query("goal") String goal,
            @Query("option") String option

    );
}
