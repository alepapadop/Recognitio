package com.alepapadop.recognitio;

public class RecognitioSetting {

    private static int         _num_detections = 3;
    private static int         _num_threads = 4;
    private static String      _model_name = "lite-model_ssd_mobilenet_v1_1_metadata_2.tflite";
    private static float       _confidence_threshold = 0.5f;

    private static final String     _prefs_num_detections_key = "PrefsDetectionKey";
    private static final String     _prefs_num_threads_key = "PrefsThreadsKey";
    private static final String     _prefs_confidence_key = "PrefsConfidenceKey";

    private final static int         _pixel_bb_offset = 10;
    private final static boolean    _use_ml_kit = false;
    private final static String     _log_tag = "___EAPDetection___";


    public static String get_prefs_num_detections_key() {
        return _prefs_num_detections_key;
    }


    public static String get_prefs_num_threads_key() {
        return _prefs_num_threads_key;
    }


    public static String get_prefs_confidence_key() {
        return _prefs_confidence_key;
    }

    public static int get_num_detections() {
        return _num_detections;
    }

    public static void set_num_detections(int num_detections) {
        _num_detections = num_detections;
    }

    public static int get_num_threads() {
        return _num_threads;
    }

    public static void set_num_threads(int num_threads) {
        _num_threads = num_threads;
    }

    public static String get_model_name() {
        return _model_name;
    }

    public static float get_confidence_threshold() { return _confidence_threshold;}

    public static void set_confidence_threshold(float confidence_threshold) {
        _confidence_threshold = confidence_threshold;
    }

    public static boolean get_use_ml_kit() {
        return _use_ml_kit;
    }

    public static String get_log_tag() {
        return _log_tag;
    }

    public static int get_pixel_bb_offset() {
        return _pixel_bb_offset;
    }
}
