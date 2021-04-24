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

    public Draw(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

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

    public void DrawSetParams(Recognition rec) {
        _list.add(rec);
    }

    private Paint CreateObjectPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStyle(Paint.Style.STROKE);

        paint.setColor(DrawGetColors(true));

        paint.setStrokeWidth(3);

        return paint;
    }

    private Paint CreateInfoPaint() {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStyle(Paint.Style.FILL);

        paint.setColor(DrawGetColors(false));

        paint.setStrokeWidth(3);

        paint.setTextSize(paint.getTextSize() * 3);

        return paint;
    }

    private void BatchDraw(Canvas canvas) {
        for (Recognition rec : _list) {
            Paint paint = CreateObjectPaint();
            Paint paint_label = CreateInfoPaint();

            RectF rect = rec.getLocation();


            canvas.drawRect(rect, paint);
            canvas.drawText(rec.toStringDraw(), rect.left, rect.top - 1, paint_label);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (_list.size() > 0) {
            BatchDraw(canvas);
        }
        _list.clear();
    }

}
