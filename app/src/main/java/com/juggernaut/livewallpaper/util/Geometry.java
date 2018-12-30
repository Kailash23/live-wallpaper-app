package com.juggernaut.livewallpaper.util;

/**
 * we’ll define a Geometry class to hold some basic
 * shape definitions and an ObjectBuilder to do the actual building.
 */

/**
 * converting a touched point into a 3D ray
 */

public class Geometry {

    /**
     * Used to load Bitmap Data
     * A ray consists of a starting point and a vector representing the direction of
     * the ray. To create this vector, we call vectorBetween() to create a vector ranging
     * from the near point to the far point.
     */

    // We’ve added a class to represent a point in 3D space.
    public static class Point {
        public final float x, y, z;

        public Point(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // Basic definition for Vector
    public static class Vector {
        public final float x, y, z;

        public Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}