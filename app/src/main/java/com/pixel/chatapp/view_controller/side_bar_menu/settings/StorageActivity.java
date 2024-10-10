package com.pixel.chatapp.view_controller.side_bar_menu.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.pixel.chatapp.R;
import com.pixel.chatapp.utilities.CacheUtils;
import com.pixel.chatapp.utilities.FolderSizeCalculator;
import com.pixel.chatapp.utilities.FolderUtils;
import com.pixel.chatapp.utilities.OpenActivityUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageActivity extends AppCompatActivity implements StorageRecyclerActivity.UpdateStorageRecord {

    TextView totalStorage;
    ImageView arrowBackSo;
    ConstraintLayout photoLayout, videoLayout, voicenoteLayout, documentLayout, audioLayout;
    ConstraintLayout thumbnailLayout, cacheLayout, localDatabaseLayout, othersLayout;

    TextView photoSize_TV, videoSize_TV, voiceNSize_TV, docSize_TV, thumbnailSize_TV, cacheSize_TV,
            audioSize_TV, localDBSize_TV, otherSize_TV;
    ProgressBar progressBarStore;

    public static List<String> allFilesList, photoList, videoList, audioList, voiceNoteList, documentList, thumbList;

    ConstraintLayout confirmDeleteLayout;
    TextView delete, msg_TV, heading, cancel_TV;

    String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        arrowBackSo = findViewById(R.id.arrowBackSo);
        totalStorage = findViewById(R.id.totalStorage);
        photoLayout = findViewById(R.id.photoLayout);
        videoLayout = findViewById(R.id.videoLayout);
        voicenoteLayout = findViewById(R.id.voicenoteLayout);
        documentLayout = findViewById(R.id.documentLayout);
        thumbnailLayout = findViewById(R.id.thumbnailLayout);
        cacheLayout = findViewById(R.id.cacheLayout);
        localDatabaseLayout = findViewById(R.id.localDatabaseLayout);
        othersLayout = findViewById(R.id.othersLayout);
        audioLayout = findViewById(R.id.audioLayout);

        photoSize_TV = findViewById(R.id.photoSize_TV);
        videoSize_TV = findViewById(R.id.videoSize_TV);
        voiceNSize_TV = findViewById(R.id.voiceNSize_TV);
        docSize_TV = findViewById(R.id.docSize_TV);
        thumbnailSize_TV = findViewById(R.id.thumbnailSize_TV);
        cacheSize_TV = findViewById(R.id.cacheSize_TV);
        localDBSize_TV = findViewById(R.id.localDBSize_TV);
        otherSize_TV = findViewById(R.id.otherSize_TV);
        audioSize_TV = findViewById(R.id.audioSize_TV);
        progressBarStore = findViewById(R.id.progressBarStore);

        confirmDeleteLayout = findViewById(R.id.confirmDeleteLayout);
        delete = confirmDeleteLayout.findViewById(R.id.negative_TV_);
        msg_TV = confirmDeleteLayout.findViewById(R.id.msg_TV);
        heading = confirmDeleteLayout.findViewById(R.id.title_TV);
        cancel_TV = confirmDeleteLayout.findViewById(R.id.cancel_TV);

        allFilesList = new ArrayList<>();
        photoList = new ArrayList<>();
        videoList = new ArrayList<>();
        audioList = new ArrayList<>();
        voiceNoteList = new ArrayList<>();
        documentList = new ArrayList<>();
        thumbList = new ArrayList<>();

        new Handler().postDelayed( ()-> {
            sizes("photo");
            sizes("video");
            sizes("audio");
            sizes("document");
            sizes("voiceNote");
            sizes("cache");
            sizes("thumb");
            sizes("files");
            sizes("db");
        }, 500);

        StorageRecyclerActivity.storageRecordListener = this;


        delete.setOnClickListener(v ->
        {
            if(from.equals("thumb"))
            {
                for (int i = 0; i < thumbList.size(); i++)
                {
                    File file = new File(thumbList.get(i));

                    if(file.exists()) file.delete();

                    if(i == thumbList.size()-1) {
                        cacheSize_TV.setText("0.00");
                        Toast.makeText(this, getString(R.string.deleteSuccessful), Toast.LENGTH_SHORT).show();
                    }
                }

            } else
            {
                new Thread(() -> {

                    Glide.get(getApplicationContext()).clearDiskCache();
                    CacheUtils.clearCache(this);

                }).start();

                Toast.makeText(this, getString(R.string.cacheClear), Toast.LENGTH_SHORT).show();
                cacheSize_TV.setText("0.00");
            }

            confirmDeleteLayout.setVisibility(View.GONE);

        });

        cancel_TV.setOnClickListener(v -> confirmDeleteLayout.setVisibility(View.GONE));
        confirmDeleteLayout.setOnClickListener(v -> confirmDeleteLayout.setVisibility(View.GONE));
        photoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, StorageRecyclerActivity.class);
            intent.putExtra("title", getString(R.string.photos));
            intent.putExtra("titleSize", photoSize_TV.getText().toString());

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        videoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, StorageRecyclerActivity.class);
            intent.putExtra("title", getString(R.string.videos));
            intent.putExtra("titleSize", videoSize_TV.getText().toString());

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });


        audioLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, StorageRecyclerActivity.class);
            intent.putExtra("title", getString(R.string.audio_));
            intent.putExtra("titleSize", audioSize_TV.getText().toString());

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        voicenoteLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, StorageRecyclerActivity.class);
            intent.putExtra("title", getString(R.string.voice_note));
            intent.putExtra("titleSize", voiceNSize_TV.getText().toString());

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        documentLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, StorageRecyclerActivity.class);
            intent.putExtra("title", getString(R.string.documents));
            intent.putExtra("titleSize", docSize_TV.getText().toString());

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        thumbnailLayout.setOnClickListener(v -> {

            confirmDeleteLayout.setVisibility(View.VISIBLE);
            msg_TV.setText(getString(R.string.thumbDelMsg));
            heading.setText(getString(R.string.deleteAllThumbNow));
            delete.setVisibility(View.VISIBLE);
            delete.setText(getString(R.string.delete_));
            cancel_TV.setText(getString(R.string.cancel));

            from = "thumb";
        });

        cacheLayout.setOnClickListener(v -> {
            confirmDeleteLayout.setVisibility(View.VISIBLE);
            heading.setText(getString(R.string.clearCache));
            msg_TV.setText(getString(R.string.cacheDelMsg));
            delete.setVisibility(View.VISIBLE);
            delete.setText(getString(R.string.clear));
            cancel_TV.setText(getString(R.string.cancel));

            from = "cache";
        });

        localDatabaseLayout.setOnClickListener(v -> {
            confirmDeleteLayout.setVisibility(View.VISIBLE);
            heading.setText(getString(R.string.clearDatabase));
            msg_TV.setText(getString(R.string.localDatabaseMsg));
            delete.setText("");
            delete.setVisibility(View.INVISIBLE);
            cancel_TV.setText(getString(R.string.close));

        });

        otherSize_TV.setOnClickListener(v -> {
            Toast.makeText(this, "open not permitted", Toast.LENGTH_SHORT).show();

        });

//        totalStorage.setOnClickListener(v -> {
//            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
//
//        });

        arrowBackSo.setOnClickListener(v -> onBackPressed());

    }

    private void sizes(String type)
    {
        if(type.equals("photo")){
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getPhotoFolder(this), files ->
            {
                photoList = files.getFileList();
                String photoSize = photoList.size() + " " + getString(R.string.items) + " ~ " +
                        FolderSizeCalculator.getFormattedSize(files.getFileSize());
                runOnUiThread(()-> photoSize_TV.setText(photoSize) );
            });

        } else if(type.equals("video"))
        {
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getVideoFolder(this), files ->
            {
                videoList = files.getFileList();
                String videoSize = videoList.size() + " " + getString(R.string.items) + " ~ " +
                        FolderSizeCalculator.getFormattedSize(files.getFileSize());
                runOnUiThread( ()-> videoSize_TV.setText(videoSize) );
            });

        } else if (type.equals("audio"))
        {
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getAudioFolder(this), files ->
            {
                audioList = files.getFileList();
                String audioSize = audioList.size() + " " + getString(R.string.items) + " ~ " +
                        FolderSizeCalculator.getFormattedSize(files.getFileSize());
                runOnUiThread(()-> audioSize_TV.setText(audioSize) );

            });

        } else if (type.equals("voiceNote"))
        {
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getVoiceNoteFolder(this), files ->
            {
                voiceNoteList = files.getFileList();
                String voiceNSize = voiceNoteList.size() + " " + getString(R.string.items) + " ~ " +
                        FolderSizeCalculator.getFormattedSize(files.getFileSize());
                runOnUiThread( ()-> voiceNSize_TV.setText(voiceNSize));
            });

        } else if(type.equals("document"))
        {
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getDocumentFolder(this), files ->
            {
                documentList = files.getFileList();
                String docSize = documentList.size() + " " + getString(R.string.items) + " ~ " +
                        FolderSizeCalculator.getFormattedSize(files.getFileSize());
                runOnUiThread( ()-> docSize_TV.setText(docSize) );
            });

        } else if (type.equals("thumb"))
        {
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getThumbnailFolder(this), files ->
            {
                thumbList = files.getFileList();
                String thumbSize = thumbList.size() + " " + getString(R.string.items) + " ~ " +
                        FolderSizeCalculator.getFormattedSize(files.getFileSize());
                runOnUiThread( ()-> thumbnailSize_TV.setText(thumbSize) );
            });

        } else if (type.equals("cache"))
        {
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getCacheDir(this), files ->
            {
                String cache = FolderSizeCalculator.getFormattedSize(files.getFileSize());
                runOnUiThread( ()-> cacheSize_TV.setText(cache));

            });

        } else if (type.equals("files"))
        {
            FolderSizeCalculator.fileNameAndSize(FolderUtils.getFileDir(this), files ->
            {
                int size = allFilesList.size() + photoList.size() + videoList.size() + audioList.size()
                        + voiceNoteList.size() + documentList.size() + thumbList.size();

                String allFileSize = getString(R.string.total) +" " + size + " " + getString(R.string.items) + " ~ "
                        + FolderSizeCalculator.getFormattedSize(files.getFileSize());

                runOnUiThread(()-> {
                    totalStorage.setText(allFileSize);
                    progressBarStore.setVisibility(View.GONE);
                    totalStorage.setVisibility(View.VISIBLE);
                });
            });

        } else if (type.equals("db"))
        {

            String localDBSize = FolderSizeCalculator.databaseSize(this);
            localDBSize_TV.setText(localDBSize);
        }

        otherSize_TV.setText("0.00");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void deleteFinish(String type) {
        sizes(type);
    }
}

