package com.gulehri.edu.pk.easyvideocompressor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;


public class DataSaver {
    private final SharedPreferences sp;
    private final SharedPreferences.Editor edit;

    public DataSaver(@NonNull Context context) {
        sp = context.getSharedPreferences("mode", Context.MODE_PRIVATE);
        edit = sp.edit();
    }

    public void saveMode(boolean darkMode) {
        edit.putBoolean("darkMode", darkMode);
        edit.apply();
    }

    public void saveUri(@NonNull Uri uri) {
        edit.putString("uri", uri.toString());
        edit.apply();
    }

    public Uri getUri() {
        String uriString = sp.getString("uri", null);
        if (uriString != null && !uriString.isEmpty()) {
            return Uri.parse(uriString);
        } else return null;

    }


    public boolean getMode() {
        return sp.getBoolean("darkMode", false);
    }

}
