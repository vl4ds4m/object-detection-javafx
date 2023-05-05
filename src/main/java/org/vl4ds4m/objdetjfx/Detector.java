package org.vl4ds4m.objdetjfx;

import java.io.*;
import java.util.ArrayList;
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
    private static final double EPS = 5.0;

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
        final String rawLabelsFileName = fileName + "_RAW.txt";
        final String labelsFileName = fileName + "_labels.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(rawLabelsFileName));
             BufferedWriter writer = new BufferedWriter(new FileWriter(labelsFileName))
        ) {
            List<List<Double>> fullObjectsList = new ArrayList<>(reader.lines().map(
                    line -> Arrays.stream(line.split(" ")).map(Double::valueOf).toList()
            ).toList());

            fullObjectsList = fullObjectsList.stream().map(data -> {
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

            sparseObjectsList.sort((a, b) -> (int) (a.get(0) - b.get(0)));

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

    native private static void getBoundedBoxes(String imageFileName);
}
