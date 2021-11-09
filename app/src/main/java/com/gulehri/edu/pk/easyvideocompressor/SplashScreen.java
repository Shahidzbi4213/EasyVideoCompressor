package com.gulehri.edu.pk.easyvideocompressor;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
import static androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.gulehri.edu.pk.easyvideocompressor.databinding.ActivitySplashScreenBinding;

public class SplashScreen extends AppCompatActivity {

    private static final int DELAY_MILLIS = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActivitySplashScreenBinding binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DataSaver saver = new DataSaver(SplashScreen.this);
        final boolean flag = saver.getMode();
        saver.saveUri(Uri.parse(""));

        if (flag) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
            } else {
                setDefaultNightMode(MODE_NIGHT_YES);
            }
        } else {
            setDefaultNightMode(MODE_NIGHT_NO);
        }
        new Handler().postDelayed(() -> {
                    SplashScreen.this.startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    finish();
                },
                DELAY_MILLIS);

    }
}