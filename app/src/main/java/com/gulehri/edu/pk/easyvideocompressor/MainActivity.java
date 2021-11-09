package com.gulehri.edu.pk.easyvideocompressor;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
import static androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode;
import static com.gulehri.edu.pk.easyvideocompressor.R.id.darkOFF;
import static com.gulehri.edu.pk.easyvideocompressor.R.id.darkOn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
import com.abedelazizshe.lightcompressorlibrary.VideoQuality;
import com.abedelazizshe.lightcompressorlibrary.config.Configuration;
import com.bumptech.glide.Glide;
import com.gulehri.edu.pk.easyvideocompressor.databinding.ActivityMainBinding;
import com.gulehri.edu.pk.easyvideocompressor.databinding.QualityDialogBinding;
import com.unity3d.ads.UnityAds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> launcher;
    private Uri videoUri;
    private String quality;
    private final String unitID = "interstitial";
    private final String gameId ="12345";
    private DataSaver saver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        setSupportActionBar(binding.mainBar.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);


        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {

                videoUri = Objects.requireNonNull(result.getData()).getData();

                saver = new DataSaver(MainActivity.this);
                saver.saveUri(videoUri);

                binding.btnSelectFile.setVisibility(GONE);
                binding.mainContents.setVisibility(VISIBLE);
                binding.btnStartCompress.setVisibility(VISIBLE);
                binding.imgThumbnails.setVisibility(VISIBLE);
                binding.btnPlay.setVisibility(VISIBLE);
                binding.btnCancelVideo.setVisibility(VISIBLE);
                binding.tvOrignalSize.setVisibility(VISIBLE);


                setThumbnails(videoUri);
                originalFileSize(videoUri);


            }
        });

        binding.btnSelectFile.setOnClickListener(view -> {
            binding.imgThumbnails.setClickable(true);
            askPerms();
            chooseVideo();
        });

        binding.btnCancelVideo.setOnClickListener(v1 -> {

            saver.saveUri(Uri.parse(""));
            binding.imgThumbnails.setVisibility(GONE);
            binding.btnPlay.setVisibility(GONE);
            binding.tvOrignalSize.setVisibility(GONE);
            binding.btnStartCompress.setVisibility(GONE);
            binding.btnCancelVideo.setVisibility(GONE);
            binding.btnCancel.setVisibility(GONE);
            binding.btnSelectFile.setVisibility(VISIBLE);

        });

        binding.btnStartCompress.setOnClickListener(v2 -> {

            QualityDialogBinding dialogBinding = QualityDialogBinding.inflate(LayoutInflater.from(this));
            AlertDialog builder = new AlertDialog.Builder(this).setView(dialogBinding.getRoot())
                    .create();
            builder.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


            dialogBinding.rbGroup.setOnCheckedChangeListener((radioGroup, i) -> {
                RadioButton button = builder.findViewById(i);
                button.setChecked(true);
                button.setTextColor(getResources().getColor(R.color.black));
                quality = button.getText().toString();
                binding.btnStartCompress.setVisibility(GONE);
                binding.btnCancel.setVisibility(VISIBLE);
                new Handler().postDelayed(() -> {
                    saver = new DataSaver(MainActivity.this);
                    startCompression(saver.getUri());
                    builder.dismiss();
                }, 1000);

            });
            builder.setOnCancelListener(dialogInterface -> {
                binding.btnStartCompress.setVisibility(VISIBLE);
                binding.btnCancel.setVisibility(GONE);
            });

            builder.show();
        });

        binding.btnCancel.setOnClickListener(v3 -> {
            saver.saveUri(Uri.parse(""));

            binding.imgThumbnails.setVisibility(GONE);
            binding.btnPlay.setVisibility(GONE);
            binding.tvOrignalSize.setVisibility(GONE);
            binding.btnStartCompress.setVisibility(GONE);
            binding.btnCancelVideo.setVisibility(GONE);
            binding.btnCancel.setVisibility(GONE);
            binding.tvProgress.setVisibility(GONE);
            binding.animationProgress.setVisibility(GONE);

            binding.mainContents.setVisibility(VISIBLE);
            binding.btnSelectFile.setVisibility(VISIBLE);
            VideoCompressor.cancel();

        });

        binding.imgThumbnails.setOnClickListener(v4 -> {
            Intent intent = new Intent(MainActivity.this, VideoPlayer.class);
            intent.putExtra("uri", videoUri);
            startActivity(intent);

        });

        askPerms();
        loadAds();

    }

    private void loadAds() {

        UnityAds.initialize(this, gameId, false);

        if (UnityAds.isInitialized()) {
            UnityAds.load(unitID);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    UnityAds.load(unitID);
                }
            }, 5000);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        try {

            saver = new DataSaver(MainActivity.this);
            final Uri uri = saver.getUri();
            if (uri != null) {

                binding.imgThumbnails.setVisibility(VISIBLE);
                binding.tvOrignalSize.setVisibility(VISIBLE);
                binding.btnCancelVideo.setVisibility(VISIBLE);
                binding.btnPlay.setVisibility(VISIBLE);
                binding.btnStartCompress.setVisibility(VISIBLE);
                binding.btnSelectFile.setVisibility(GONE);

                setThumbnails(uri);
                originalFileSize(uri);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void askPerms() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 234);
    }

    private void chooseVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        launcher.launch(intent);
    }

    private void setThumbnails(Uri videoUri) {
        Glide.with(MainActivity.this).load(videoUri).into(binding.imgThumbnails);
    }

    @SuppressLint("SetTextI18n")
    private void originalFileSize(Uri uri) {

        try {
            AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(uri, "r");
            final long fileSize = fileDescriptor.getLength();

            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(fileSize) / Math.log10(1024));
            final String size = new DecimalFormat("#,##0.#").format(fileSize / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
            binding.tvOrignalSize.setText("Size: " + size);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static String getVideoType(Context context, @NonNull Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    @Nullable
    private VideoQuality qualityVideo() {
        if (quality.equals("Very High")) {
            return VideoQuality.VERY_HIGH;
        }
        if (quality.equals("High")) {
            return VideoQuality.HIGH;
        }
        if (quality.equals("Medium")) {
            return VideoQuality.MEDIUM;
        }
        if (quality.equals("Very Low")) {
            return VideoQuality.VERY_LOW;
        }
        if (quality.equals("Low")) {
            return VideoQuality.LOW;
        } else {
            return null;
        }
    }

    private void startCompression(Uri uri) {
        String extension = getVideoType(MainActivity.this, uri);

        File newfile = null;
        try {
            AssetFileDescriptor videoAsset = getContentResolver().openAssetFileDescriptor(uri, "r");
            FileInputStream in = videoAsset.createInputStream();

            File filepath = Environment.getExternalStorageDirectory();
            File dir = new File(filepath.getAbsolutePath() + "/" + "EVC" + "/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            newfile = new File(dir, System.currentTimeMillis() + "." + extension);

            if (newfile.exists()) {
                newfile.delete();
            }


            OutputStream out = new FileOutputStream(newfile);

            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


        assert newfile != null;
        File tempFile = newfile;
        VideoCompressor.start(
                MainActivity.this,
                uri,
                null,
                newfile.getPath(),
                null, /*String, or null*/
                new CompressionListener() {
                    @Override
                    public void onStart() {
                        binding.btnCancelVideo.setVisibility(GONE);
                        binding.tvProgress.setVisibility(VISIBLE);
                        binding.animationProgress.setVisibility(VISIBLE);
                    }

                    @Override
                    public void onSuccess() {
                        // On Compression success

                        if (UnityAds.isReady(unitID)) {
                            UnityAds.show(MainActivity.this, unitID);
                        } else {
                            goTo(tempFile);
                        }

                        goTo(tempFile);


                    }

                    @Override
                    public void onFailure(@NonNull String failureMessage) {

                        binding.btnCancel.setVisibility(GONE);
                        binding.mainContents.setVisibility(GONE);
                        binding.btnSelectFile.setVisibility(VISIBLE);
                        Toast.makeText(getApplicationContext(), failureMessage, Toast.LENGTH_SHORT).show();
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onProgress(float v) {
                        binding.imgThumbnails.setClickable(false);
                        int progress = (int) v;
                        runOnUiThread(() -> binding.tvProgress.setText(progress + "%"));
                    }

                    @Override
                    public void onCancelled() {
                        // On Cancelled
                        binding.btnCancel.setVisibility(GONE);
                        binding.mainContents.setVisibility(GONE);
                        binding.btnSelectFile.setVisibility(VISIBLE);
                    }
                }, new Configuration(
                        Objects.requireNonNull(qualityVideo()),
                        24,
                        false,
                        null
                )
        );
    }

    private void goTo(File tempFile) {
        Intent intent = new Intent(MainActivity.this, CompressedActivity.class);
        intent.putExtra("originalSize", binding.tvOrignalSize.getText());
        intent.putExtra("videoThumbnail", videoUri);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("path", tempFile);
        startActivity(intent);
        finish();

        binding.btnCancel.setVisibility(GONE);
        binding.mainContents.setVisibility(GONE);
        binding.btnSelectFile.setVisibility(VISIBLE);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        saver = new DataSaver(MainActivity.this);

        if (saver.getMode()) {
            menu.findItem(darkOn).setChecked(true);
        } else {
            menu.findItem(darkOFF).setChecked(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        saver = new DataSaver(MainActivity.this);
        if (item.getItemId() == darkOn) {
            saver.saveMode(true);
            item.setChecked(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
            } else {
                setDefaultNightMode(MODE_NIGHT_YES);
            }

        } else if (item.getItemId() == darkOFF) {
            item.setChecked(true);
            saver.saveMode(false);
            setDefaultNightMode(MODE_NIGHT_NO);
        }
        return true;
    }


    @Override
    public void onBackPressed() {

        finishAndRemoveTask();
        finishAffinity();

    }
}