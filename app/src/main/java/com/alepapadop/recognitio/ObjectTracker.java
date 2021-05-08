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

import org.tensorflow.lite.task.vision.detector.Detection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

public class ObjectTracker {

    private Draw                _draw;

    private int                 _view_height = 1;
    private int                 _view_width = 1;

    private int                 _detector_height = 1;
    private int                 _detector_width = 1;

    private final int           _pixel_offset = RecognitioSetting.get_pixel_bb_offset();

    private List<Recognition>   _prev_recognitions = null;

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

    private boolean ObjectTrackerCompareToLock(Recognition cur_rec, Recognition prev_rec) {
        boolean ret_val = false;
        float cur_rec_x_center = cur_rec.get_object_x_center();
        float cur_rec_y_center = cur_rec.get_object_y_center();
        float cur_rec_x_size = cur_rec.get_object_x_size();
        float cur_rec_y_size = cur_rec.get_object_y_size();
        String cur_label = cur_rec.getLabel();

        float prev_rec_x_center = prev_rec.get_object_x_center();
        float prev_rec_y_center = prev_rec.get_object_y_center();
        float prev_rec_x_size = prev_rec.get_object_x_size();
        float prev_rec_y_size = prev_rec.get_object_y_size();
        String prev_label = prev_rec.getLabel();


        double centers_x_pow = pow(cur_rec_x_center - prev_rec_x_center, 2);
        double centers_y_pow = pow(cur_rec_y_center - prev_rec_y_center, 2);
        double distance = sqrt(centers_x_pow + centers_y_pow);

        float max_x_size = min(cur_rec_x_size, prev_rec_x_size);
        float max_y_size = min(cur_rec_y_size, prev_rec_y_size);

        double x_dist = sqrt(centers_x_pow);
        double y_dist = sqrt(centers_y_pow);

        double x_dist_perc = x_dist / distance;
        double y_dist_perc = y_dist / distance;

        double comp_x = max_x_size * x_dist_perc;
        double comp_y = max_y_size * y_dist_perc;

        Log.d(RecognitioSetting.get_log_tag(), "id: " + cur_rec.getId() + " prev_id: " + prev_rec.getId());
        Log.d(RecognitioSetting.get_log_tag(), "label: " + cur_label + " prev_label: " + prev_label);
        Log.d(RecognitioSetting.get_log_tag(), "size_x: " + cur_rec_x_size + " prev_size_x: " + prev_rec_x_size);
        Log.d(RecognitioSetting.get_log_tag(), "size_y: " + cur_rec_y_size + " prev_size_y: " + prev_rec_y_size);
        Log.d(RecognitioSetting.get_log_tag(), "distance: " + distance + " max_x_size: " + max_x_size + " max_y_size: " + max_y_size);
        Log.d(RecognitioSetting.get_log_tag(), "x_dist: " + x_dist + " y_dist: " + y_dist + " comp_x: " + comp_x + " comp_y: " + comp_y);

        if (x_dist < comp_x && y_dist < comp_y) {
            if (cur_label.equals(prev_label)) {
                ret_val = true;
            }
        }


        return ret_val;
    }

    private void ObjectTrackerTryToLockObject(List<Recognition> current_recognitions) {

        HashMap<String, Recognition> ids_rec_map = new HashMap<>();
        HashMap<String, String> ids_map = new HashMap<>();


        for (Recognition cur_rec : current_recognitions) {

            ids_rec_map.put(cur_rec.getId(), cur_rec);
        }

        Log.d(RecognitioSetting.get_log_tag(), "----");
        if (_prev_recognitions != null) {

            for (Recognition cur_rec : current_recognitions) {
                for (Recognition prev_rec : _prev_recognitions) {
                    if (ObjectTrackerCompareToLock(cur_rec, prev_rec)) {

                        ids_map.put(cur_rec.getId(), prev_rec.getId());

                        Log.d(RecognitioSetting.get_log_tag(), cur_rec.debugRecognition());
                        Log.d(RecognitioSetting.get_log_tag(), prev_rec.debugRecognition());
                    }
                }
            }

            if (!ids_map.isEmpty()) {
                for (HashMap.Entry<String, String> entry : ids_map.entrySet()) {
                    String old_id = entry.getValue();
                    String new_id = entry.getKey();

                    Recognition old_id_rec = ids_rec_map.get(old_id);
                    Recognition new_id_rec = ids_rec_map.get(new_id);

                    if (old_id_rec != null && new_id_rec != null) {

                        new_id_rec.setId(old_id);
                        old_id_rec.setId(new_id);
                    }
                }
            }
        }


        if (current_recognitions != null) {
            _prev_recognitions = current_recognitions;
        }

    }

    public void ObjectTrackerDraw(List<Recognition> recognitions) {

        // for testing the lock
        Collections.reverse(recognitions);

        ObjectTrackerTryToLockObject(recognitions);

        for (Recognition rec : recognitions) {
                ObjectTrackerFixLocations(rec);
                _draw.DrawSetParams(rec);
        }
        if (recognitions.size() > 0) {
            _draw.invalidate();
        }
    }
}
