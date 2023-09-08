package com.machbohu.bailador;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    ListView audioItemsListView;
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final List<String> listOfAudioFileNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        setContentView(R.layout.audio_list_layout);

        audioItemsListView = findViewById(R.id.audioList);

        String[] audio_files;
        try {
            audio_files = getAssets().list("audio_files");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < audio_files.length; i++) {
            listOfAudioFileNames.add(audio_files[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, listOfAudioFileNames);

        adapter.notifyDataSetChanged();
        audioItemsListView.setAdapter(adapter);
        audioItemsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        audioItemsListView.setCacheColorHint(Color.TRANSPARENT);

        audioItemsListView.setOnItemClickListener((parent, view, position, id) -> {
            AssetFileDescriptor assetFileDescriptior;

            try {
                assetFileDescriptior = getAssets().openFd("audio_files/" + listOfAudioFileNames.get(position));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            playSong(assetFileDescriptior);
        });

        audioItemsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            InputStream audioInputStream;

            try {
                audioInputStream = getAssets().open("audio_files/" + listOfAudioFileNames.get(position));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String downloadOrder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

            File tempFile = new File(downloadOrder+"/"+"bailador-je-tanecnik.mp3");
            tempFile.deleteOnExit();
            FileOutputStream out;

            try {
                out = new FileOutputStream(tempFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            try {
                IOUtils.copy(audioInputStream, out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Uri audioFileUri = Uri.fromFile(tempFile);

            Intent sendIntent = new Intent();
            sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, audioFileUri);
            sendIntent.setType("audio/mp3");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);

            return true;
        });
    }

    private void playSong(AssetFileDescriptor assetFileDescriptor) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.v(getString(R.string.app_name), e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}