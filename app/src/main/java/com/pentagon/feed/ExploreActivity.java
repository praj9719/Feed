package com.pentagon.feed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExploreActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, EditDialog.EditDialogListner {

    private File directory;
    private List<File> mList;
    private static final int REQUEST_PERMISSION = 44;
    private boolean isPermissionGranted = false;
    private static Boolean mHidden = true;
    private Button mBack, mHide;
    private TextView mName, mAddress, mMore;
    private RecyclerView mRecycler;
    private SwipeRefreshLayout mSwipe;
    private Boolean mCreateFolder = false, mCreateFile = false;
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
        mMore = findViewById(R.id.ae_more);
        mSwipe = findViewById(R.id.ae_swipe);
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
        mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Explore(directory);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipe.setRefreshing(false);
                    }
                }, 1000);
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

    private void showPopupMenu(View view){
        PopupMenu popupMenu = new PopupMenu(ExploreActivity.this, view);
        popupMenu.inflate(R.menu.popup_explore_menu);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.popup_explore_new_file:
                EditDialog editDialog = new EditDialog();
                editDialog.show(getSupportFragmentManager(), "Edit Dialog");
                mCreateFile = true;
                return true;
            case R.id.popup_explore_new_folder:
                EditDialog editDialog1 = new EditDialog();
                editDialog1.show(getSupportFragmentManager(), "Edit Dialog");
                mCreateFolder = true;
                return true;
        }
        return false;
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


    @Override
    public void applyResult(String text) {
        if (mCreateFile && mCreateFolder){
            mCreateFile = false;
            mCreateFolder = false;
            Toast.makeText(this, "Error occurred! try again", Toast.LENGTH_SHORT).show();
        }else {
            if (mCreateFolder){
                File folder = new File(directory.getPath() + "/" + text);
                if (folder.mkdir()){
                    Toast.makeText(ExploreActivity.this, "Folder Created!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(ExploreActivity.this, "Failed To Create!", Toast.LENGTH_SHORT).show();
                }
                mCreateFolder = false;
                Explore(directory);
            }
            if (mCreateFile){
                try {
                    File newFile = new File(directory.getPath() + "/" + text);
                    if (newFile.createNewFile()){
                        Toast.makeText(this, "New File Created", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(this, "Failed to create new file", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                mCreateFile = false;
                Explore(directory);
            }
        }
    }
}
