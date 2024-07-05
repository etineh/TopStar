package com.pixel.chatapp.adapters;

import static com.pixel.chatapp.home.MainActivity.nightMood;
import static com.pixel.chatapp.side_bar_menu.settings.StorageRecyclerActivity.isOnlongPressMood;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pixel.chatapp.R;
import com.pixel.chatapp.utils.FileUtils;
import com.pixel.chatapp.utils.OpenActivityUtil;
import com.pixel.chatapp.photos.PlayFileActivity;
import com.pixel.chatapp.photos.ZoomImage;
import com.pixel.chatapp.side_bar_menu.settings.StorageRecyclerActivity;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.ItemViewHolder> {

    public static List<String> itemUri;
    private Context context;

    private static List<ItemViewHolder> itemViewList;
    private static List<ItemViewHolder> allViewList;


    public StorageAdapter(List<String> itemUri, Context context) {
        this.itemUri = itemUri;
        this.context = context;
        itemViewList = new ArrayList<>();
        allViewList = new ArrayList<>();

    }

    private static OnItemSelected itemSelectedListener;

    public void setItemSelectedListener(OnItemSelected itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    public interface OnItemSelected {
        void onLongPressItem(File item);
        void onSelectAll(File file);
        void highlighSuccess();

    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_card, parent, false);
        return new StorageAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        holder.showItem_IV.setImageURI(null);
        holder.showItem_IV.setBackgroundColor(0);
        holder.showItem_IV.setBackground(null);
        holder.checkBoxTerms_.setVisibility(View.GONE);
        holder.playIcon.setVisibility(View.GONE);

        if(!allViewList.contains(holder)) allViewList.add(holder);

        String item = itemUri.get(position);
        File file = new File(item);
        Uri uri = Uri.fromFile(new File(item));

//  file:///storage/emulated/0/Android/data/com.pixel.chatapp/files/Media/Audio/Topper_599114Pheelz-JELO-feat-Young-Jonn.mp3
        String extension = item.substring(item.lastIndexOf('.') + 1).toLowerCase();

        String sizeAndDate = FileUtils.getFileSize(uri, context) + "\n"+ FileUtils.getFileCreationDate(uri);

        holder.sizeTV.setText( sizeAndDate );

        displaySettings(holder, uri, file, extension);

        holder.itemView.setOnLongClickListener(v ->
        {
            isOnlongPressMood = true;

           longPressMethod(file, holder);

            return true;
        });


        View.OnClickListener onClickListener = v ->
        {
            if(isOnlongPressMood){
                longPressMethod(file, holder);

            } else {

                if(holder.checkBoxTerms_.getVisibility() == View.VISIBLE){
                    holder.checkBoxTerms_.setChecked(false);
                    holder.checkBoxTerms_.setVisibility(View.GONE);
                }

                // open file
                if(extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png"))
                {
                    Intent intent = new Intent(context, ZoomImage.class);
                    intent.putExtra("otherName", "Photo");
                    intent.putExtra("imageLink", uri.toString());
                    OpenActivityUtil.openColorHighlight(v, context, intent);

                } else if(extension.equals("mp4") || extension.equals("mov"))
                {
                    Intent intent = new Intent(context, PlayFileActivity.class);
                    intent.putExtra("file", item);
                    intent.putExtra("type", "video");
                    OpenActivityUtil.openColorHighlight(v, context, intent);

                } else if(extension.equals("mp3") || extension.equals("wav") || extension.equals("3gp") || extension.equals("opus"))
                {
                    Intent intent = new Intent(context, PlayFileActivity.class);
                    intent.putExtra("file", item);
                    intent.putExtra("type", "audio");
                    OpenActivityUtil.openColorHighlight(v, context, intent);

                } else{

                    try {
                        FileUtils.openDocumentFromUrl(context, item);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }

                }

            }
        };
        holder.itemView.setOnClickListener(onClickListener);
        holder.checkBoxTerms_.setOnClickListener(onClickListener);

    }


    //  =========   methods     =============
    private void displaySettings(ItemViewHolder holder, Uri uri, File file, String extension)
    {
        // set the checkout when scrolling
        if(StorageRecyclerActivity.fileList.contains(file) || itemViewList.contains(holder))
        {
            holder.checkBoxTerms_.setChecked(true);
            holder.checkBoxTerms_.setVisibility(View.VISIBLE);
        } else {
            holder.checkBoxTerms_.setChecked(false);
            holder.checkBoxTerms_.setVisibility(View.GONE);
        }
        
        //file type detection logic
        if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png") || extension.equals("mp4") || extension.equals("mov") )
        {
            holder.showItem_IV.setBackgroundColor(0);
            Glide.with(context).load(uri).into(holder.showItem_IV);

            if(extension.equals("mp4") || extension.equals("mov")){
                holder.playIcon.setVisibility(View.VISIBLE);
            }

        } else {
            if(extension.equals("3gp") || extension.equals("opus")) {   // voice note
                holder.showItem_IV.setImageResource(R.drawable.baseline_mic_24);

            } else if(extension.equals("mp3") || extension.equals("wav")) {
                holder.showItem_IV.setImageResource(R.drawable.baseline_audio_file_24);  // audio

            } else holder.showItem_IV.setImageResource(R.drawable.baseline_document_scanner_24);    // other files

            if(nightMood){
                holder.showItem_IV.setBackgroundColor(ContextCompat.getColor(context, R.color.black));  // background
                Drawable drawable = holder.showItem_IV.getDrawable().mutate(); // tint drawable
                DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.cool_orange)); // Set the tint color
            } else {
                holder.showItem_IV.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                Drawable drawable = holder.showItem_IV.getDrawable().mutate(); // Get the drawable and mutate it to make it unique
                DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.orange)); // Set the tint color
            }

        }
    }

    private void longPressMethod(File file, ItemViewHolder holder)
    {
        if(StorageRecyclerActivity.fileList.contains(file))
        {
            holder.checkBoxTerms_.setChecked(false);
            holder.checkBoxTerms_.setVisibility(View.GONE);
            itemViewList.remove(holder);
        } else {
            holder.checkBoxTerms_.setChecked(true);
            holder.checkBoxTerms_.setVisibility(View.VISIBLE);
            itemViewList.add(holder);
        }

        itemSelectedListener.onLongPressItem(file);

    }

    public static void highlightAll(){
        for (int i = 0; i < itemUri.size(); i++) {  // add file

            String item = itemUri.get(i);
            File file = new File(item);
            itemSelectedListener.onSelectAll(file);

            if(i == itemUri.size()-1){
                itemSelectedListener.highlighSuccess();
            }

        }

        itemViewList.addAll(allViewList);   // add holder
        for (int i = 0; i < allViewList.size(); i++) {
            ItemViewHolder holder = allViewList.get(i);

            holder.checkBoxTerms_.setChecked(true);
            holder.checkBoxTerms_.setVisibility(View.VISIBLE);
        }
    }

    // remove checkout highlight selected
    public static void removeHighlightCheckbox()
    {
        for (int i = 0; i < itemViewList.size(); i++) {
            ItemViewHolder holder = itemViewList.get(i);

            holder.checkBoxTerms_.setChecked(false);
            holder.checkBoxTerms_.setVisibility(View.GONE);

            if(i == itemViewList.size()-1) itemViewList.clear();
        }

    }

    @Override
    public int getItemCount() {
        return itemUri.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView showItem_IV;
        CheckBox checkBoxTerms_;
        private TextView sizeTV;
        ImageView playIcon;

        public ItemViewHolder(@NonNull View itemView)
        {
            super(itemView);
            showItem_IV = itemView.findViewById(R.id.photoCard_IV);
            checkBoxTerms_ = itemView.findViewById(R.id.checkBoxTerms_);
            sizeTV = itemView.findViewById(R.id.sizeTV);
            playIcon = itemView.findViewById(R.id.playIcon_);

        }
    }

}
