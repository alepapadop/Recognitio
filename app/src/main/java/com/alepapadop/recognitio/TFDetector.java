package com.alepapadop.recognitio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.common.ops.QuantizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.metadata.MetadataExtractor;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.min;
import static java.lang.Math.random;

public class TFDetector {

    private final MappedByteBuffer          _tf_file_model;
    private int                             _img_sz_x = 0;
    private int                             _img_sz_y = 0;
    private ObjectDetector                  _tf_obj_detector;
    private Bitmap                          _bitmap = null;


    public TFDetector(Activity activity) throws IOException {

        // load the model for the Interpreter
        _tf_file_model = FileUtil.loadMappedFile(activity, RecognitioSetting.get_model_name());

        // set the options for the detection
        ObjectDetector.ObjectDetectorOptions tf_obj_detector_options = ObjectDetector.ObjectDetectorOptions.builder()
                                                                            .setMaxResults(RecognitioSetting.get_num_detections())
                                                                            .setNumThreads(RecognitioSetting.get_num_threads())
                                                                            .setScoreThreshold(RecognitioSetting.get_confidence_threshold())
                                                                            .build();

        // create the detector
        _tf_obj_detector = ObjectDetector.createFromBufferAndOptions(_tf_file_model, tf_obj_detector_options);

        // Get the metadata of the tfile
        MetadataExtractor meta = new MetadataExtractor(_tf_file_model);

        if (meta.hasMetadata()) {
            int shape[] = meta.getInputTensorShape(0);  // this is of {1, 300, 300 ,3}

            _img_sz_x = shape[1];
            _img_sz_y = shape[2];

        } else {
            assert false;
        }
    }

    // Prepares the bitmap image for the CNN network
    private TensorImage TFLoadImage(final Bitmap bitmap, int sensorOrientation) {

        //_tf_image.load(bitmap);
        // Create a TensorImage from the bitmap in order to apply on the image translations
        TensorImage tf_image = TensorImage.fromBitmap(bitmap);


        //int cropSize = min(bitmap.getWidth(), bitmap.getHeight());

        // make the rotation, this is a very complex issue. The sensor of the camera has its own orientation
        // and the phone itself has its own orientation
        int numRotation = (360 - sensorOrientation) / 90;

        // make the image translation and prepare it for the CNN network
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        //.add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(_img_sz_x, _img_sz_y, ResizeOp.ResizeMethod.BILINEAR))
                        .add(new Rot90Op(numRotation))
                        //.add(new NormalizeOp(127.5f, 127.5f))
                        //.add(new QuantizeOp(0, 255))
                        .add(new CastOp(DataType.UINT8))
                        .build();

        return imageProcessor.process(tf_image);
    }

    // Converts the image proxy to a bitmap
    private Bitmap TFPreProcessImage(Context context,@NonNull ImageProxy image_proxy) {
        YuvToRgbConverter y2b = new YuvToRgbConverter(context);

        if (_bitmap == null) {
            // create once a single dummy bitmap object and used for filling it later with the
            // image data
            _bitmap = Bitmap.createBitmap(
                    image_proxy.getWidth(), image_proxy.getHeight(), Bitmap.Config.ARGB_8888);
        }

        // this is another issue here. There is nowhere an explanation about the YUV format in the
        // android documentation. There is only a link that says you should use a converter from
        // the AOSP (Android Open Source Project) to convert the image proxy image to a bitmap
        // i have lost many days on this issue
        try (@SuppressLint("UnsafeExperimentalUsageError") Image img = image_proxy.getImage()) {
            y2b.yuvToRgb(img, _bitmap);
        }

        return _bitmap;
    }

    // Runs the Detector and stores the results
    public ArrayList<Recognition> TFDetectImage(Context context, ImageProxy image_proxy) {
        final ArrayList<Recognition> recognitions = new ArrayList<>();

        // get the bitmap image from the image proxy
        Bitmap bitmap = TFPreProcessImage(context, image_proxy);

        // debug operation, store an image on the device to check the orientation
        //debug_write_image_wrap(context, bitmap);

        //_tf_image = TensorImage.fromBitmap(bitmap);
        // Get the TensorImage form the bitmap
        TensorImage tf_image = TFLoadImage(bitmap, image_proxy.getImageInfo().getRotationDegrees());

        // debug operation, store an image on the device to check the cropping and the translations
        // of the image
        //debug_write_image_wrap(context, tf_image.getBitmap());

        // make the detections and store them
        List<Detection> results = _tf_obj_detector.detect(tf_image);

        int cnt = 0;
        for (Detection detection : results) {
                recognitions.add(
                        new Recognition(
                                "" + cnt++,
                                detection.getCategories().get(0).getLabel(),
                                detection.getCategories().get(0).getScore(),
                                detection.getBoundingBox()));
        }



        return recognitions;
    }

    public Size TFDetectorImageInputSize() {
        Size size = new Size(_img_sz_x, _img_sz_y);

        return size;
    }

    //-----------------------------------------
    //-----------------------------------------
    //------------ DEBUG FUNCTIONS ------------
    //-----------------------------------------
    //-----------------------------------------

    private static boolean flg = true;

    private void debug_write_image_wrap(Context context, Bitmap bitmap) {
        try {
            debug_write_image(context, bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // a function to write a botmap in a file in the device in order to check the bitmap
    // image status (rotation, colors, corpping  etc)
    private void debug_write_image(Context context, Bitmap bitmap) throws IOException {

        if (flg == false) {
            return;
        }
        try{
            OutputStream fos;

            String fileName = "detect_" + random() + "_.jpg";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));

                Log.d(RecognitioSetting.get_log_tag(), "Pathxxx" + imageUri.toString() + "  " + imageUri.getPath() + "   ");
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File image = new File(imagesDir, fileName);
                fos = new FileOutputStream(image);
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Objects.requireNonNull(fos).close();
        }catch (IOException e) {
            // Log Message
        }

        flg = false;
    }

}
