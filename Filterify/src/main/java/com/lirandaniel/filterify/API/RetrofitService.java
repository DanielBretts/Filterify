package com.lirandaniel.filterify.API;


import com.lirandaniel.filterify.model.ImageData;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface RetrofitService {
    @GET("/filters")
    Call<List<String>> listFilters();

    @Headers("Content-Type: application/json")
    @POST("/apply_filters")
    Call<ImageData> applyFilters(@Body ImageData bytes, @Query("filters") List<String> filters);
}