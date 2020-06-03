package com.pentagon.feed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder> implements PopupMenu.OnMenuItemClickListener{

    private Context context;
    private List<File> mList;
    private String state;
    private String fileName;

    public VideoAdapter(Context context, List<File> mList, String state, String fileName) {
        this.context = context;
        this.mList = mList;
        this.state = state;
        this.fileName = fileName;
    }

    @NonNull
    @Override
    public VideoAdapter.VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_layout, parent, false);
        return new VideoHolder(view);    }

    @Override
    public void onBindViewHolder(@NonNull final VideoAdapter.VideoHolder holder, final int position) {
        final File file = mList.get(position);
        if (state.equals("inbox")){
            if (position == 0){
                holder.mMore.setVisibility(View.VISIBLE);
            }
//            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
//            holder.mImage.setImageBitmap(bitmap);
//            holder.mImage.setImageResource(R.drawable.civil);
        }else {
//            holder.mImage.setImageResource(R.drawable.civil);
        }
        Glide.with(context)
                .load(Uri.fromFile(file))
                .into(holder.mImage);
        holder.mName.setText(file.getName());
        if (!state.equals("upcoming")){
            holder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, VideoActivity.class);
                    intent.putExtra("filePath", file.getPath());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setDataAndType(Uri.parse(file.getPath()), "video/*");
                }
            });
        }
        holder.mDetails.setText("Size: " + (file.length()/1000000) + " MB");
        holder.mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.video_menu_watched:
                boolean watchedUpdate = MainActivity.updateWatched(fileName);
                if (watchedUpdate){
                    notifyItemRemoved(0);
                    Toast.makeText(context, "Video Watched!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, "Task Failed", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return false;
        }
    }


    public class VideoHolder extends RecyclerView.ViewHolder {
        TextView mName, mDetails, mMore;
        ImageView mImage;
        LinearLayout mLayout;
        public VideoHolder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.vl_name);
            mImage = itemView.findViewById(R.id.vl_image);
            mDetails = itemView.findViewById(R.id.vl_details);
            mMore = itemView.findViewById(R.id.vl_more);
            mLayout = itemView.findViewById(R.id.vl_layout);
        }
    }


    private void showPopupMenu(View view){
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.popup_video_menu);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

}
