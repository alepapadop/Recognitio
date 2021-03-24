package com.alepapadop.recognitio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private String TAG = "CameraX";
    private int ACTIVITY_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (hasCameraPermission()) {
            enableCamera();
        } else {
            requestPermission();
        }
        Log.d(TAG, "on_create");

    }

    @Override
    public void onResume(){
        super.onResume();

        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText("Press to restart the camera");
        Log.d(TAG, "Resume");
    }

    public void textViewClick(View v) {
        enableCamera();
        Log.d(TAG, "click");
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult (int requestCode,
                                           String[] permissions,
                                           int[] grantResults){
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "permission was granted");
                    enableCamera();
                } else {
                    Log.d(TAG, "permission was denied");

                    TextView tv = (TextView)findViewById(R.id.textView);
                    tv.setText(R.string.camera_permission);
                }
            }
        }
    }

    private void enableCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }


}