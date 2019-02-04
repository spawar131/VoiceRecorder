package com.example.javavoicerecorder;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;


public class MainActivity extends AppCompatActivity {
    private MediaRecorder audioRecorder;
    private String outputFile, convertedFile;
    private MediaPlayer mediaPlayer;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                // Great!
            }
            @Override
            public void onFailure(Exception error) {
                // FFmpeg is not supported by device
                error.printStackTrace();
            }
        });
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
        convertedFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.mp3";

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final ToggleButton recordPauseButton = findViewById(R.id.toggleButton);
                recordPauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (recordPauseButton.isChecked()) {
                           if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                               ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                                       0);
                           } else {
                               audioRecorder = new MediaRecorder();
                               audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                               audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                               audioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                               audioRecorder.setOutputFile(outputFile);
                                try {
                                    audioRecorder.prepare();
                                    audioRecorder.start();
                                } catch (IllegalStateException ise) {
                                    // make something ...
                                } catch (IOException ioe) {
                                    // make something
                                }
                            }
                            //Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();

                        } else {
                            audioRecorder.stop();
                            audioRecorder.release();
                            audioRecorder = null;
                            Snackbar.make(view, "Paused", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                });

        final ToggleButton playPauseButton = findViewById(R.id.playButton);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recordPauseButton.isChecked()) {
                   return;
                } else {
                    File flacFile = new File(Environment.getExternalStorageDirectory(), "recording.3gp");
                    IConvertCallback callback = new IConvertCallback() {
                        @Override
                        public void onSuccess(File convertedFile) {
                            // So fast? Love it!
                            String fileName = convertedFile.getName();
                            fileName = convertedFile.getName();

                            if(mediaPlayer == null) {
                                //set up MediaPlayer
                                mediaPlayer = new MediaPlayer();
                            }
                            if (playPauseButton.isChecked()) {
                                try {
                                    mediaPlayer.setDataSource(convertedFile.getAbsolutePath());
                                    mediaPlayer.prepare();
                                    mediaPlayer.setVolume(40.0f,40.0f);
                                    mediaPlayer.start();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                mediaPlayer.stop();
                                mediaPlayer.reset();
                                //mediaPlayer = null;
                            }
                        }
                        @Override
                        public void onFailure(Exception error) {
                            // Oops! Something went wrong
                            String fileName = error.getMessage();
                            fileName = error.getMessage();
                        }
                    };
                    AndroidAudioConverter.with(MainActivity.this)
                            // Your current audio file
                            .setFile(flacFile)

                            // Your desired audio format
                            .setFormat(AudioFormat.MP3)

                            // An callback to know when conversion is finished
                            .setCallback(callback)

                            // Start conversion
                            .convert();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
