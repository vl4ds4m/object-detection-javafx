package org.vl4ds4m.objdetjfx;

import javafx.scene.paint.Color;

record ObjectData(double X_CENTER, double Y_CENTER, double WIDTH, double HEIGHT, Type type, double confidence) {
    enum Type {
        SMALL_VEHICLE(Color.BLUE),
        LARGE_VEHICLE(Color.PURPLE),
        PLANE(Color.LIGHTGREEN),
        HELICOPTER(Color.ORANGE),
        SHIP(Color.RED);

        private final Color color;

        Type(Color color) {
            this.color = color;
        }

        int toNum() {
            return ordinal();
        }

        Color toColor() {
            return color;
        }
    }
}
