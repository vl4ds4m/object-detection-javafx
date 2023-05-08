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
    private static final double EPS = 5.0;
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

        sparseObjectsList.sort((a, b) -> Math.toIntExact(Math.round(a.get(0) - b.get(0))));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(labelsFileName))) {
            if (sparseObjectsList.size() > 0) {
                int uniqueObjectIndex = 0;
                boolean isEqual;
                for (int i = 1; i < sparseObjectsList.size(); ++i) {
                    isEqual = true;
                    for (int j = 0; j < 2; ++j) {
                        if (Math.abs(sparseObjectsList.get(i).get(j) - sparseObjectsList.get(i - 1).get(j)) > EPS) {
                            isEqual = false;
                            break;
                        }
                    }
                    if (!isEqual) {
                        int bestObjectIndex = uniqueObjectIndex;
                        double maxConfidence = sparseObjectsList.get(uniqueObjectIndex).get(4);
                        for (int j = uniqueObjectIndex + 1; j < i; ++j) {
                            if (sparseObjectsList.get(j).get(4) > maxConfidence) {
                                maxConfidence = sparseObjectsList.get(j).get(4);
                                bestObjectIndex = j;
                            }
                        }
                        for (double num : sparseObjectsList.get(bestObjectIndex)) {
                            writer.write(num + " ");
                        }
                        writer.newLine();
                        uniqueObjectIndex = i;
                    }
                }
                double maxConfidence = sparseObjectsList.get(uniqueObjectIndex).get(4);
                for (int j = uniqueObjectIndex + 1; j < sparseObjectsList.size(); ++j) {
                    if (sparseObjectsList.get(j).get(4) > maxConfidence) {
                        maxConfidence = sparseObjectsList.get(j).get(4);
                        uniqueObjectIndex = j;
                    }
                }
                for (double num : sparseObjectsList.get(uniqueObjectIndex)) {
                    writer.write(num + " ");
                }
                writer.newLine();
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
}