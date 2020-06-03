package com.pentagon.feed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PreviewActivity extends AppCompatActivity {

    private TextView mDelete, mName, mHome, mAddress, mDate, mSize, mWatched, mDisplayed;
    private ImageView mImage;
    private ListView mListView;
    private Button mCancel, mSave;
    private LinearLayout mNewLayout;
    private File directory;
    private PlayList playList;
    private List<String> mList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Intent intent = getIntent();
        String filePath = intent.getStringExtra("filePath");
        String command = intent.getStringExtra("command");
        directory = new File(filePath);
        mDelete = findViewById(R.id.ap_delete);
        mName = findViewById(R.id.ap_name);
        mHome = findViewById(R.id.ap_home);
        mAddress = findViewById(R.id.ap_address);
        mDate = findViewById(R.id.ap_date);
        mSize = findViewById(R.id.ap_size);
        mWatched = findViewById(R.id.ap_watched);
        mDisplayed = findViewById(R.id.ap_displayed);
        mImage = findViewById(R.id.ap_image);
        mListView = findViewById(R.id.ap_list);
        mCancel = findViewById(R.id.ap_cancel);
        mSave = findViewById(R.id.ap_save);
        mNewLayout = findViewById(R.id.ap_layout_new);
        mList = new ArrayList<>();
        getList();
        if (command.equals("Save")){
            saveData();
        }else {

        }

    }

    private void saveData() {
        mNewLayout.setVisibility(View.VISIBLE);
        mDelete.setVisibility(View.GONE);
        mHome.setVisibility(View.GONE);
        String name = directory.getName();
        String address = directory.getPath();
        String date = getCurrentDate();
        String listSize = String.valueOf(mList.size());
        String watched = "0";
        String displayed = "2";
        playList = new PlayList(name, address, date, listSize, watched, displayed);
        displayResult();
    }

    private void displayResult() {
        mName.setText(playList.getName());
        mAddress.setText(playList.getAddress());
        mDate.setText(playList.getDate());
        mSize.setText(playList.getListSize());
        mWatched.setText(playList.getWatched());
        mDisplayed.setText(playList.getDisplayed());
        File listFiles[] = directory.listFiles();
        if (listFiles!=null && listFiles.length!=0){
            boolean imgFound = false;
            for (int i=0; i<listFiles.length; i++){
                if (listFiles[i].getName().endsWith(".jpg") || listFiles[i].getName().endsWith(".jpeg") || listFiles[i].getName().endsWith(".png")){
                    Bitmap bitmap = BitmapFactory.decodeFile(listFiles[i].getPath());
                    mImage.setImageBitmap(bitmap);
                    imgFound = true;
                    break;
                }
            }
            if (!imgFound){
                mImage.setImageResource(R.drawable.civil);
            }
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, android.R.id.text1, mList);
        mListView.setAdapter(adapter);
    }

    private void getList() {
        File listFiles[] = directory.listFiles();
        if (listFiles!=null && listFiles.length!=0){
            for (int i=0; i<listFiles.length; i++){
                if (listFiles[i].getName().endsWith("mp4") || listFiles[i].getName().endsWith("mkv")){
                    mList.add(listFiles[i].getName());
                }
            }
        }
    }

    private String getCurrentDate() {
        String format = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date current = new Date(System.currentTimeMillis());
        String currDate = sdf.format(current);
        return currDate;
    }
}
