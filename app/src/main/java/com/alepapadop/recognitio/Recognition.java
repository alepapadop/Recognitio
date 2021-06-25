package com.alepapadop.recognitio;

import android.graphics.RectF;

// This is a simple class for storing data of the recognized objects
public class Recognition {

    // id of the detected object
    private String _id;

    // The label of the detected object
    private final String _label;

    // the confidence score of the detected object
    private final Float _confidence;

    // the location of the detected object. The locations are according to the input image of the
    // detections algorithm.
    private RectF _location;

    // the size of the bounding box in the x and the y axis
    private float  _object_x_size;
    private float  _object_y_size;

    private float   _object_x_center;
    private float   _object_y_center;

    private void CalcRecognitionLocationMagnitudes() {
        _object_x_size = _location.right - _location.left;
        _object_y_size = _location.bottom - _location.top;

        _object_x_center = _location.left +_object_x_size / 2;
        _object_y_center = _location.bottom +_object_y_size / 2;
    }

    public Recognition(final String id, final String title, final Float confidence, final RectF location) {

        _id = id;
        _label = title;
        _confidence = confidence;
        _location = location;

        CalcRecognitionLocationMagnitudes();

    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public String getLabel() {
        return _label;
    }

    public Float getConfidence() {
        return _confidence;
    }

    public RectF getLocation() {
        return new RectF(_location);
    }

    public void setLocation(RectF location) {
        _location = location;
    }

    public float get_object_x_size() {
        return _object_x_size;
    }

    public float get_object_y_size() {
        return _object_y_size;
    }

    public float get_object_x_center() {
        return _object_x_center;
    }

    public float get_object_y_center() {
        return _object_y_center;
    }

    public String debugRecognition() {
        String resultString = "";

        if (_id != null) {
            resultString += "[" + _id + "] ";
        }

        if (_label != null) {
            resultString += _label + " ";
        }

        if (_confidence != null) {
            resultString += String.format("(%.1f%%) ", _confidence * 100.0f);
        }

        if (_location != null) {
            resultString += _location + " ";
        }

        if (_location != null) {
            resultString += "(" + _object_x_size / 2 + ", " + _object_y_size / 2 + ")";
        }

        return resultString.trim();
    }

    public String toStringDraw() {
        String resultString = "";

        if (_id != null) {
            resultString += "[" + _id + "] ";
        }


        if (_label != null) {
            resultString += _label + " ";
        }

        if (_confidence != null) {
            resultString += String.format("(%.1f%%) ", _confidence * 100.0f);
        }

        return resultString.trim();
    }
}

