package com.pixel.chatapp.side_bar_menu.settings;

import static com.pixel.chatapp.home.MainActivity.nightMood;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.StorageAdapter;
import com.pixel.chatapp.all_utils.FolderSizeCalculator;
import com.pixel.chatapp.all_utils.FolderUtils;
import com.pixel.chatapp.photos.PlayFileActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageRecyclerActivity extends AppCompatActivity implements StorageAdapter.OnItemSelected, PlayFileActivity.DeleteListener {

    private static ConstraintLayout constraintLayout1, constraintLayout2;
    CheckBox checkBoxAll, checkAll2;
    TextView count_TV, size1_TV, headingTV, titleSize;
    ImageView delete_IV1, cancel_IV, arrowBackRecy;
    RecyclerView recyclerView;

    String title, title_size;
    private static ProgressBar progressBar, allItemProgressLoad, allItemProgressLoad1;

    private static long fileLength = 0;
    public static List<File> fileList;
    public static boolean isOnlongPressMood;
    StorageAdapter adapter;
    ConstraintLayout confirmDeleteLayout;
    TextView delete, cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_recycler);

        constraintLayout1 = findViewById(R.id.constraintLayout1);
        constraintLayout2 = findViewById(R.id.constraintLayout2);
        checkBoxAll = findViewById(R.id.checkBoxAll);
        checkAll2 = findViewById(R.id.checkAll2);

        count_TV = findViewById(R.id.count_TV);
        cancel_IV = findViewById(R.id.cancel_IV);
        delete_IV1 = findViewById(R.id.delete_IV1);
        allItemProgressLoad = findViewById(R.id.allItemProgressLoad);
        allItemProgressLoad1 = findViewById(R.id.allItemProgressLoad1);

        size1_TV = findViewById(R.id.size1_TV);
        headingTV = findViewById(R.id.headingTV);
        titleSize = findViewById(R.id.titleSize);
        arrowBackRecy = findViewById(R.id.arrowBackRecy);
        progressBar = findViewById(R.id.progressB_);
        recyclerView = findViewById(R.id.recyclerView);

        confirmDeleteLayout = findViewById(R.id.confirmDeleteLayout);
        delete = confirmDeleteLayout.findViewById(R.id.negative_TV_);
        cancel = confirmDeleteLayout.findViewById(R.id.cancel_TV);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        fileList = new ArrayList<>();

        PlayFileActivity.deleteListener = this;

        title = getIntent().getStringExtra("title");
        title_size = getIntent().getStringExtra("titleSize");

        headingTV.setText(title);
        titleSize.setText(title_size);

        View.OnClickListener selectAll = v ->
        {
            allItemProgressLoad.setVisibility(View.VISIBLE);
            allItemProgressLoad1.setVisibility(View.VISIBLE);
            delete_IV1.setVisibility(View.GONE);

            new Handler().postDelayed(()->{
                if(checkAll2.isChecked() || checkBoxAll.isChecked())
                {
                    isOnlongPressMood = true;
                    checkBoxAll.setChecked(true);
                    StorageAdapter.highlightAll();

                } else {
                    StorageAdapter.removeHighlightCheckbox();
                    closeLongPress();
                }

            }, 10);

        };
        checkAll2.setOnClickListener(selectAll);
        checkBoxAll.setOnClickListener(selectAll);

        delete_IV1.setOnClickListener(v -> confirmDeleteLayout.setVisibility(View.VISIBLE));

        delete.setOnClickListener(this::deleteItem);

        View.OnClickListener cancelItem = v -> {
            StorageAdapter.removeHighlightCheckbox();
            closeLongPress();
        };
        cancel_IV.setOnClickListener(cancelItem);
        cancel.setOnClickListener(cancelItem);

//        file:///storage/emulated/0/Android/data/com.pixel.chatapp/files/Media/Photos/WinnerChat_1705993376263.jpg
        arrowBackRecy.setOnClickListener(v -> onBackPressed());

        new Handler().postDelayed(()->{

            fillRecyclerAdapter();
            new Handler().postDelayed(()-> progressBar.setVisibility(View.GONE), 500);

        }, 500);

    }

    //  =========== methods ==============

    public void deleteItem(View v)
    {
        if(nightMood) v.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_orange2));
        else v.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_orange));

        allItemProgressLoad1.setVisibility(View.VISIBLE);
        delete_IV1.setVisibility(View.GONE);

        for (int i = 0; i < fileList.size(); i++) {

            File file = fileList.get(i);

            boolean exist = false;

            if(file.exists())
            {
                file.delete();

                if(StorageAdapter.itemUri.contains(file.getPath())) exist = true;

            } else {
                Toast.makeText(this, getString(R.string.fileNotExist), Toast.LENGTH_SHORT).show();
            }

            if(exist){
                int position = getItemPosition(file.getPath());
                StorageAdapter.itemUri.remove(position);
                adapter.notifyItemRemoved(position);
            }

            if(i == fileList.size()-1){
                allItemProgressLoad1.setVisibility(View.GONE);
                delete_IV1.setVisibility(View.VISIBLE);
                v.setBackgroundColor(0);
                closeLongPress();
                updateItemCountAndSize();

            }
        }
    }

    private int getItemPosition(String filePath){

        for (int i = 0; i < StorageAdapter.itemUri.size(); i++) {
            String file = StorageAdapter.itemUri.get(i);
            if(file.contains(filePath)){
                return i;
            }
        }
        return 0;
    }

    private void fillRecyclerAdapter()
    {
        adapter = new StorageAdapter(StorageActivity.voiceNoteList, this);;
        if(title.equals(getString(R.string.photos)))
        {
            adapter = new StorageAdapter(StorageActivity.photoList, this);

        } else if (title.equals(getString(R.string.audio_)))
        {
            adapter = new StorageAdapter(StorageActivity.audioList, this);

        } else if (title.equals(getString(R.string.videos)))
        {
            adapter = new StorageAdapter(StorageActivity.videoList, this);

        } else if (title.equals(getString(R.string.documents)))
        {
            adapter = new StorageAdapter(StorageActivity.documentList, this);
        }

        recyclerView.setAdapter(adapter);

        adapter.setItemSelectedListener(this);
    }

    private void updateItemCountAndSize(){

        if(title.equals(getString(R.string.photos)))
        {
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getPhotoFolder(this), files ->
            {
                String photoSize = files.getFileList().size() + " " + getString(R.string.items) + " ~ " +
                        FolderSizeCalculator.getFormattedSize(files.getFileSize());
                runOnUiThread(()-> titleSize.setText(photoSize) );
            });
            // call callback to update storage activity file size
            storageRecordListener.deleteFinish("photo");
            storageRecordListener.deleteFinish("files");

        } else if (title.equals(getString(R.string.audio_)))
        {
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getAudioFolder(this), files ->
            {
                String size = files.getFileList().size() + " " + getString(R.string.items) + " ~ " +
                        FolderSizeCalculator.getFormattedSize(files.getFileSize());
                runOnUiThread(()-> titleSize.setText(size) );
            });
            storageRecordListener.deleteFinish("audio");
            storageRecordListener.deleteFinish("files");

        } else if (title.equals(getString(R.string.videos)))
        {
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getVideoFolder(this), files ->
            {
                String size = files.getFileList().size() + " " + getString(R.string.items) + " ~ " +
                        FolderSizeCalculator.getFormattedSize(files.getFileSize());
                runOnUiThread(()-> titleSize.setText(size) );
            });
            storageRecordListener.deleteFinish("video");
            storageRecordListener.deleteFinish("files");

        } else if (title.equals(getString(R.string.documents)))
        {
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getDocumentFolder(this), files ->
            {
                String size = files.getFileList().size() + " " + getString(R.string.items) + " ~ " +
                        FolderSizeCalculator.getFormattedSize(files.getFileSize());
                runOnUiThread(()-> titleSize.setText(size) );
            });
            storageRecordListener.deleteFinish("document");
            storageRecordListener.deleteFinish("files");

        } else if (title.equals(getString(R.string.voice_note))) {
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getVoiceNoteFolder(this), files ->
            {
                String size = files.getFileList().size() + " " + getString(R.string.items) + " ~ " +
                        FolderSizeCalculator.getFormattedSize(files.getFileSize());
                runOnUiThread(()-> titleSize.setText(size) );
            });
            storageRecordListener.deleteFinish("voiceNote");
            storageRecordListener.deleteFinish("files");
        }
    }

    private void closeLongPress(){
        constraintLayout1.setVisibility(View.GONE);
        constraintLayout2.setVisibility(View.VISIBLE);

        fileLength = 0;
        size1_TV.setText(null);
        fileList.clear();
        allItemProgressLoad.setVisibility(View.GONE);
        allItemProgressLoad1.setVisibility(View.GONE);
        isOnlongPressMood = false;
        checkAll2.setChecked(false);
        checkBoxAll.setChecked(false);
        confirmDeleteLayout.setVisibility(View.GONE);

    }

    @Override
    public void onLongPressItem(File item) {
        constraintLayout1.setVisibility(View.VISIBLE);
        constraintLayout2.setVisibility(View.INVISIBLE);
        delete_IV1.setVisibility(View.VISIBLE);

        if(fileList.contains(item)){
            fileLength -= item.length();
            fileList.remove(item);

            if(fileList.size() == 0){
                closeLongPress();
                StorageAdapter.removeHighlightCheckbox();
            }

        } else {
            fileList.add(item);
            fileLength += item.length();
        }

        String size = "• " + FolderSizeCalculator.getFormattedSize(fileLength);
        size1_TV.setText(size);

        String totalSelected = "" + fileList.size();
        count_TV.setText(totalSelected);
    }

    @Override
    public void onSelectAll(File file) {
        constraintLayout1.setVisibility(View.VISIBLE);
        constraintLayout2.setVisibility(View.INVISIBLE);

        checkBoxAll.setChecked(true);

        if(!fileList.contains(file)) {
            fileList.add(file);
            fileLength += file.length();
        }

        String size = "• " + FolderSizeCalculator.getFormattedSize(fileLength);
        size1_TV.setText(size);

        String totalSelected = ""+ fileList.size();
        count_TV.setText(totalSelected);
    }

    @Override
    public void highlighSuccess() {
        allItemProgressLoad.setVisibility(View.GONE);
        allItemProgressLoad1.setVisibility(View.GONE);
        delete_IV1.setVisibility(View.VISIBLE);
        checkAll2.setChecked(false);
    }

    @Override
    public void onDelete() {
        deleteItem(delete_IV1);
    }

    public static UpdateStorageRecord storageRecordListener;


    public interface UpdateStorageRecord {
        void deleteFinish(String type);
    }

    //  ===     methods =========
//    public static List<String> getFilesInFolder(String folderPath) {
//        List<String> fileList = new ArrayList<>();
//        File folder = new File(folderPath);
//
//        if (folder.exists() && folder.isDirectory()) {
//            File[] files = folder.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    fileList.add(new FileItem(file.getName(), file.length()));
//                }
//            }
//        }
//
//        return fileList;
//    }


}








