package com.example.this_is_changwon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class directionApiClient {

    private static final String BASE_URL = "https://naveropenapi.apigw.ntruss.com/map-direction-15/";
    private static Retrofit retrofit;

    public static directionInterface getDirectionInterface() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(directionInterface.class);
    }
}
