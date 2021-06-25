package com.alepapadop.recognitio;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Size;

import androidx.camera.core.ImageProxy;
import java.io.IOException;
import java.util.ArrayList;

public class Detector {

    // a variable for deciding for the detection API. MLKit was tested but was not working as
    // expected the _use_mlk_kit should always be zero
    private final boolean       _use_ml_kit;
    // The detector fot the MLKit API
    private MLDetector          _ml_detector;
    // The TensorFlow Task Library detector object
    private TFDetector          _tf_detector;
    // The application context
    private final Context       _context;

    // detector initialization function
    public Detector(Activity activity, Context context, ObjectTracker obj_tracker) {

        // get the global settings for the app
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

    // Returns the object detection results in an array list of custom Recognition objects
    public ArrayList<Recognition> DetectImage(ImageProxy image_proxy) {

        ArrayList detection_results;

        if (_use_ml_kit) {
            detection_results = _ml_detector.MLDetectImage(image_proxy);
        } else {
            detection_results = _tf_detector.TFDetectImage(_context, image_proxy);
        }

        return detection_results;
    }

    // Returns the detector CNN network input image size
    public Size DetectorImageInputSize() {
        Size size = null;

        if (_use_ml_kit) {
            size = new Size(0,0);
            assert false;
        } else {
            size = _tf_detector.TFDetectorImageInputSize();
        }

        return size;
    }
}
