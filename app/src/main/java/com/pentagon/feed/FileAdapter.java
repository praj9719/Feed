package com.pentagon.feed;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> implements PopupMenu.OnMenuItemClickListener {

    private Context context;
    private List<File> mList;
    int count = 0;

    public FileAdapter(Context context, List<File> mList) {
        this.context = context;
        this.mList = mList;
    }


    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_view, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, final int position) {
        final File file = mList.get(position);
        holder.mName.setText(file.getName());

        String details = "null";
        if (file.isDirectory() && file.listFiles() != null && file.listFiles().length > 0){
            details = file.listFiles().length + " Items";
        }else if (!file.isDirectory()){
            details = String.valueOf(file.length()/1000000.0) + " MB";
        }
        holder.mDetails.setText(details);

        String imgType = file.isDirectory() ? "ic_folder" : "ic_file";
        if (file.isHidden()){
            imgType = file.isDirectory() ? "ic_folder_hidden" : "ic_hidden";
        }
        String uri = "drawable/" + imgType;
        int imgResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
        holder.mType.setImageDrawable(context.getResources().getDrawable(imgResource));

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (file.isDirectory()){
                    context.startActivity(new Intent(context, ExploreActivity.class).putExtra("filePath", file.getPath()));
                }else {
                    Toast.makeText(context, "Not a Directory!\n" + file.getPath(), Toast.LENGTH_SHORT).show();
                }
            }
        });


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


    public class FileViewHolder extends RecyclerView.ViewHolder {
        private TextView mName, mDetails, mMore;
        private LinearLayout mLayout;
        private ImageView mType;
        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.fv_name);
            mDetails = itemView.findViewById(R.id.fv_details);
            mMore = itemView.findViewById(R.id.fv_more);
            mLayout = itemView.findViewById(R.id.fv_layout);
            mType = itemView.findViewById(R.id.fv_image);
        }

    }
    private void showPopupMenu(View view){
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.popup_hide:
                hideMedia();
                return true;
            case R.id.popup_select:
                selectMedia();
                return true;
            case R.id.popup_restore:
                restoreMedia();
                return true;
            default:
                return false;
        }
    }

    private void hideMedia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(mList.get(count).getName())
                .setMessage(mList.get(count).getPath())
                .setPositiveButton("Hide", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean isHidden = MainActivity.hideMedia(mList.get(count).getPath());
                        if (isHidden){
                            Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context, "FileAdapter: hideMedia: Failed", Toast.LENGTH_SHORT).show();
                        }
//                        context.startActivity(new Intent(context, PreviewActivity.class)
//                                .putExtra("filePath", mList.get(count).getPath())
//                                .putExtra("command", "Save"));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.show();
    }

    private void selectMedia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(mList.get(count).getName())
                .setMessage(mList.get(count).getPath())
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean isSaved = MainActivity.saveMedia(mList.get(count).getPath());
                        if (isSaved){
                            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.show();
    }

    private void restoreMedia() {

    }


    private void selectFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(mList.get(count).getName())
                .setMessage(mList.get(count).getPath())
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean isSaved = MainActivity.hideMedia(mList.get(count).getPath());
                        if (isSaved){
                            Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show();
                            //context.startActivity(new Intent(context, MainActivity.class));
                        }else {
                            Toast.makeText(context, "Invalid Argument", Toast.LENGTH_SHORT).show();
                        }
//                        context.startActivity(new Intent(context, PreviewActivity.class)
//                                .putExtra("filePath", mList.get(count).getPath())
//                                .putExtra("command", "Save"));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.show();
    }



}





/*

    private void hideFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(mList.get(count).getName())
                .setMessage(mList.get(count).getPath())
                .setPositiveButton("Hide/Show", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File file = mList.get(count);
                        if (file.isDirectory()){
                            if (file.isHidden()){
                                File newFile = new File(file.getParent()+"/"+file.getName().split("\\.")[1]);
                                if (file.renameTo(newFile)){
                                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                File newFile = new File(file.getParent()+"/."+file.getName());
                                if (file.renameTo(newFile)){
                                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.show();
    }

    private void deleteFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(mList.get(count).getName())
                .setMessage(mList.get(count).getPath())
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mList.get(count).delete()){
                            Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        }else {
                            Toast.makeText(context, "Failed To Delete", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.show();
    }



 */