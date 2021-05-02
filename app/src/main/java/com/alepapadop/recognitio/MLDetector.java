package com.alepapadop.recognitio;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import java.util.ArrayList;

public class MLDetector {

    private final ObjectDetector  _ml_object_detector;
    private final LocalModel      _ml_local_model;

    public MLDetector() {

        _ml_local_model = new LocalModel.Builder()
                .setAssetFilePath(RecognitioSetting.get_model_name())
                .build();

        CustomObjectDetectorOptions objec_detector_options =
                new CustomObjectDetectorOptions.Builder(_ml_local_model)
                        .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                        .enableClassification()
                        .setClassificationConfidenceThreshold(RecognitioSetting.get_confidence_threshold())
                        .setMaxPerObjectLabelCount(1)
                        .enableMultipleObjects()
                        .build();

        _ml_object_detector =
                ObjectDetection.getClient(objec_detector_options);

    }


    public ArrayList<Recognition> MLDetectImage(ImageProxy image_proxy) {
        final ArrayList<Recognition> recognitions = new ArrayList<>();

        @SuppressLint("UnsafeExperimentalUsageError")
        InputImage input_image =
                InputImage.fromMediaImage(image_proxy.getImage(), image_proxy.getImageInfo().getRotationDegrees());

        _ml_object_detector
                .process(input_image)
                .addOnFailureListener(e -> {
                    Log.d(RecognitioSetting.get_log_tag(), e.toString());
                })
                .addOnSuccessListener(results -> {

                    for (DetectedObject detectedObject : results) {
                        Rect boundingBox = detectedObject.getBoundingBox();
                        Integer trackingId = detectedObject.getTrackingId();
                        for (DetectedObject.Label label : detectedObject.getLabels()) {
                            String text = label.getText();
                            int index = label.getIndex();
                            float confidence = label.getConfidence();
                        }

                        Recognition rec = new Recognition(trackingId.toString(),
                                detectedObject.getLabels().get(0).getText(), detectedObject.getLabels().get(0).getConfidence(), new RectF(boundingBox));

                        recognitions.add(rec);
                    }
                });

        return recognitions;
    }
}
