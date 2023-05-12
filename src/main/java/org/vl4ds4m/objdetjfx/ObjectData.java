package org.vl4ds4m.objdetjfx;

record ObjectData(double X_CENTER, double Y_CENTER, double WIDTH, double HEIGHT, Type type, double confidence) {
    enum Type {
        SMALL_VEHICLE, LARGE_VEHICLE, PLANE, HELICOPTER, SHIP;

        int toNum() {
            return ordinal();
        }
    }
}
