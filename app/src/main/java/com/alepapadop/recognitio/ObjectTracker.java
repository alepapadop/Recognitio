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

    Draw    _draw;
    int     _width = 1;
    int     _height = 1;

    public ObjectTracker(Draw draw) {
        _draw = draw;
    }

    public void ObjectTrackerSetSize(int width, int height) {

        _width = width;
        _height = height;
    }

    private void ObjectTrackerFixLocations(Recognition rec) {

        RectF rectf = rec.getLocation();

        rectf.left = rec.getLocation().left * _height/300;
        rectf.top = rec.getLocation().top * _width/300;
        rectf.right = rec.getLocation().right * _height/300;
        rectf.bottom = rec.getLocation().bottom * _width/300;

        Log.d(RecognitioSetting.get_log_tag(), "rect size: left: " + rectf.left + " top: " + rectf.top + " right: " + rectf.right + " bottom:" + rectf.bottom);

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
