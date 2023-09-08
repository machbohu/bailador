package com.example.bailador;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    ListView audioItemsListView;
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final List<String> listOfAudioFileNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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