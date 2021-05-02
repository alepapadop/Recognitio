package com.alepapadop.recognitio;

public class RecognitioSetting {

    private final static int         _num_detections = 5;
    private final static int         _num_threads = 4;
    private final static String      _model_name = "ssd_mobilenet_v1_1_metadata_1.tflite";
    private final static float       _confidence_threshold = 0.5f;

    private final static boolean    _use_ml_kit = false;

    private final static String     _log_tag = "___EAPDetection___";

    public static int get_num_detections() {
        return _num_detections;
    }

    public static int get_num_threads() {
        return _num_threads;
    }

    public static String get_model_name() {
        return _model_name;
    }

    public static float get_confidence_threshold() { return _confidence_threshold;}

    public static boolean get_use_ml_kit() {
        return _use_ml_kit;
    }

    public static String get_log_tag() {
        return _log_tag;
    }
}
