package com.example.filterifydemo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.filterifydemo.databinding.ActivityMainBinding;
import com.lirandaniel.filterify.Filterify;

import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_PICK = 102;
    private static final int REQUEST_CAMERA_PERMISSION = 103;
    private static final int REQUEST_GALLERY_PERMISSION = 104;

    private Uri cam_uri;
    private ActivityMainBinding binding;

    private Filterify filterify;
    private FiltersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        filterify = new Filterify();
        setSpinnerValues();
        binding.filterButton.setOnClickListener(v->this.filter());
        binding.pictureButton.setOnClickListener(v->showImageSourceDialog());
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        openCameraNew();
                        break;
                    case 1:
                        openGallery();
                        break;
                }
            }
        });
        builder.show();
    }

    private void filter() {
        List<String> selectedFilters = getSelectedFilters();
        if(selectedFilters.size()==0){
            Toast.makeText(this,"Please choose at least one filter",Toast.LENGTH_SHORT).show();
            return;
        }
        filterify.sendImageToServer(binding.original, selectedFilters, new Filterify.ImageResponseCallback() {
            @Override
            public void onSuccess(Bitmap filteredImage) {
                binding.filtered.setImageBitmap(filteredImage);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActiviy", Objects.requireNonNull(throwable.getMessage()));
            }
        });
    }

    private List<String> getSelectedFilters() {
        return FiltersAdapter.selected;
    }


    private void setUpRecyclerView(List<String> stringList) {

        adapter = new FiltersAdapter(stringList);
        binding.filterRV.setAdapter(adapter);
        binding.filterRV.setLayoutManager(new LinearLayoutManager(this));
    }
    private void setSpinnerValues() {

        filterify.getFilters(new Filterify.FilterCallback() {
            @Override
            public void onSuccess(List<String> filters) {
                setUpRecyclerView(filters);

            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActiviy", Objects.requireNonNull(throwable.getMessage()));
            }
        });

    }

    private Bitmap getBitmapFromImageRes(int res) {
        return BitmapFactory.decodeResource(getResources(), res);
    }

    private void openCamera() {
        // Check if camera permission is granted
        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.CAMERA)) {
            openCameraNew();
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//            }
        } else {
            // Request camera permission if not granted
            EasyPermissions.requestPermissions(this, "Camera permission is required",
                    REQUEST_CAMERA_PERMISSION, android.Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward permission results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    // Handle result of camera or gallery activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                binding.original.setImageBitmap(imageBitmap);
                binding.filtered.setImageBitmap(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImageUri = data.getData();
                binding.original.setImageURI(selectedImageUri);
                binding.filtered.setImageURI(selectedImageUri);
            }
        }
    }

    ActivityResultLauncher<Intent> startCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    binding.original.setImageURI(cam_uri);
                    binding.filtered.setImageURI(cam_uri);
                }
            }
    );

    private void openCameraNew() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cam_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cam_uri);

        //startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE); // OLD WAY
        startCamera.launch(cameraIntent); // VERY NEW WAY
    }



    // Method to open the gallery
    private void openGallery() {
        // Check if gallery permission is granted
        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
        } else {
            // Request gallery permission if not granted
            EasyPermissions.requestPermissions(this, "Gallery permission is required",
                    REQUEST_GALLERY_PERMISSION, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }
}