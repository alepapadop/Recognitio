package com.alepapadop.recognitio;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.ceil;
import static java.lang.Math.round;

public class ObjectTracker {

    private Draw        _draw;

    private int         _view_height = 1;
    private int         _view_width = 1;

    private int         _detector_height = 1;
    private int         _detector_width = 1;

    private final int   _pixel_offset = 10;

    public ObjectTracker(Draw draw) {
        _draw = draw;
    }

    public void ObjectTrackerSetViewSize(int width, int height) {

        _view_width = width;
        _view_height = height;
    }

    public void ObjectTrackerSetDetectorSize(int width, int height) {
        _detector_width = width;
        _detector_height = height;
    }

    private void ObjectTrackerFixLocations(Recognition rec) {

        RectF rectf = rec.getLocation();

        rectf.left = ((rec.getLocation().left * _view_width) / _detector_width) - _pixel_offset;
        rectf.top = ((rec.getLocation().top * _view_height) / _detector_height) + _pixel_offset;
        rectf.right = ((rec.getLocation().right * _view_width) / _detector_width) + _pixel_offset;
        rectf.bottom = ((rec.getLocation().bottom * _view_height) / _detector_height) - _pixel_offset;

        rec.setLocation(rectf);
    }

    public void ObjectTrackerDraw(List<Recognition> recognitions) {

        for (Recognition rec : recognitions) {
                ObjectTrackerFixLocations(rec);
                _draw.DrawSetParams(rec);
        }
        if (recognitions.size() > 0) {
            _draw.invalidate();
        }
    }
}
