package com.alepapadop.recognitio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

// This is a custom Draw layout class, it works as a overlay layout on the
// camera preview layout and allows us to draw the bounding boxes
public class Draw extends View {

    int                     _color_index = 0;
    ArrayList<Recognition>  _list = new ArrayList<>();

    private static final int[] COLORS = {
            Color.BLUE,
            Color.RED,
            Color.GREEN,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA,
            Color.WHITE
    };

    public Draw(Context context) {
        super(context);
    }

    // this was a crazy issue, if this class was not provided the program crashes without a
    // good explanation. So always remember you need also this constructor.
    public Draw(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    // Returns a unique color for each bounding box
    private int DrawGetColors(boolean new_color) {
        int color = COLORS[_color_index];

        if (new_color) {
            ++_color_index;
        }

        if (_color_index >= COLORS.length) {
            _color_index = 0;
        }

        return color;
    }

    // Appends a Recognition to the array in order to draw it later
    public void DrawSetParams(Recognition rec) {
        _list.add(rec);
    }

    // Creates a basic Paint object and initializes some basic options
    // This object will be used for the bounding boxes
    private Paint CreateObjectPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStyle(Paint.Style.STROKE);

        paint.setColor(DrawGetColors(false));

        paint.setStrokeWidth(3);

        return paint;
    }

    // Creates a basic Paint object and initializes some basic options
    // This object will be used for the text info for each bounding box
    private Paint CreateInfoPaint() {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStyle(Paint.Style.FILL);

        paint.setColor(DrawGetColors(false));

        paint.setStrokeWidth(3);

        paint.setTextSize(paint.getTextSize() * 3);

        return paint;
    }

    // This function draws all the recognition data for each frame at once
    private void BatchDraw(Canvas canvas) {
        for (Recognition rec : _list) {
            _color_index = Integer.parseInt(rec.getId());
            Paint paint = CreateObjectPaint();
            Paint paint_label = CreateInfoPaint();

            RectF rect = rec.getLocation();

            canvas.drawRect(rect, paint);
            canvas.drawText(rec.toStringDraw(), rect.left, rect.top - 1, paint_label);
        }
    }

    // This function controls the draw process.
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (_list.size() > 0) {
            BatchDraw(canvas);
        }
        _list.clear();
    }

}
