package org.vl4ds4m.objdetjfx;

/**
 * This class performs directly object detection on the passing image
 */
public class Detector {
    static {
        System.loadLibrary("Detector");
    }

    /**
     * The main method of objects detection on the image. It gets the name of the image file and
     * writes objects coordinates in the TXT file of image name.
     * <p>
     * The output file has the following format: each line looks like
     * <p>
     * {@code X Y WIDTH HEIGHT CONFIDENCE}
     * <p>
     * and defines one object, where X and Y are coordinates of the center of the bounded box WIDTH x HEIGHT size.
     * @param imageFile the name of image file
     */
    public static void detectObjectsOnImage(String imageFile) {
        getBoundedBoxes(imageFile);
    }
    native private static void getBoundedBoxes(String imageFile);
}
