package com.pentagon.feed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.media.tv.TvContentRating;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaActivity extends AppCompatActivity {
    private static final String TAG = "MediaActivity";
    private static final int REQUEST_PERMISSION = 44;
    private PlayList playList;
    private File directory;
    private boolean mPermission = false;
    private Spinner mSpinner;
    private Button mSet;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmpty;
    private String fileName;
    private RecyclerView mRecycler;
    private List<File> mList;
    private List<File> mSortList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        initFile(fileName);
        permissionForVideo();
        mList = new ArrayList<>();
        mSortList = new ArrayList<>();
        mRecycler = findViewById(R.id.media_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setHasFixedSize(true);
        mSpinner = findViewById(R.id.media_spinner);
        mSwipeRefreshLayout = findViewById(R.id.media_swipe);
        mSet = findViewById(R.id.media_set);
        mEmpty = findViewById(R.id.media_empty);
        if (mPermission){
            init();
            inbox();
        }
        mSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = mSpinner.getSelectedItemPosition();
                switch (pos){
                    case 0:
                        inbox();
                        break;
                    case 1:
                        watched();
                        break;
                    case 2:
                        upcoming();
                        break;
                    default:
                        inbox();
                        break;
                }
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initFile(fileName);
                init();
                int pos = mSpinner.getSelectedItemPosition();
                switch (pos){
                    case 0:
                        inbox();
                        break;
                    case 1:
                        watched();
                        break;
                    case 2:
                        upcoming();
                        break;
                    default:
                        inbox();
                        break;
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

    }

    private void inbox() {
        int start = Integer.parseInt(playList.getWatched());
        int end = Integer.parseInt(playList.getDisplayed());
        mSortList.clear();
        for (int i=start; i<end; i++){
            mSortList.add(mList.get(i));
        }
        if (mSortList.size()>0){
            mEmpty.setVisibility(View.INVISIBLE);
        }else {
            mEmpty.setVisibility(View.VISIBLE);
        }
        VideoAdapter adapter = new VideoAdapter(MediaActivity.this, mSortList, "inbox", fileName);
        mRecycler.setAdapter(adapter);
    }

    private void watched() {
        int start = Integer.parseInt(playList.getWatched())-1;
        int end = -1;
        mSortList.clear();
        for (int i=start; i>end; i--){
            mSortList.add(mList.get(i));
        }
        if (mSortList.size()>0){
            mEmpty.setVisibility(View.INVISIBLE);
        }else {
            mEmpty.setVisibility(View.VISIBLE);
        }
        VideoAdapter adapter = new VideoAdapter(MediaActivity.this, mSortList, "watched", fileName);
        mRecycler.setAdapter(adapter);
    }

    private void upcoming() {
        int start = Integer.parseInt(playList.getDisplayed());
        int end = Integer.parseInt(playList.getListSize());
        mSortList.clear();
        for (int i=start; i<end; i++){
            mSortList.add(mList.get(i));
        }
        if (mSortList.size()>0){
            mEmpty.setVisibility(View.INVISIBLE);
        }else {
            mEmpty.setVisibility(View.VISIBLE);
        }
        VideoAdapter adapter = new VideoAdapter(MediaActivity.this, mSortList, "upcoming", fileName);
        mRecycler.setAdapter(adapter);
    }



    private void init() {
        File listFiles[] = directory.listFiles();
        if (listFiles!=null && listFiles.length!=0){
            mList.clear();
            for (int i=0; i<listFiles.length; i++){
                if (listFiles[i].getName().endsWith("mp4") || listFiles[i].getName().endsWith("mkv")){
                    mList.add(listFiles[i]);
                }
            }
        }
    }

    private void initFile(String fileName) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(fileName.trim(), "default");
        playList = gson.fromJson(json, PlayList.class);
        directory = new File(playList.getAddress());
    }



    // Using but not sure if it is necessary
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mPermission = true;
            }else {
                Toast.makeText(this, "Permission not allowed", Toast.LENGTH_SHORT).show();
            }

        }
    }
    private void permissionForVideo() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)){
            if ((ActivityCompat.shouldShowRequestPermissionRationale(MediaActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))){

            }else {
                ActivityCompat.requestPermissions(MediaActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
        }else {
            mPermission = true;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK) {
                    String path = "";
                    path += "\nPath:\t" + data.getData().getPath();
                    path += "\nLastPathSegment:\t" + data.getData().getLastPathSegment();

                }
                break;
        }
    }
}
