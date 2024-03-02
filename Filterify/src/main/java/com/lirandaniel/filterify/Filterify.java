package com.lirandaniel.filterify;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.lirandaniel.filterify.API.RetrofitService;
import com.lirandaniel.filterify.model.ImageData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Filterify {

    final String BASE_URL = "http://seminartest.pythonanywhere.com";
    // Method to send the image to the server
    public void sendImageToServer(ImageView imageViewUser, List<String> filters,ImageResponseCallback callback) {
        Bitmap bitmap = ((BitmapDrawable) imageViewUser.getDrawable()).getBitmap();
        if (bitmap == null) {
            Log.e("OnFailure", "Bitmap is null");
            return;
        }
        // Convert Bitmap to byte array
        String baseString = convertBitmapToBase64String(bitmap);

        // Make API call to send image to server
        RetrofitService retrofitService = createRetrofitConnection();


        Call<ImageData> call = retrofitService.applyFilters(new ImageData(baseString), filters);
        call.enqueue(new Callback<ImageData>() {
            @Override
            public void onResponse(@NonNull Call<ImageData> call, @NonNull Response<ImageData> response) {
                if (response.isSuccessful()) {
                    // Handle success response
                    Log.d("Filterify", "Image filtered successfully");                    assert response.body() != null;
                    byte[] decodedString = Base64.decode(response.body().getByteArray(), Base64.DEFAULT);
                    Bitmap bitmapResponse = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    callback.onSuccess(bitmapResponse);
                } else {
                    callback.onError(new Exception(response.message()));
                    Log.e("Filterify", "Failed to filter image: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ImageData> call, @NonNull Throwable t) {
                // Handle failure
                Log.e("Filterify", "Failed to send image", t);
            }
        });

    }

    private String convertBitmapToBase64String(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public void getFilters(final FilterCallback callback) {
        RetrofitService retrofitService = createRetrofitConnection();
        Call<List<String>> call = retrofitService.listFilters();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    List<String> filters = response.body();
                    Log.d("Filterify", "Filters Available: " + filters.toString());
                    callback.onSuccess(filters);
                } else {
                    Log.e("Filterify", "Failed to fetch filters: " + response.message());
                    callback.onError(new Exception("Failed to fetch filters"));
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e("Filterify", "Failed to fetch filters: " + t.getMessage());
                callback.onError(t);
            }
        });
    }

    public interface FilterCallback {
        void onSuccess(List<String> filters);
        void onError(Throwable throwable);
    }

    public interface ImageResponseCallback {
        void onSuccess(Bitmap filteredImage);
        void onError(Throwable throwable);
    }

    @NonNull
    private RetrofitService createRetrofitConnection() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(RetrofitService.class);
    }
}