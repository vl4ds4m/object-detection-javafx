#include <fstream>
#include <opencv2/highgui.hpp>
#include <opencv2/dnn.hpp>
#include "org_vl4ds4m_objdetjfx_Detector.h"

JNIEXPORT void JNICALL Java_org_vl4ds4m_objdetjfx_Detector_getBoundedBoxes(JNIEnv *env, jclass jcls, jstring jstr) {
    const char *raw_string = env->GetStringUTFChars(jstr, NULL);
    const long string_length = env->GetStringUTFLength(jstr);
    const std::string image_file_name(raw_string, string_length);

    const int NUM_OF_CLASSES = 5;
    const int IMAGE_SIDE = 640;

    cv::dnn::Net net(cv::dnn::readNetFromONNX(
        "/home/vladsam/Projects/object-detection-javafx/src/main/resources/net.onnx"));

    cv::Mat origin_image(cv::imread(image_file_name));
    cv::Mat resized_image(cv::Size(IMAGE_SIDE, IMAGE_SIDE), cv::rawType<cv::Vec3b>());

    for (int i = 0; i < IMAGE_SIDE; ++i) {
        for (int j = 0; j < IMAGE_SIDE; ++j) {
            resized_image.at<cv::Vec3b>(i, j) =
                    i < origin_image.rows && j < origin_image.cols ?
                    origin_image.at<cv::Vec3b>(i, j) :
                    cv::Vec3b(0, 0, 0);
        }
    }

    net.setInput(cv::dnn::blobFromImage(resized_image, 1.0 / 255.0));

    cv::Mat net_output(net.forward().reshape(0, 4 + NUM_OF_CLASSES).t());

    std::ofstream labels_file(image_file_name.substr(0, image_file_name.find(".")) + "_RAW.txt");

    for (int i = 0; i < net_output.rows; ++i) {
        for (int j = 0; j < net_output.cols; ++j) {
            labels_file << net_output.at<float>(i, j) << " ";
        }
        labels_file << std::endl;
    }

    labels_file.close();
}

