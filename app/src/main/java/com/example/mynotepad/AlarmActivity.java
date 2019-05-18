package com.example.mynotepad;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AlarmActivity extends AppCompatActivity {

    public static String ITEM_CONTENT;
    MediaPlayer alarmMusic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmMusic=MediaPlayer.create(AlarmActivity.this,R.raw.alarm);
        alarmMusic.setLooping(true);
        alarmMusic.start();
        new AlertDialog.Builder(AlarmActivity.this)
                .setTitle("闹钟").
                setMessage("闹钟响了").
                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarmMusic.stop();
                        AlarmActivity.this.finish();
                    }
                }).show();
    }
}

