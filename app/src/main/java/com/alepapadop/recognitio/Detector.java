package com.alepapadop.recognitio;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.camera.core.ImageProxy;
import java.io.IOException;
import java.util.ArrayList;

public class Detector {

    private final boolean       _use_ml_kit;
    private MLDetector          _ml_detector;
    private TFDetector          _tf_detector;
    private final Context       _context;


    public Detector(Activity activity, Context context, ObjectTracker obj_tracker) {

        _use_ml_kit = RecognitioSetting.get_use_ml_kit();
        _context = context;

        if (_use_ml_kit) {
            _ml_detector = new MLDetector();
        } else {

            try {
                _tf_detector = new TFDetector(activity);
            } catch (IOException e) {
                Log.d(RecognitioSetting.get_log_tag(), e.toString());
            }
        }
    }

    public ArrayList<Recognition> DetectImage(ImageProxy image_proxy) {

        ArrayList detection_results;

        if (_use_ml_kit) {
            detection_results = _ml_detector.MLDetectImage(image_proxy);
        } else {
            detection_results = _tf_detector.TFDetectImage(_context, image_proxy);
        }

        return detection_results;
    }

}
