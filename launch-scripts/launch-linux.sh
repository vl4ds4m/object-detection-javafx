#!/usr/bin/bash

PATH=/usr/lib/jvm/jdk-17/bin:$PATH

# Define application root path
APP_PATH=/home/vladsam/ObjectDetectionApplicationLinux/

cd $APP_PATH

java --module-path \
jar/object-detection-javafx-1.0-SNAPSHOT.jar:\
jar/javafx-base-17.0.6-linux.jar:\
jar/javafx-controls-17.0.6-linux.jar:\
jar/javafx-fxml-17.0.6-linux.jar:\
jar/javafx-graphics-17.0.6-linux.jar:\
jar/javafx-swing-17.0.6-linux.jar:\
jar/opencv-4.7.0-0.jar \
--module org.vl4ds4m.objdetjfx/org.vl4ds4m.objdetjfx.Main