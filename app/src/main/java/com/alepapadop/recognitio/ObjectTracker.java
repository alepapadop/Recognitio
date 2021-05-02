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

public class ObjectTracker {

    private Draw    _draw;
    private int     _width = 1;
    private int     _height = 1;

    private int     _view_height = 1;
    private int     _view_width = 1;

    private int     _detecctor_height = 1;
    private int     _detector_width = 1;


    public ObjectTracker(Draw draw) {
        _draw = draw;
    }

    public void ObjectTrackerSetSize(int width, int height) {

        _width = width;
        _height = height;

    }

    public void ObjectTrackerSetViewSize(int width, int height) {

        _view_width = width;
        _view_height = height;

    }

    private void ObjectTrackerFixLocations(Recognition rec) {

        RectF rectf = rec.getLocation();

        Log.d(RecognitioSetting.get_log_tag(), "width: " + _width + " height: " + _height);
        Log.d(RecognitioSetting.get_log_tag(), "width: " + _view_width + " height: " + _view_height);

        Log.d(RecognitioSetting.get_log_tag(), "before rect size: left: " + rectf.left + " top: " + rectf.top + " right: " + rectf.right + " bottom:" + rectf.bottom);

        rectf.left = rec.getLocation().left * (_width/300) * (_view_width/_width);
        rectf.top = rec.getLocation().top * (_height/300) * (_view_height/_height);
        rectf.right = rec.getLocation().right * (_width/300) * (_view_width/_width);
        rectf.bottom = rec.getLocation().bottom * (_height/300) * (_view_height/_height);

        Log.d(RecognitioSetting.get_log_tag(), "after rect size: left: " + rectf.left + " top: " + rectf.top + " right: " + rectf.right + " bottom:" + rectf.bottom);

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
