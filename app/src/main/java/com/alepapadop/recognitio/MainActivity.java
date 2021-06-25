package com.alepapadop.recognitio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    private static final String[] WRITE_PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String[] READ_PERMISSION = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String[] MANAGE_PERMISSION = new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE};
    private static final String[] ACCESS_PERMISSION = new String[]{Manifest.permission.ACCESS_MEDIA_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // when the application starts we read the shared prederences, we store there some
        // user options for the detection like a threshold value the num of threads etc
        read_shared_preferences();

        // without a camera permission the application can not work
        // some other permissions are also requested by the app. Those permissions are needed
        // for debug purposes.
        if (!hasCameraPermission()) {
            requestPermission();
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        // on resume the user should press again to restart the camera
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText("Press to start the camera");
    }

    public void textViewClick(View v) {
        // pressing on the TextView the camera will start
        enableCamera();
    }

    public void settingsClick(View v) {
        // pressing on the floating button the user options menu will appear
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private boolean hasCameraPermission() {
        // checking for the camera permission
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        // requesting the permission from the user. Only the camera permission is needed
        // the rest of the permissions is used by debug code. I was saving on the device
        // some frames of the camera in order to check the rotations of the sensor and the
        // transformations. Also i was checking the convertion form the YUV to the bitmap
        // format.
        String[] perm = new String[] {CAMERA_PERMISSION[0], WRITE_PERMISSION[0], READ_PERMISSION[0], MANAGE_PERMISSION[0], ACCESS_PERMISSION[0]};

        ActivityCompat.requestPermissions(
                this,
                //CAMERA_PERMISSION,
                perm,
                CAMERA_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult (int requestCode,
                                           String[] permissions,
                                           int[] grantResults){
        // Callback of the user action after the permission request.
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(RecognitioSetting.get_log_tag(), "permission was granted");
                    enableCamera();
                } else {
                    Log.d(RecognitioSetting.get_log_tag(), "permission was denied");

                    TextView tv = (TextView) findViewById(R.id.textView);
                    tv.setText(R.string.camera_permission);
                }
            }
        }
    }

    private void enableCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStop () {
        super.onStop();

        // when the user closes the application the preferences are written in the shared
        // preferences. So after a restart of the app the user gets the same options
        write_shared_preferences();
    }

    private void write_shared_preferences() {
        // writing the shared preference variables
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(RecognitioSetting.get_prefs_num_threads_key(), RecognitioSetting.get_num_threads());
        editor.putInt(RecognitioSetting.get_prefs_num_detections_key(), RecognitioSetting.get_num_detections());
        editor.putFloat(RecognitioSetting.get_prefs_confidence_key(), RecognitioSetting.get_confidence_threshold());
        editor.apply();

        debug_key_values("Write");
    }

    // reading the shared preference variables
    private void read_shared_preferences() {

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        int num_threads = sharedPref.getInt(RecognitioSetting.get_prefs_num_threads_key(), RecognitioSetting.get_num_threads());
        RecognitioSetting.set_num_threads(num_threads);

        int num_detect = sharedPref.getInt(RecognitioSetting.get_prefs_num_detections_key(), RecognitioSetting.get_num_detections());
        RecognitioSetting.set_num_detections(num_detect);

        float confidence = sharedPref.getFloat(RecognitioSetting.get_prefs_confidence_key(), RecognitioSetting.get_confidence_threshold());
        RecognitioSetting.set_confidence_threshold(confidence);

        debug_key_values("Read");

    }

    private void debug_key_values(String name) {
        // debug function for checking the shared preferences
        Log.d(RecognitioSetting.get_log_tag(), name);
        Log.d(RecognitioSetting.get_log_tag(), RecognitioSetting.get_prefs_num_threads_key() + " : " + RecognitioSetting.get_num_threads());
        Log.d(RecognitioSetting.get_log_tag(), RecognitioSetting.get_prefs_num_detections_key() + " : " + RecognitioSetting.get_num_detections());
        Log.d(RecognitioSetting.get_log_tag(), RecognitioSetting.get_prefs_confidence_key() + " : " + RecognitioSetting.get_confidence_threshold());
    }
}