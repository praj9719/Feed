package com.pentagon.feed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> implements PopupMenu.OnMenuItemClickListener {
    private Context mContext;
    private List<PlayList> mList;
    private int count = 0;

    public BannerAdapter(Context mContext, List<PlayList> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public BannerAdapter.BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_item, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerAdapter.BannerViewHolder holder, final int position) {
        final PlayList playList = mList.get(position);
        holder.mName.setText(playList.getName());
        holder.mAddress.setText(playList.getAddress());
        holder.mDate.setText(playList.getDate());
        holder.mListSize.setText(playList.getListSize());
        holder.mWatched.setText(playList.getWatched());
        holder.mDisplayed.setText(playList.getDisplayed());
        int diff = Integer.parseInt(playList.getDisplayed()) - Integer.parseInt(playList.getWatched());
        holder.mCount.setText(String.valueOf(diff));

        final File file = new File(playList.getAddress());
        File listFiles[] = file.listFiles();
        if (listFiles!=null && listFiles.length!=0){
            boolean imgFound = false;
            for (int i=0; i<listFiles.length; i++){
                if (listFiles[i].getName().endsWith(".jpg") || listFiles[i].getName().endsWith(".jpeg") || listFiles[i].getName().endsWith(".png")){
                    Bitmap bitmap = BitmapFactory.decodeFile(listFiles[i].getPath());
                    holder.mImage.setImageBitmap(bitmap);
                    imgFound = true;
                    break;
                }
            }
            if (!imgFound){
                holder.mImage.setImageResource(R.drawable.civil);
            }
            holder.mCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext.startActivity(new Intent(mContext, MediaActivity.class).putExtra("fileName", mList.get(position).getName()));
                }
            });
        }
        holder.mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = position;
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
            case R.id.popup_banner_remove:
                boolean isUnhide = MainActivity.unhideMedia(mList.get(count).getAddress());
                if (isUnhide){
                    boolean isDelete = MainActivity.deleteMedia(mList.get(count).getName());
                    if (isDelete){
                        Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(mContext, "Failed to remove from database", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(mContext, "Failed to unhide", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return false;
        }
    }

    public class BannerViewHolder extends RecyclerView.ViewHolder {
        private TextView mName, mAddress, mCount, mDate, mListSize, mWatched, mDisplayed, mMore;
        private ImageView mImage;
        private CardView mCard;
        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.bn_name);
            mAddress = itemView.findViewById(R.id.bn_address);
            mCount = itemView.findViewById(R.id.bn_count);
            mDate = itemView.findViewById(R.id.bn_date);
            mListSize = itemView.findViewById(R.id.bn_size);
            mWatched = itemView.findViewById(R.id.bn_watched);
            mDisplayed = itemView.findViewById(R.id.bn_displayed);
            mImage = itemView.findViewById(R.id.bn_image);
            mCard = itemView.findViewById(R.id.bn_card);
            mMore = itemView.findViewById(R.id.bn_more);
        }
    }

    private void showPopupMenu(View view){
        PopupMenu popupMenu = new PopupMenu(mContext, view);
        popupMenu.inflate(R.menu.popup_banner_menu);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }


}
