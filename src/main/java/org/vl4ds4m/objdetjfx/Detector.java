package org.vl4ds4m.objdetjfx;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

import static org.opencv.imgcodecs.Imgcodecs.imread;


/**
 * This class performs directly object detection on the passing image
 */
public class Detector {
    static {
        nu.pattern.OpenCV.loadLocally();
    }

    private static final double CONFIDENCE_THRESHOLD = 0.25;
    private static final double IOU_THRESHOLD = 0.25;
    private static final int NUM_OF_CLASSES = 5;
    private static final int IMAGE_SIDE = 640;
    private static final String OUT_FILE_SUFFIX = "_labels.txt";

    /**
     * The main method of objects detection on the image. It gets the name of the image file and
     * writes objects coordinates in the TXT file of image name.
     * <p>
     * The output file has the following format: each line looks like
     * <p>
     * {@code X Y WIDTH HEIGHT}
     * <p>
     * and defines one object, where X and Y are coordinates of the center of the bounded box WIDTH x HEIGHT size.
     *
     * @param imageFileName the name of image file
     */
    public static String detectObjectsOnImage(String imageFileName) throws IOException {
        final String labelsFileName = getLabelsFileName(imageFileName);

        List<List<Double>> fullObjectsList = writeHBBToList(imageFileName).stream().map(data -> {
            List<Double> result = new ArrayList<>(5);
            for (int i = 0; i < 4; ++i) {
                result.add(data.get(i));
            }
            double maxConfidence = data.get(4);
            for (int i = 5; i < data.size(); ++i) {
                maxConfidence = Double.max(maxConfidence, data.get(i));
            }
            result.add(maxConfidence);
            return result;
        }).toList();

        List<List<Double>> sparseObjectsList = new ArrayList<>();
        fullObjectsList.forEach(data -> {
            if (data.get(4) > CONFIDENCE_THRESHOLD) {
                sparseObjectsList.add(data);
            }
        });

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(labelsFileName))) {
            List<List<Double>> finalObjectsList = new ArrayList<>();
            for (List<Double> checkedItem : sparseObjectsList) {
                boolean isNew = true;
                for (List<Double> finalItem : finalObjectsList) {
                    if (calculateIOU(checkedItem, finalItem) > IOU_THRESHOLD) {
                        isNew = false;
                        break;
                    }
                }
                if (isNew) {
                    finalObjectsList.add(checkedItem);
                    for (double num : checkedItem) {
                        writer.write(num + " ");
                    }
                    writer.newLine();
                }
            }
        }

        return labelsFileName;
    }

    private static List<List<Double>> writeHBBToList(String imageFileName) {
        Net net = Dnn.readNetFromONNX("net.onnx");

        Mat originImage = imread(imageFileName);
        Mat resizedImage = new Mat(IMAGE_SIDE, IMAGE_SIDE, originImage.type());

        for (int i = 0; i < IMAGE_SIDE; ++i) {
            for (int j = 0; j < IMAGE_SIDE; ++j) {
                if (i < originImage.rows() && j < originImage.cols()) {
                    resizedImage.put(i, j, originImage.get(i, j));
                } else {
                    resizedImage.put(i, j, 0.0, 0.0, 0.0);
                }
            }
        }

        net.setInput(Dnn.blobFromImage(resizedImage, 1.0 / 255.0));

        Mat netOutput = net.forward().reshape(0, 4 + NUM_OF_CLASSES).t();

        List<List<Double>> objectsList = new ArrayList<>(netOutput.rows());
        for (int i = 0; i < netOutput.rows(); ++i) {
            objectsList.add(new ArrayList<>(netOutput.cols()));
            for (int j = 0; j < netOutput.cols(); ++j) {
                objectsList.get(i).add(netOutput.get(i, j)[0]);
            }
        }

        return objectsList;
    }

    private static String getLabelsFileName(String imageFileName) {
        return imageFileName.split("\\.", 2)[0] + OUT_FILE_SUFFIX;
    }

    private static double calculateIOU(List<Double> a, List<Double> b) {
        double aXMin = a.get(0) - a.get(2) / 2;
        double aXMax = a.get(0) + a.get(2) / 2;
        double aYMin = a.get(1) - a.get(3) / 2;
        double aYMax = a.get(1) + a.get(3) / 2;
        double bXMin = b.get(0) - b.get(2) / 2;
        double bXMax = b.get(0) + b.get(2) / 2;
        double bYMin = b.get(1) - b.get(3) / 2;
        double bYMax = b.get(1) + b.get(3) / 2;

        if (aXMax < bXMin || bXMax < aXMin || aYMax < bYMin || bYMax < aYMin) {
            return 0.0;
        }

        double cXMin = Double.max(aXMin, bXMin);
        double cXMax = Double.min(aXMax, bXMax);
        double cYMin = Double.max(aYMin, bYMin);
        double cYMax = Double.min(aYMax, bYMax);

        double aSquare = (aXMax - aXMin) * (aYMax - aYMin);
        double bSquare = (bXMax - bXMin) * (bYMax - bYMin);
        double cSquare = (cXMax - cXMin) * (cYMax - cYMin);

        return cSquare / (aSquare + bSquare - cSquare);
    }
}