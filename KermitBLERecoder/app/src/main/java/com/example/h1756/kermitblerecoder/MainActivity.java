package com.example.h1756.kermitblerecoder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private static Button mBtnBlue, mBtnRecode, mBtnTrack;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnBlue = (Button)findViewById(R.id.idBtnBlue);
        mBtnRecode = (Button)findViewById(R.id.idBtnRecord);
        mBtnTrack = (Button)findViewById(R.id.idBtnTrack);


        mBtnBlue.setOnClickListener(mBtnBlueToothList);
        mBtnRecode.setOnClickListener(mBtnRecodeList);
        mBtnTrack.setOnClickListener(mBtnTrackList);
    }


    

    private Button.OnClickListener mBtnBlueToothList = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.i(TAG, "Using bluetooth mic now by kermit");
        }
    };

    private Button.OnClickListener mBtnRecodeList = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.i(TAG, "Recoding rnow by kermit");
        }
    };

    private Button.OnClickListener mBtnTrackList = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.i(TAG, "Tracking now by kermit");
        }
    };


}
