package com.pentagon.feed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExploreActivity extends AppCompatActivity {

    private File directory;
    private List<File> mList;
    private static final int REQUEST_PERMISSION = 1;
    private boolean isPermissionGranted = false;
    private static Boolean mHidden = true;
    private Button mBack, mHide;
    private TextView mName, mAddress;
    private RecyclerView mRecycler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        final Intent intent = getIntent();
        String filePath = intent.getStringExtra("filePath");
        mList = new ArrayList<>();
        directory = new File(filePath);
        mBack = findViewById(R.id.ae_back);
        mHide = findViewById(R.id.ae_hide);
        mName = findViewById(R.id.ae_file_name);
        mAddress = findViewById(R.id.ae_address);
        mRecycler = findViewById(R.id.ae_recycler);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        init();
    }

    private void init() {
        if (mHidden){
            mHide.setBackgroundResource(R.drawable.ic_hidden);
        }else {
            mHide.setBackgroundResource(R.drawable.ic_visible);
        }
        mHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHidden = !mHidden;
                if (mHidden){
                    mHide.setBackgroundResource(R.drawable.ic_hidden);
                }else {
                    mHide.setBackgroundResource(R.drawable.ic_visible);
                }
                Explore(directory);
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    finish();
                }catch (Exception e){
                    Toast.makeText(ExploreActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Explore(directory);
            }
        });
    }

    private void Explore(File directory) {
        mName.setText(directory.getName());
        mAddress.setText(directory.getPath());
        File listFile[] = directory.listFiles();
        if (listFile != null && listFile.length >0){
            mList.clear();
            for (int i=0; i<listFile.length; i++){
                if (mHidden){
                    if (!listFile[i].isHidden()){
                        mList.add(listFile[i]);
                    }
                }else {
                    mList.add(listFile[i]);
                }
            }
            Collections.sort(mList);
            FileAdapter adapter = new FileAdapter(ExploreActivity.this, mList);
            mRecycler.setAdapter(adapter);
        }
    }

    private void askPermission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)){
            if ((ActivityCompat.shouldShowRequestPermissionRationale(ExploreActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))){

            }else {
                ActivityCompat.requestPermissions(ExploreActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
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

    @Override
    protected void onStart() {
        super.onStart();
        askPermission();
        if (mHidden){
            mHide.setBackgroundResource(R.drawable.ic_hidden);
        }else {
            mHide.setBackgroundResource(R.drawable.ic_visible);
        }
        if (isPermissionGranted){
            Explore(directory);
        }
    }
}
