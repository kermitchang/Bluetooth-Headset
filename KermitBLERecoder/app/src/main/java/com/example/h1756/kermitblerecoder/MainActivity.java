package com.example.h1756.kermitblerecoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    // View
    private static Button mBtnBlue, mBtnRecode, mBtnTrack;
    private final String strBtnUseBT    = "Use Bluetooth";
    private final String strBtnNonUseBT = "Close Bluetooth";
    private final String strBtnRecode   = "Start Recode";
    private final String strBtnNonRecode   = "Stop Recode";
    private final String strBtnTrack    = "Start Track";
    private final String strBtnNonTrack    = "Stop Track";

    // Audio control
    private static AudioManager mAudioManager;
    private static ReceiverBluetoothHead mReceiverBluetoothHead;
    private static AudioTrack mAudioTrack;
    private static AudioRecord mAudioRecord;

    // Audio Format for Recorder
    private static final int RECORDER_SAMPLERATE = 16000; // 16000
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int iAlexaBufferSize = 5120; // 2 bytes in 16bit format
    private static final int iCyberonBufferSize = 5120; // 2 bytes in 16bit format
    private static int minBufferSizeRec;

    private static TrackThread mTrackThread;
    private static RecordThread mRecordThread;
    private static boolean bIsRecording;

    private static List<Byte> mListByte;
    private static byte[] bufferRecoder;


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

        bIsRecording = false;
        minBufferSizeRec = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);


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

            if(mBtnRecode.getText().toString().equals(strBtnRecode)){
                mBtnRecode.setText(strBtnNonRecode);
                mBtnBlue.setEnabled(false);
                mBtnTrack.setEnabled(false);
                StartRecode();
            }
            else{
                mBtnRecode.setText(strBtnRecode);
                mBtnBlue.setEnabled(true);
                mBtnTrack.setEnabled(true);
                StopRecode();
            }
        }
    };

    private Button.OnClickListener mBtnTrackList = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.i(TAG, "Tracking now by kermit");

            if(mBtnTrack.getText().toString().equals(strBtnTrack)){
                mBtnTrack.setText(strBtnNonTrack);
                mBtnBlue.setEnabled(false);
                mBtnRecode.setEnabled(false);
                StartTrack();
            }
            else{
                mBtnTrack.setText(strBtnTrack);
                mBtnBlue.setEnabled(true);
                mBtnRecode.setEnabled(true);
                StopTrack();
            }
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

    private void StartRecode(){
        Log.i(TAG, "Start Recording by kermit");
        bIsRecording = true;
        if( bufferRecoder != null ){
            bufferRecoder = null;
        }
        if(mListByte == null ){
            mListByte = new ArrayList<Byte>();
        }
        if( mAudioRecord == null  ){
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, iAlexaBufferSize);
            mAudioRecord.startRecording();
        }
        if( mRecordThread == null ) {
            try {
                mRecordThread = new RecordThread();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecordThread.start();
        }

    }

    private void StopRecode(){
        Log.i(TAG, "Stop Recording by kermit");
        bIsRecording = false;
        if( mRecordThread != null ) {
            mRecordThread.interrupt();
            mRecordThread = null;
        }
        if( mAudioRecord != null  ){
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
        if(mListByte != null ){
            Log.i(TAG, "mListByte Len:" + mListByte.size() + " by kermit");
            bufferRecoder = new byte [mListByte.size()];
            for(int i = 0; i < bufferRecoder.length; i++){
                bufferRecoder[i] = mListByte.get(i);
            }
            mListByte.clear();
            mListByte = null;
        }
    }

    private void StartTrack(){
        Log.i(TAG, "StartTrack by kermit");
        if( mAudioTrack == null ){
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO, RECORDER_AUDIO_ENCODING, iAlexaBufferSize, AudioTrack.MODE_STREAM);
            mAudioTrack.setPlaybackRate(RECORDER_SAMPLERATE);
            mAudioTrack.play();
        }

        if( mTrackThread == null ) {
            try {
                mTrackThread = new TrackThread();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mTrackThread.start();
        }
        else{
            mTrackThread.start();
        }

    }

    private void StopTrack(){
        Log.i(TAG, "StopTrack by kermit");
        if( mTrackThread != null ) {
            mTrackThread.interrupt();
            mTrackThread = null;
        }
        if( mAudioTrack != null ){
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    private class RecordThread extends Thread{

        private byte[] bufferRec;
        RecordThread() throws IOException {
            bufferRec = new byte[minBufferSizeRec/2];
        }

        @Override
        public void run(){
            Log.i(TAG,"in thread by kermit");
            while(bIsRecording){
                mAudioRecord.read(bufferRec, 0, (minBufferSizeRec/2));
                for (int i = 0; i < bufferRec.length; i++) {
                    mListByte.add(bufferRec[i]);
                }
                bufferRec = new byte[minBufferSizeRec/2];
            }
        }
    }

    private class TrackThread extends Thread{

        TrackThread() throws IOException {
        }

        @Override
        public void run(){
            Log.i(TAG,"in thread by kermit");
            mAudioTrack.write(bufferRecoder, 0, bufferRecoder.length);
        }
    }

}
