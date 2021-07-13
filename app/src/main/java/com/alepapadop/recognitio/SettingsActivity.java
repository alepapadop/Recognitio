package com.alepapadop.recognitio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.Slider;

// This class is the setting user interface whtich appears when the user preses the
// floating button
public class SettingsActivity extends AppCompatActivity {


    private SeekBar     _seek_num_threads;
    private TextView    _txt_num_threads;
    private TextView    _title_num_threads;

    private SeekBar     _seek_num_detect;
    private TextView    _txt_num_detect;
    private TextView    _title_num_detect;

    private SeekBar     _seek_num_confidence;
    private TextView    _txt_num_confidence;
    private TextView    _title_num_confidence;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        LinearLayout ll;

        ll = (LinearLayout) findViewById(R.id.slider_num_threads);

        _title_num_threads = (TextView) ll.findViewById(R.id.seekbar_name);
        _txt_num_threads = (TextView) ll.findViewById(R.id.seekbar_value);
        _seek_num_threads = (SeekBar) ll.findViewById(R.id.seekbar);

        _title_num_threads.setText("Threads");
        initialize_seekbar(_seek_num_threads, _txt_num_threads, Runtime.getRuntime().availableProcessors(), 1, RecognitioSetting.get_num_threads());

        ll = (LinearLayout) findViewById(R.id.slider_num_detect);

        _title_num_detect = (TextView) ll.findViewById(R.id.seekbar_name);
        _txt_num_detect = (TextView) ll.findViewById(R.id.seekbar_value);
        _seek_num_detect = (SeekBar) ll.findViewById(R.id.seekbar);

        _title_num_detect.setText("Detections");
        initialize_seekbar(_seek_num_detect, _txt_num_detect, 5, 1, RecognitioSetting.get_num_detections());


        ll = (LinearLayout) findViewById(R.id.slider_confidence);

        _title_num_confidence = (TextView) ll.findViewById(R.id.seekbar_name);
        _txt_num_confidence = (TextView) ll.findViewById(R.id.seekbar_value);
        _seek_num_confidence = (SeekBar) ll.findViewById(R.id.seekbar);

        _title_num_confidence.setText("Confidence");
        initialize_seekbar(_seek_num_confidence, _txt_num_confidence, 100, 0, (int)(RecognitioSetting.get_confidence_threshold() * 100));

    }

    @Override
    public void onStop () {
        super.onStop();

        RecognitioSetting.set_confidence_threshold(_seek_num_confidence.getProgress()/100f);
        RecognitioSetting.set_num_detections(_seek_num_detect.getProgress());
        RecognitioSetting.set_num_threads(_seek_num_threads.getProgress());

    }

/*
    private static final int PICK_PDF_FILE = 2;

    public void PickFile(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, PICK_PDF_FILE);

    }
*/

    private void initialize_seekbar(SeekBar seek, TextView text, int max, int min, int current) {

        seek.setMin(min);
        seek.setMax(max);
        seek.setProgress(current);
        seek.showContextMenu();
        text.setText(String.valueOf(current));


        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                text.setText(String.valueOf(i));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}