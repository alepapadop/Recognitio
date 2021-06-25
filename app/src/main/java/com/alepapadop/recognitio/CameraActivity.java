package com.alepapadop.recognitio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
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
import android.graphics.Rect;
import android.graphics.RectF;
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
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;

import org.tensorflow.lite.support.model.Model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    // camera preview layout
    private PreviewView                             _previewView;
    // the android building camera provider
    private ListenableFuture<ProcessCameraProvider> _cameraProviderFuture;
    // custom object tracker for controlling the bounding box drawing process
    private ObjectTracker                           _obj_tracker;
    // custom draw layout for drawing the bounding boxes
    private Draw                                    _draw;
    // custon detetor object for which controls the MLKit and Task Library detection process
    private Detector                                _detector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        _draw = findViewById(R.id.draw);
        _previewView = findViewById(R.id.previewView);

        _obj_tracker = new ObjectTracker(_draw);
        _detector = new Detector(this, getApplicationContext(), _obj_tracker);
        Size size = _detector.DetectorImageInputSize();
        // the object tracker stores the cnn detector input image size in pixels
        _obj_tracker.ObjectTrackerSetDetectorSize(size.getWidth(), size.getWidth());

        _cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // try to open the camera on the device
        _cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = _cameraProviderFuture.get();

                    // run the camera preview image analysis
                    bindImageAnalysis(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));

        // keep the screen on while the camera preview is open
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @SuppressLint("RestrictedApi")
    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        //.setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        // The object tracker also stores the viel layout image in pixels
        _obj_tracker.ObjectTrackerSetViewSize(_draw.getWidth(), _draw.getHeight());

        // the callback functions for every frame the camera provider is processing
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void analyze(@NonNull ImageProxy image_proxy) {

                // get the detections from the cnn detector
                ArrayList<Recognition> detections = _detector.DetectImage(image_proxy);

                // draw the bounding boxes
                _obj_tracker.ObjectTrackerDraw(detections);

                image_proxy.close();

            }
        });

        Preview preview = new Preview.Builder().build();

        // use the back camera of the device
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        // set the surface provider for the preview layout, which is the camera preview
        preview.setSurfaceProvider(_previewView.getSurfaceProvider());

        // lifecycle binding for the camera
        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector,
                imageAnalysis, preview);

    }
}