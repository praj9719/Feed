package com.pentagon.feed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
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
    private Button mAdd, mInternal, mExtrernal;
    private LinearLayout mLayoutStorage;
    private RecyclerView mRecycler;
    private static List<PlayList> mPlayList;
    private TextView mAppName;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static String PlayList = "PlayList";
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdd = findViewById(R.id.am_add);
        mInternal = findViewById(R.id.am_internal);
        mExtrernal = findViewById(R.id.am_external);
        mLayoutStorage = findViewById(R.id.am_linear_layout_storage);
        mAppName = findViewById(R.id.am_app_name);
        mPlayList = new ArrayList<>();
        mRecycler = findViewById(R.id.am_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setHasFixedSize(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        mSwipeRefreshLayout = findViewById(R.id.am_swipe);
        init();
        askPermission();
        LoadMedia();
        checkForUpdates();
        Set<String> keys = sharedPreferences.getAll().keySet();
        for (String key : keys){
            Log.d(TAG, "onCreate: " + key);
        }
        editor.remove(".ADM");
        editor.remove(".Arial");
        editor.apply();
        Set<String> keyss = sharedPreferences.getAll().keySet();
        for (String key : keyss){
            Log.d(TAG, "onCreate: " + key);
        }
    }

    private void checkForUpdates() {
        String curDate = getCurrentDate();
        for (int i=0; i<mPlayList.size(); i++){
            PlayList updatedPlayList = mPlayList.get(i);
            String oriDate = updatedPlayList.getDate();
            int diff = calcDiffDays(curDate, oriDate);
            int display = (diff + 1)*2;
            int limit = Integer.parseInt(updatedPlayList.getListSize());
            if (display > 0 && display <= limit){
                updatedPlayList.setDisplayed(String.valueOf(display));
                updateMedia(updatedPlayList);
            }else {
                updatedPlayList.setDisplayed(String.valueOf(limit));
                updateMedia(updatedPlayList);
            }
        }
        LoadMedia();
    }

    private static void updateMedia(PlayList updatedPlayList) {
        Gson gson = new Gson();
        String json = gson.toJson(updatedPlayList);
        editor.putString(updatedPlayList.getName(), json);
        editor.commit();
    }


    private void LoadMedia() {
        String list = sharedPreferences.getString(PlayList, "default");
        Log.d(TAG, "LoadMedia: List: " + list);
        String listArray[] = list.split(",");
        if (listArray.length>1){
            mPlayList.clear();
            Gson gson = new Gson();
            for (int i=1; i<listArray.length; i++){
                String json = sharedPreferences.getString(listArray[i].trim(), "");
                PlayList Item = gson.fromJson(json, PlayList.class);
                File file = null;
                boolean isFile = false;
                try {
                    file = new File(Item.getAddress());
                    isFile = true;
                }catch (Exception e){
                    isFile = false;
                    Log.d(TAG, "LoadMedia: Execption: " + e.getMessage());
                }
                if (isFile && file != null && file.isDirectory()){
                    mPlayList.add(Item);
                }
            }
            BannerAdapter adapter = new BannerAdapter(MainActivity.this, mPlayList);
            mRecycler.setAdapter(adapter);
        }
    }

    private void init() {
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
        mExtrernal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ExploreActivity.class).putExtra("filePath", "/storage/"));
            }
        });
        mAppName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String value = "PlayList";
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putString(PlayList, value);
//                editor.commit();
                String result = sharedPreferences.getString(PlayList, "default");
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                //mResult.setText(result);

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
                }, 2000);
            }
        });

    }

    public static boolean unhideMedia(String path) {
        File file = new File(path);
        File noMedia = new File(path+"/.nomedia");
        if (noMedia.delete()){
            File renameFile = new File(file.getParent()+"/"+file.getName().split("\\.")[1]);
            if (file.renameTo(renameFile)){
                return true;
            }else {
                Log.d(TAG, "unhideMedia: Failed to rename File");
            }
        }else {
            Log.d(TAG, "unhideMedia: Failed to delete nomedia");
        }
        return false;
    }

    public static boolean hideMedia(String path) {
        File file = new File(path);
        File listFiles[] = file.listFiles();
        int length = 0;
        if (listFiles!=null && listFiles.length!=0){
            for (int i=0; i<listFiles.length; i++){
                if (listFiles[i].getName().endsWith("mp4") || listFiles[i].getName().endsWith("mkv")){
                    length++;
                }
            }
        }else {
            Log.d(TAG, "hideMedia: hideMedia: empty file");
        }
        if (length>0){
            boolean hideFile = false, hideFolder = false;
            try {
                File noMedia = new File(file.getPath() + "/.nomedia");
                if (noMedia.createNewFile()){
                    hideFile = true;
                }else {
                    Log.d(TAG, "hideMedia: failed to create .nomedia file");
                }
            } catch (IOException e) {
                Log.d("MainActivity", "hideMedia: hide file: " + e.getMessage());
            }
            File newFile = new File(file.getParent()+"/."+file.getName());
            if (file.renameTo(newFile)){
                hideFolder = true;
            }else {
                Log.d(TAG, "hideMedia: failed to hide folder");
            }
            return hideFile && hideFolder;

        }else {
            Log.d(TAG, "hideMedia: no Video file inside");
            return false;
        }
    }

    public static boolean deleteMedia(String name) {
        String list = sharedPreferences.getString(PlayList, "default");
        if (list.equals("default")){
            Log.d(TAG, "deleteMedia: list value is default");
            return false;
        }
        String listArray[] = list.split(",");
        String finalString = "default";
        if (listArray.length>0){
            for (int i=1; i<listArray.length; i++){
                if (!listArray[i].equals(name)){
                    finalString+=", " + listArray[i];
                }
            }
            editor.remove(PlayList);
            if (editor.commit()){
                Log.d(TAG, "deleteMedia: playlist removed");
            }
            editor.remove(name);
            if (editor.commit()){
                Log.d(TAG, "deleteMedia: name removed");
            }
            editor.putString(PlayList, finalString);
            if (editor.commit()){
                Log.d(TAG, "deleteMedia: replaced");
            }
            return true;
        }
        return false;
    }

    public static boolean saveMedia(String path) {
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
            // retrive array
            String list = sharedPreferences.getString(PlayList, "default");
            // add playlist name to array
            list+=", " + playList.getName();
            editor.putString(PlayList, list);
            editor.commit();
            // add playlist object to array
            Gson gson = new Gson();
            String json = gson.toJson(playList);
            editor.putString(playList.getName(), json);
            editor.commit();

            return true;
        }else {
            Log.d(TAG, "saveMedia: empty folder");
            return false;
        }
    }

    public static boolean updateWatched(String name) {
        for (int i=0; i<mPlayList.size(); i++){
            if (mPlayList.get(i).getName().equals(name)){
                PlayList updatedPlayList = mPlayList.get(i);
                int watched = Integer.parseInt(updatedPlayList.getWatched()) + 1;
                updatedPlayList.setWatched(String.valueOf(watched));
                updateMedia(updatedPlayList);
                return true;
            }
        }
        return false;
    }

//    private static boolean hideMedia(String path) {
//        // SaveMedia
//        if (hideFile && hideFolder){
//            String name = file.getName();
//            String address = file.getPath();
//            String date = getCurrentDate();
//            String listSize = String.valueOf(length);
//            String watched = "0";
//            String displayed = "0";
//            PlayList playList = new PlayList(name, address, date, listSize, watched, displayed);
//
//            String list = sharedPreferences.getString(PlayList, "default");
//            list+=", " + playList.getName() + ", " + playList.getAddress();
//            editor.putString(PlayList, list);
//            editor.commit();
//
//            return true;
//        }else {
//            return false;
//        }
//    }
//

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

    private void askPermission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)){
            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))){

            }else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
        }else {
            //isPermissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //isPermissionGranted = true;
            }else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }

        }
    }

}
