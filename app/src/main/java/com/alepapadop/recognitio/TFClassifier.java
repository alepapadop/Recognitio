package com.alepapadop.recognitio;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.os.Trace;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.TensorOperatorWrapper;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import static java.lang.Math.min;


public abstract class TFClassifier {

    private static int                  _num_detections = 5;
    private int                         _num_threads = 4;
    private boolean                     _is_quantized;

    private int[]                       _detections;
    private double[][][]                _locations;
    private int[][]                     _classes;
    private double[][]                  _scores;

    private Interpreter                 _tf_interpreter;
    private MappedByteBuffer            _tf_file_model;
    private Interpreter.Options         _tf_options;
    private TensorImage                 _tf_image;
    private TensorBuffer                _tf_buffer;
    private TensorProcessor             _tf_proc;

    private int                         _img_sz_x;
    private int                         _img_sz_y;
    private List<String>                _labels;

    ObjectDetector.ObjectDetectorOptions    _tf_obj_detector_options;
    ObjectDetector                          _tf_obj_detector;


    protected abstract String           getModelPath();
    protected abstract String           getLabelPath();
    protected abstract TensorOperator   getPostProcessNormalizeOp();
    protected abstract TensorOperator   getPreProcessNormalizeOp();

    public static TFClassifier create(Activity activity, Model.Device device, int numThreads)
            throws IOException {

        return new FloatMobileNetClassifier(activity, device, numThreads);
    }

    private Tensor TFInterpreterInputData()
    {
        Tensor tensor_in = null;

        if (_tf_interpreter.getInputTensorCount() > 0) {
            tensor_in = _tf_interpreter.getInputTensor(0);
        }

        return tensor_in;
    }

    private Tensor TFInterpreterOutputData()
    {
        Tensor tensor_out = null;

        if (_tf_interpreter.getOutputTensorCount() > 0) {
            tensor_out = _tf_interpreter.getOutputTensor(0);
        }

        return tensor_out;
    }

    protected TFClassifier(Activity activity, Model.Device device, int num_threads) throws IOException {
        // create the options for the Intepreter
        _tf_options = new Interpreter.Options();
        _tf_options.setNumThreads(num_threads);

        // load the model for the Interpreter
        _tf_file_model = FileUtil.loadMappedFile(activity, getModelPath());

        _labels = FileUtil.loadLabels(activity, getLabelPath());

        // create the Interpreter
        _tf_interpreter = new Interpreter(_tf_file_model, _tf_options);

        Tensor tensor_in = TFInterpreterInputData();
        Tensor tensor_out = TFInterpreterOutputData();

        // {1, height, width, 3}
        int[] shape_in = tensor_in.shape();
        int[] shape_out = tensor_out.shape();

        DataType data_type_in = tensor_in.dataType();
        DataType data_type_out = tensor_out.dataType();

        _img_sz_x = shape_in[2];
        _img_sz_y = shape_in[1];

        // Create a TensorImage object.
        _tf_image = new TensorImage(data_type_in);

        // Creates the output tensor and its processor.
        _tf_buffer = TensorBuffer.createFixedSize(shape_out, data_type_out);
        //TensorBuffer.



        // Creates the post processor for the output probability.
        _tf_proc = new TensorProcessor.Builder().add(getPostProcessNormalizeOp()).build();

    }


    public void TFClose() {

        if (_tf_interpreter != null) {
            _tf_interpreter.close();
            _tf_interpreter = null;
        }

        //if (gpuDelegate != null) {
        //    gpuDelegate.close();
        //    gpuDelegate = null;
        //}
        //if (nnApiDelegate != null) {
        //    nnApiDelegate.close();
        //    nnApiDelegate = null;
        //}
    }

    private TensorImage TFLoadImage(final Bitmap bitmap, int sensorOrientation) {

        _tf_image.load(bitmap);

        int cropSize = min(bitmap.getWidth(), bitmap.getHeight());
        int numRotation = sensorOrientation / 90;

        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(_img_sz_x, _img_sz_y, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(new Rot90Op(numRotation))
                        .add(getPreProcessNormalizeOp())
                        .build();

        return imageProcessor.process(_tf_image);
    }

    private static List<Recognition> TFGetBestResults(Map<String, Float> labelProb) {

        // a sorted queue with the recognition results
        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(
                        _num_detections,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition rec1, Recognition rec2) {
                                return Float.compare(rec1.getConfidence(), rec2.getConfidence());
                            }
                        });

        // generate a string with the results of the sorted queue
        for (Map.Entry<String, Float> entry : labelProb.entrySet()) {
            pq.add(new Recognition("" + entry.getKey(), entry.getKey(),
                                    entry.getValue(), null));
        }

        // ArrayList containing the results
        ArrayList<Recognition> recognitions = new ArrayList<>();

        // Find the correct size to pass items from the queue to the ArrayList
        int size = min(pq.size(), _num_detections);

        for (int i = 0; i < size; ++i) {
            recognitions.add(pq.poll());
        }

        return recognitions;
    }

    public List<Recognition> TFRecognizeImage(final Bitmap bitmap, int sensorOrientation) {

        _tf_image = TFLoadImage(bitmap, sensorOrientation);

        _tf_interpreter.run(_tf_image.getBuffer(), _tf_buffer.getBuffer().rewind());


        // Gets the map of label and probability.
        Map<String, Float> labeledProbability = new
                TensorLabel(_labels, _tf_proc.process(_tf_buffer)).getMapWithFloatValue();

        _tf_buffer.getFloatValue(0);

        return TFGetBestResults(labeledProbability);
    }


    protected TFClassifier(Activity activity, int num_threads) throws IOException {
        // create the options for the Intepreter

        _tf_file_model = FileUtil.loadMappedFile(activity, getModelPath());

        _tf_obj_detector_options = ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(2).build();
        _tf_obj_detector = ObjectDetector.createFromBufferAndOptions(_tf_file_model, _tf_obj_detector_options);

    }

    public List<Recognition> TFRecognizeImage2(final Bitmap bitmap, int sensorOrientation) {

        _tf_image = TensorImage.fromBitmap(bitmap);
        // Run inference
        List<Detection> results = _tf_obj_detector.detect(_tf_image);

        final ArrayList<Recognition> recognitions = new ArrayList<>();
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
}
