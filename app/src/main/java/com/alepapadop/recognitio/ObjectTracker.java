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

    public ObjectTracker(Draw draw) {
        _draw = draw;
    }

    private void ObjectTrackerFixLocations(Recognition rec) {

        RectF rectf = rec.getLocation();

        rectf.left = rec.getLocation().left;
        rectf.top = rec.getLocation().top;
        rectf.right = rec.getLocation().right;
        rectf.bottom = rec.getLocation().bottom;

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
