package com.example.h1756.kermitblerecoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    // View
    private static Button mBtnBlue, mBtnRecode, mBtnTrack;
    private final String strBtnUseBT    = "Use Bluetooth";
    private final String strBtnNonUseBT = "Close Bluetooth";
    private final String strBtnRecode   = "Recode";
    private final String strBtnTrack    = "Track";


    // Audio control
    private static AudioManager mAudioManager;
    private static ReceiverBluetoothHead mReceiverBluetoothHead;
    private static AudioTrack mAudioTrack;
    private static AudioRecord mAudioRecord;

    // Audio Format
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    private static final int BytesPerElement = 2; // 2 bytes in 16bit format
    private static final int iAlexaBufferSize = 5120; // 2 bytes in 16bit format



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setBluetoothScoOn(true);
        IntentFilter mBluetoothReceiverIntent = new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        mReceiverBluetoothHead = new ReceiverBluetoothHead();
        registerReceiver(mReceiverBluetoothHead, mBluetoothReceiverIntent);


        // View Setting
        mBtnBlue = (Button)findViewById(R.id.idBtnBlue);
        mBtnRecode = (Button)findViewById(R.id.idBtnRecord);
        mBtnTrack = (Button)findViewById(R.id.idBtnTrack);

        mBtnBlue.setText(strBtnUseBT);
        mBtnRecode.setText(strBtnRecode);
        mBtnTrack.setText(strBtnTrack);

        mBtnBlue.setOnClickListener(mBtnBlueToothList);
        mBtnRecode.setOnClickListener(mBtnRecodeList);
        mBtnTrack.setOnClickListener(mBtnTrackList);



    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "onDestroy by kermit");
        unregisterReceiver(mReceiverBluetoothHead);
    }

    private Button.OnClickListener mBtnBlueToothList = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.i(TAG, "Using bluetooth mic now by kermit");
            if(mBtnBlue.getText().toString().equals(strBtnUseBT)){
                mBtnBlue.setText(strBtnNonUseBT);
                mAudioManager.startBluetoothSco();
            }
            else{
                mBtnBlue.setText(strBtnUseBT);
                mAudioManager.stopBluetoothSco();
            }
        }
    };

    private Button.OnClickListener mBtnRecodeList = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.i(TAG, "Recoding now by kermit");
        }
    };

    private Button.OnClickListener mBtnTrackList = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.i(TAG, "Tracking now by kermit");
        }
    };

    private class ReceiverBluetoothHead extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int iState = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            if (AudioManager.SCO_AUDIO_STATE_DISCONNECTED == iState)    Log.i(TAG, "onReceive Bluetooth is disconnect by kermit");
            if (AudioManager.SCO_AUDIO_STATE_CONNECTED == iState)       Log.i(TAG, "onReceive Bluetooth is connected by kermit");
            if (AudioManager.SCO_AUDIO_STATE_CONNECTING == iState)      Log.i(TAG, "onReceive Bluetooth is connecting by kermit");
            if (AudioManager.SCO_AUDIO_STATE_ERROR == iState)            Log.i(TAG, "onReceive Bluetooth is error by kermit");
        }
    }

}
