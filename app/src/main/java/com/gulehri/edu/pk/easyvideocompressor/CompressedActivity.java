package com.gulehri.edu.pk.easyvideocompressor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.gulehri.edu.pk.easyvideocompressor.databinding.ActivityCompressedBinding;
import com.unity3d.ads.UnityAds;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Objects;

public class CompressedActivity extends AppCompatActivity {

    private ActivityCompressedBinding binding;
    private String originalSize;
    private File path;
    private Uri uri;
    private DataSaver saver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompressedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.tBar.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        saver = new DataSaver(CompressedActivity.this);


        getValues();
        getSize(path);
        setValue();



        binding.thumbnail.setOnClickListener(view -> {
            Intent intent = new Intent(CompressedActivity.this, VideoPlayer.class);
            intent.putExtra("uri", Uri.fromFile(path));
            startActivity(intent);
        });

        binding.btnSFile.setOnClickListener(v1 -> {
            startActivity(new Intent(CompressedActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            saver.saveUri(Uri.parse(""));
            finish();
        });

    }




    @SuppressLint("SetTextI18n")
    private void setValue() {
        Glide.with(CompressedActivity.this).load(uri).into(binding.thumbnail);
        binding.tvOS.setText(originalSize.replaceAll("Size: ", "Original Video Size: "));
        binding.tvLocation.setText("Location: Internal Storage/EVC");


    }

    private void getValues() {
        Intent intent = getIntent();
        originalSize = intent.getStringExtra("originalSize");
        path = (File) intent.getExtras().get("path");
        uri = (Uri) intent.getExtras().get("videoThumbnail");
        if (uri == null) {
            uri = saver.getUri();
        }

    }


    @SuppressLint("SetTextI18n")
    private void getSize(File path) {

        try {
            AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(Uri.fromFile(path), "r");
            final long fileSize = fileDescriptor.getLength();

            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(fileSize) / Math.log10(1024));
            final String size = new DecimalFormat("#,##0.#").format(fileSize / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
            binding.tvCS.setText("Compressed Video Size: " + size);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CompressedActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        saver.saveUri(Uri.parse(""));
        finish();

    }
}