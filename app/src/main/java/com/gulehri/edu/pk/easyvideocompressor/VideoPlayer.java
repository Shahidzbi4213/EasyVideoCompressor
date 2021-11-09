package com.gulehri.edu.pk.easyvideocompressor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.gulehri.edu.pk.easyvideocompressor.databinding.ActivityVideoPlayerBinding;

public class VideoPlayer extends AppCompatActivity {

    private ActivityVideoPlayerBinding binding;
    private SimpleExoPlayer player;
    private Uri uri;
    private boolean fullScreen = false;
    private ImageView fullScreenView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityVideoPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        getValue();
        initializePlayer(uri);
    }

    private void getValue() {
        Intent intent = getIntent();
        uri = (Uri) intent.getExtras().get("uri");
    }

    private void initializePlayer(Uri uri) {
        player = new SimpleExoPlayer.Builder(VideoPlayer.this).build();
        binding.playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        fullScreenView = binding.playerView.findViewById(R.id.exo_fullscreen_icon);


        fullScreenView.setOnClickListener(v -> {
            if (!fullScreen) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                fullScreenView.setImageDrawable(ContextCompat.
                        getDrawable(VideoPlayer.this, R.drawable.exo_ic_fullscreen_exit));
                fullScreen = true;
            } else {
                fullScreenView.setImageDrawable(ContextCompat.
                        getDrawable(VideoPlayer.this, R.drawable.exo_ic_fullscreen_enter));
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }

        });

    }


    @Override
    protected void onPause() {
        super.onPause();
        player.stop();
        player.clearMediaItems();
    }
}