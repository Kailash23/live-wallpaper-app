package com.juggernaut.livewallpaper.objects;

import com.juggernaut.livewallpaper.util.Geometry;

import java.util.Random;

import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.setRotateEulerM;


/**
 * This class shoots particles in a particular direction.
 */

public class ParticleShooter {

    // We’ve given our particle shooter its own position, direction, and color.
    private final Geometry.Point position;
    private final Geometry.Vector direction;
    private final int color;

    /**
     * The first thing we’ll do is spread out our particles, and we’ll also vary the speed of each
     * particle to give each particle fountain some more variety
     */

    private final float angleVariance;
    // Each shooter will have an angle variance that will control the spread of particles

    private final float speedVariance;
    // A speed variance to alter the speed of each particle

    private final Random random = new Random();

    // We also have a matrix and two vectors, so we can use Android’s Matrix class to do some math.
    private float[] rotationMatrix = new float[16];
    private float[] directionVector = new float[4];
    private float[] resultVector = new float[4];

    public ParticleShooter(
            Geometry.Point position, Geometry.Vector direction, int color,
            float angleVarianceInDegrees, float speedVariance) {

        // to assign the new member variables.
        this.position = position;
        this.direction = direction;
        this.color = color;
        this.angleVariance = angleVarianceInDegrees;
        this.speedVariance = speedVariance;

        directionVector[0] = direction.x;
        directionVector[1] = direction.y;
        directionVector[2] = direction.z;
    }

    /**
     * In addParticles(), we pass in the particle system and how many particles we want to add, as
     * well as the current time for the particle system. We now have all of our components in place,
     * and we just need to add a few calls to our renderer class to glue everything together.
     */

    public void addParticles(ParticleSystem particleSystem, float currentTime,
                             int count) {

        for (int i = 0; i < count; i++) {

            /*
              To alter the shooting angle, we use Android’s Matrix.setRotateEulerM() to create a
              rotation matrix that will alter the angle by a random amount of angleVariance, which
              is in degrees.
             */
            setRotateEulerM(rotationMatrix, 0,
                    (random.nextFloat() - 0.5f) * angleVariance,
                    (random.nextFloat() - 0.5f) * angleVariance,
                    (random.nextFloat() - 0.5f) * angleVariance);

            /*
              We then multiply this matrix with the direction vector to get a slightly rotated vector.
             */
            multiplyMV(
                    resultVector, 0,
                    rotationMatrix, 0,
                    directionVector, 0);

            float speedAdjustment = 1f + random.nextFloat() * speedVariance;

            /*
              To adjust the speed, we multiply each component of the direction vector with an equal
              random adjustment of speedVariance
             */
            Geometry.Vector thisDirection = new Geometry.Vector(
                    resultVector[0] * speedAdjustment,
                    resultVector[1] * speedAdjustment,
                    resultVector[2] * speedAdjustment);

            // Once that’s done, we add the new particle by calling particleSystem.addParticle().
            particleSystem.addParticle(position, color, thisDirection, currentTime);
        }
    }
}

