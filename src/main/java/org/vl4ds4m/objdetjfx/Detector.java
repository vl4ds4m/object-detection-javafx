package org.vl4ds4m.objdetjfx;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

import static org.opencv.imgcodecs.Imgcodecs.imread;


/**
 * This class performs directly object detection on the passing image
 */
class Detector {
    private static final double CONFIDENCE_THRESHOLD = 0.25;
    private static final double IOU_THRESHOLD = 0.25;
    private static final int IMAGE_SIDE = 640;

    static {
        nu.pattern.OpenCV.loadLocally();
    }

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
    static List<ObjectData> detectObjectsOnImage(String imageFileName) {
        // select objects with good net confidence.
        List<ObjectData> sparseObjectsList = new ArrayList<>();
        writeHBBToList(imageFileName).forEach(objectData -> {
            int maxConfidenceIndex = 4;
            double maxConfidence = objectData.get(maxConfidenceIndex);
            for (int i = maxConfidenceIndex + 1; i < objectData.size(); ++i) {
                if (objectData.get(i) > maxConfidence) {
                    maxConfidence = objectData.get(i);
                    maxConfidenceIndex = i;
                }

                if (maxConfidence > CONFIDENCE_THRESHOLD) {
                    ObjectData.Type objectType = ObjectData.Type.SMALL_VEHICLE;
                    if (maxConfidenceIndex - 4 == ObjectData.Type.LARGE_VEHICLE.toNum()) {
                        objectType = ObjectData.Type.LARGE_VEHICLE;
                    } else if (maxConfidenceIndex - 4 == ObjectData.Type.PLANE.toNum()) {
                        objectType = ObjectData.Type.PLANE;
                    } else if (maxConfidenceIndex - 4 == ObjectData.Type.HELICOPTER.toNum()) {
                        objectType = ObjectData.Type.HELICOPTER;
                    } else if (maxConfidenceIndex - 4 == ObjectData.Type.SHIP.toNum()) {
                        objectType = ObjectData.Type.SHIP;
                    }
                    sparseObjectsList.add(new ObjectData(
                            objectData.get(0), objectData.get(1), objectData.get(2), objectData.get(3),
                            objectType, maxConfidence));
                }
            }
        });

        List<ObjectData> finalObjectsList = new ArrayList<>();
        for (ObjectData objectData : sparseObjectsList) {
            boolean isNew = true;
            for (int i = 0; i < finalObjectsList.size(); ++i) {
                if (calculateIOU(objectData, finalObjectsList.get(i)) > IOU_THRESHOLD) {
                    isNew = false;
                    if (objectData.confidence() > finalObjectsList.get(i).confidence()) {
                        finalObjectsList.set(i, objectData);
                    }
                    break;
                }
            }
            if (isNew) {
                finalObjectsList.add(objectData);
            }
        }

        return finalObjectsList;
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

        Mat netOutput = net.forward().reshape(0, 4 + ObjectData.Type.values().length).t();

        List<List<Double>> objectsList = new ArrayList<>(netOutput.rows());
        for (int i = 0; i < netOutput.rows(); ++i) {
            objectsList.add(new ArrayList<>(netOutput.cols()));
            for (int j = 0; j < netOutput.cols(); ++j) {
                objectsList.get(i).add(netOutput.get(i, j)[0]);
            }
        }

        return objectsList;
    }

    private static double calculateIOU(ObjectData a, ObjectData b) {
        double aXMin = a.X_CENTER() - a.WIDTH() / 2;
        double aXMax = a.X_CENTER() + a.WIDTH() / 2;
        double aYMin = a.Y_CENTER() - a.HEIGHT() / 2;
        double aYMax = a.Y_CENTER() + a.HEIGHT() / 2;
        double bXMin = b.X_CENTER() - b.WIDTH() / 2;
        double bXMax = b.X_CENTER() + b.WIDTH() / 2;
        double bYMin = b.Y_CENTER() - b.HEIGHT() / 2;
        double bYMax = b.Y_CENTER() + b.HEIGHT() / 2;

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
