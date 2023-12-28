package com.example.this_is_changwon;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApiClient{
    private static final String BASE_URL = "https://openapi.naver.com/v1/";
    private static Retrofit retrofit;

    public static Retrofit getInstance()
    {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        if (retrofit == null)
        {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
