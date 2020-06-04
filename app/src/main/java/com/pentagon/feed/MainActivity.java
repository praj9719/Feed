package com.pentagon.feed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION = 44;
    private Button mAdd, mInternal, mExternal;
    private LinearLayout mLayoutStorage;
    private RecyclerView mRecycler;
    private TextView mAppName;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private static List<PlayList> mAlbums;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private static boolean isPermissionGranted = false;

    public static boolean deleteMedia(String path) {
        for (int i=0; i<mAlbums.size(); i++) {
            PlayList updatedPlayList = mAlbums.get(i);
            if (mAlbums.get(i).getAddress().equals(path)){
                File file = new File(mAlbums.get(i).getAddress());
                if (!file.isDirectory()) {
                    editor.remove(updatedPlayList.getName());
                    editor.apply();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdd = findViewById(R.id.am_add);
        mInternal = findViewById(R.id.am_internal);
        mExternal = findViewById(R.id.am_external);
        mLayoutStorage = findViewById(R.id.am_linear_layout_storage);
        mAppName = findViewById(R.id.am_app_name);
        mAlbums = new ArrayList<>();
        mRecycler = findViewById(R.id.am_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setHasFixedSize(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        editor.remove("PlayList");
        editor.apply();
        mSwipeRefreshLayout = findViewById(R.id.am_swipe);
        init();
        askPermission();
        LoadMedia();
        checkForUpdates();
    }

    private void init() {
        Log.d(TAG, "init: Initialising widgets");
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLayoutStorage.getVisibility() == View.GONE){
                    mLayoutStorage.setVisibility(View.VISIBLE);
                    mAdd.setBackgroundResource(R.drawable.ic_close);
                }else {
                    mLayoutStorage.setVisibility(View.GONE);
                    mAdd.setBackgroundResource(R.drawable.ic_add);
                }
            }
        });
        mInternal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ExploreActivity.class).putExtra("filePath", "/mnt/sdcard/"));
            }
        });
        mExternal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ExploreActivity.class).putExtra("filePath", "/storage/"));
            }
        });
        mAppName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<String> keys = sharedPreferences.getAll().keySet();
                String PlayLists = "\n";
                for (String key : keys){
                    PlayLists += key;
                }
                Toast.makeText(MainActivity.this, PlayLists.trim(), Toast.LENGTH_SHORT).show();
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkForUpdates();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
    }

    private void askPermission() {
        Log.d(TAG, "askPermission: Asking for the permissions");
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)){
            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))){

            }else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
        }else {
            isPermissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                isPermissionGranted = true;
            }else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void LoadMedia() {
        Log.d(TAG, "LoadMedia: loading media");
        Set<String> keys = sharedPreferences.getAll().keySet();
        Gson gson = new Gson();
        mAlbums.clear();
        for (String key : keys){
            Log.d(TAG, "onCreate: " + key);
            String json = sharedPreferences.getString(key, "default");
            if (json.equals("default")){
                Log.d(TAG, "LoadMedia: key pair not found! key: " + key);
            }else {
                PlayList playList = gson.fromJson(json, PlayList.class);
                mAlbums.add(playList);
            }
        }
        BannerAdapter adapter = new BannerAdapter(MainActivity.this, mAlbums);
        mRecycler.setAdapter(adapter);
    }

    private void checkForUpdates() {
        Log.d(TAG, "checkForUpdates: checking for updates");
        String curDate = getCurrentDate();
        for (int i=0; i<mAlbums.size(); i++){
            PlayList updatedPlayList = mAlbums.get(i);
            File file = new File(mAlbums.get(i).getAddress());
            if (!file.isDirectory()){
                editor.remove(updatedPlayList.getName());
                editor.apply();
            }
            String oriDate = updatedPlayList.getDate();
            int diff = calcDiffDays(curDate, oriDate);
            int display = (diff + 1)*2;
            int limit = Integer.parseInt(updatedPlayList.getListSize());
            if (display > 0 && display <= limit){
                updatedPlayList.setDisplayed(String.valueOf(display));
            }else {
                updatedPlayList.setDisplayed(String.valueOf(limit));
            }
            updateMedia(updatedPlayList);
        }
        LoadMedia();
    }

    private static String getCurrentDate() {
        String format = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date current = new Date(System.currentTimeMillis());
        String currDate = sdf.format(current);
        return currDate;
    }

    private int calcDiffDays(String curDate, String oriDate) {
        try {
            String format = "MM/dd/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date current = sdf.parse(curDate);
            Date original = sdf.parse(oriDate);
            long diff = current.getTime() - original.getTime();
            int diffDays = (int) (diff/(24*60*60*1000));
            return diffDays;
        }catch (Exception e){
            Log.d(TAG, "calcDiffDays: " + e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return 0;
    }

    private static void updateMedia(PlayList updatedPlayList) {
        Log.d(TAG, "updateMedia: Updated PlayList: " + updatedPlayList.getName());
        Gson gson = new Gson();
        String json = gson.toJson(updatedPlayList);
        editor.putString(updatedPlayList.getName(), json);
        editor.commit();
    }

    public static boolean unHideMedia(String path) {
        Log.d(TAG, "unHideMedia: initialised");
        File file = new File(path);
        File noMedia = new File(path+"/.nomedia");
        if (noMedia.delete()){
            Log.d(TAG, "unHideMedia: noMedia file deleted");
        }else {
            Log.d(TAG, "unHideMedia: Failed to delete .nomedia file");
        }
        if (file.isHidden()){
            File renameFile = new File(file.getParent()+"/"+file.getName().split("\\.")[1]);
            if (file.renameTo(renameFile)){
                return true;
            }else {
                Log.d(TAG, "unHideMedia: Failed to rename File: " + file.getName());
            }
        }else {
            Log.d(TAG, "unHideMedia: file is not hidden");
        }
        return false;
    }

    public static boolean hideMedia(String path, Context context) {
        Log.d(TAG, "hideMedia: Initialised");
        File file = new File(path);
        boolean hideFile = false, hideFolder = false;
        try {
            File noMedia = new File(file.getPath() + "/.nomedia");
            int permission = ActivityCompat.checkSelfPermission((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        (Activity) context,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION
                );
            }
            if (isPermissionGranted){
                if (noMedia.createNewFile()){
                    hideFile = true;
                    Log.d(TAG, "hideMedia: noMedia file added succesfully");
                }else {
                    Log.d(TAG, "hideMedia: failed to create .nomedia file");
                }
            }else {
                Log.d(TAG, "hideMedia: isPermissionGranted: " + isPermissionGranted);
            }
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "hideMedia: hide file: " + e.getMessage());
        }
        if (!file.isHidden()){
            File newFile = new File(file.getParent()+"/."+file.getName());
            if (file.renameTo(newFile)){
                hideFolder = true;
                Log.d(TAG, "hideMedia: Folder is hidden now!");
            }else {
                Log.d(TAG, "hideMedia: failed to hide folder");
            }
        }else {
            Toast.makeText(context, "file is already hidden", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "hideMedia: file is already hidden");
        }
        return hideFile && hideFolder;
    }

    public static boolean saveMedia(String path) {
        Log.d(TAG, "saveMedia: Initialised");
        File file = new File(path);
        File listFiles[] = file.listFiles();
        int length = 0;
        if (listFiles!=null && listFiles.length!=0){
            for (int i=0; i<listFiles.length; i++){
                if (listFiles[i].getName().endsWith("mp4") || listFiles[i].getName().endsWith("mkv")){
                    length++;
                }
            }
        }
        if (length>0){
            String name = file.getName();
            String address = file.getPath();
            String date = getCurrentDate();
            String listSize = String.valueOf(length);
            String watched = "0";
            String displayed = "0";
            PlayList playList = new PlayList(name, address, date, listSize, watched, displayed);
            // add playlist object to array
            Gson gson = new Gson();
            String json = gson.toJson(playList);
            editor.putString(playList.getName(), json);
            editor.commit();
            return true;
        }else {
            Log.d(TAG, "saveMedia: empty folder: " + file.getName());
            return false;
        }
    }

    public static boolean updateWatched(String name) {
        Log.d(TAG, "updateWatched: Initialised");
        for (int i=0; i<mAlbums.size(); i++){
            if (mAlbums.get(i).getName().equals(name)){
                PlayList updatedPlayList = mAlbums.get(i);
                int watched = Integer.parseInt(updatedPlayList.getWatched()) + 1;
                updatedPlayList.setWatched(String.valueOf(watched));
                updateMedia(updatedPlayList);
                return true;
            }
        }
        return false;
    }


}
