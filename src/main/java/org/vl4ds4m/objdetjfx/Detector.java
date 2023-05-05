package org.vl4ds4m.objdetjfx;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * This class performs directly object detection on the passing image
 */
public class Detector {
    static {
        System.loadLibrary("Detector");
    }

    private static final double CONFIDENCE_THRESHOLD = 0.25;

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
        getBoundedBoxes(imageFileName);

        final String fileName = imageFileName.split("\\.", 2)[0];
        final String labelsFileName = fileName + "_labels.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName + "_RAW.txt"));
             BufferedWriter writer = new BufferedWriter(new FileWriter(labelsFileName))
        ) {
            reader.lines().forEach(line -> {
                try {
                    List<String> objectData = Arrays.stream(line.split(" ")).toList();
                    double maxConfidence = Double.MIN_VALUE;
                    for (int i = 4; i < objectData.size(); ++i) {
                        maxConfidence = Double.max(maxConfidence, Double.parseDouble(objectData.get(i)));
                    }
                    if (maxConfidence > CONFIDENCE_THRESHOLD) {
                        for (int i = 0; i < 4; ++i) {
                            writer.write(objectData.get(i) + " ");
                        }
                        writer.write(Double.toString(maxConfidence));
                        writer.newLine();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return labelsFileName;
    }

    native private static void getBoundedBoxes(String imageFileName);
}
