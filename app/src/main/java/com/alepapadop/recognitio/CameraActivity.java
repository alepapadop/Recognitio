package com.alepapadop.recognitio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.SystemClock;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.support.model.Model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    private PreviewView                             _previewView;
    private ListenableFuture<ProcessCameraProvider> _cameraProviderFuture;
    private TFClassifier                            _tf_classifier;
    private Activity                                _activity;
    private ObjectTracker                           _obj_tracker;
    private Draw                                    _draw;

    private String TAG = "CameraX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        _activity = this;
        _draw = findViewById(R.id.draw);
        _previewView = findViewById(R.id.previewView);

        _obj_tracker = new ObjectTracker(_draw);

        _cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        _cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = _cameraProviderFuture.get();
                    bindImageAnalysis(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void analyze(@NonNull ImageProxy image) {

                YuvToRgbConverter y2b = new YuvToRgbConverter(getApplicationContext());
                Image img = image.getImage();
                Bitmap bitmap = Bitmap.createBitmap(
                        img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
                y2b.yuvToRgb(image.getImage(), bitmap);

                if (_tf_classifier == null) {
                    try {
                        _tf_classifier = TFClassifier.create(_activity, Model.Device.CPU, 2);
                    } catch (IOException e) {
                        Log.d(TAG, "no classifier");
                    }
                }

                if (bitmap != null) {
                    final List<Recognition> results =
                            _tf_classifier.TFRecognizeImage2(bitmap, image.getImageInfo().getRotationDegrees());
                    Log.d(TAG, results.toString());

                    _obj_tracker.ObjectTrackerDraw(results);
                } else {
                    Log.d(TAG, "No bitmap");
                }



/*
                new Runnable() {
                    @Override
                    public void run() {
                        if (_tf_classifier != null) {
                            final long startTime = SystemClock.uptimeMillis();
                            final List<Recognition> results =
                                    _tf_classifier.TFRecognizeImage(bitmap, image.getImageInfo().getRotationDegrees());

                            Log.d(TAG, "Analysis");
                            //lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                            //LOGGER.v("Detect: %s", results);

                            //runOnUiThread(
                            //        new Runnable() {
                            //            @Override
                            //            public void run() {
                            //                showResultsInBottomSheet(results);
                            //                showFrameInfo(previewWidth + "x" + previewHeight);
                            //                showCropInfo(imageSizeX + "x" + imageSizeY);
                            //                showCameraResolution(imageSizeX + "x" + imageSizeY);
                            //                showRotationInfo(String.valueOf(sensorOrientation));
                            //                showInference(lastProcessingTimeMs + "ms");
                            //            }
                            //        });
                        }
                        //readyForNextImage();
                    }
                };

 */

                image.close();

            }
        });

        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        preview.setSurfaceProvider(_previewView.getSurfaceProvider());

        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector,
                imageAnalysis, preview);
    }
}