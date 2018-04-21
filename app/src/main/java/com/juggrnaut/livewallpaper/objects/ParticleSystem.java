package com.juggrnaut.livewallpaper.objects;

import android.graphics.Color;

import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.glDrawArrays;

import com.juggrnaut.livewallpaper.data.VertexArray;
import com.juggrnaut.livewallpaper.programs.ParticleShaderProgram;
import com.juggrnaut.livewallpaper.util.Geometry.Point;
import com.juggrnaut.livewallpaper.util.Geometry.Vector;

public class ParticleSystem {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int VECTOR_COMPONENT_COUNT = 3;
    private static final int PARTICLE_START_TIME_COMPONENT_COUNT = 1;

    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT
                    + COLOR_COMPONENT_COUNT
                    + VECTOR_COMPONENT_COUNT
                    + PARTICLE_START_TIME_COMPONENT_COUNT;

    /* So far we just have some basic definitions for the component counts and the
       stride between particles */

    private static final int STRIDE = TOTAL_COMPONENT_COUNT * 4;

    private final float[] particles;
    // We now have a floating-point array to store the particles.

    private final VertexArray vertexArray;
    // A VertexArray to represent the data that we’ll send to OpenGL.

    private final int maxParticleCount;
    // maxParticleCount to hold the maximum number of particles,
    // since the size of the array is fixed.

    private int currentParticleCount;
    private int nextParticle;
    // currentParticleCount and nextParticle to keep track of
    // the particles in the array.

    public ParticleSystem(int maxParticleCount) {
        particles = new float[maxParticleCount * TOTAL_COMPONENT_COUNT];
        vertexArray = new VertexArray(particles);
        this.maxParticleCount = maxParticleCount;
    }

    /* To create the new particle, we first pass in the position, color, direction, and
       the particle creation time. The color is passed in as an Android color, and
       we’ll use Android’s Color class to parse the color into its separate components.*/

    /* Before we can add a new particle to our array, we need to calculate where it
       needs to go. Our array is sort of like an amorphous blob, with all of the particles
       stored together. To calculate the right offset, we use nextParticle to store
       the number of the next particle, with the first particle starting at zero. We
       can then get the offset by multiplying nextParticle by the number of components
       per particle. We store this offset in particleOffset and currentOffset; we’ll use
       particleOffset to remember where our new particle started, and currentOffset to
       remember the position for each attribute of the new particle */

    public void addParticle(Point position, int color, Vector direction,
                            float particleStartTime) {
        final int particleOffset = nextParticle * TOTAL_COMPONENT_COUNT;

        int currentOffset = particleOffset;
        nextParticle++;
        // Each time a new particle is added, we increment nextParticle by 1, and when
        // we reach the end, we start over at 0 so we can recycle the oldest particles.

        if (currentParticleCount < maxParticleCount) {
            currentParticleCount++;
            // We also need to keep track of how many particles need to be drawn, and we
            // do this by incrementing currentParticleCount each time a new particle is added,
            // keeping it clamped to the maximum.
        }

        if (nextParticle == maxParticleCount) {
            // Start over at the beginning, but keep currentParticleCount so
            // that all the other particles still get drawn.
            nextParticle = 0;
        }

        // First we write out the position.
        particles[currentOffset++] = position.x;
        particles[currentOffset++] = position.y;
        particles[currentOffset++] = position.z;

        // Then the color (using Android’s Color class to
        // parse each component), then the direction vector.

        // Android’s Color class returns components in a range from 0 to
        // 255, while OpenGL expects the color to be from 0 to 1.
        particles[currentOffset++] = Color.red(color) / 255f;
        particles[currentOffset++] = Color.green(color) / 255f;
        particles[currentOffset++] = Color.blue(color) / 255f;

        // And finally the particle creation time.
        particles[currentOffset++] = direction.x;
        particles[currentOffset++] = direction.y;
        particles[currentOffset++] = direction.z;

        particles[currentOffset++] = particleStartTime;

        // We still need to copy the new particle over to our native buffer so that OpenGL
        // can access the new data.

        // We want to copy over only the new data so that we don’t waste time copying
        // over data that hasn’t changed, so we pass in the start offset for the new
        // particle and the count.
        vertexArray.updateBuffer(particles, particleOffset, TOTAL_COMPONENT_COUNT);
    }

    // Add a binding function(boiler-plate code):-binding our vertex data to the right attributes in the
    // shader program and taking care to respect the same ordering as the one we
    // used in addParticle().
    public void bindData(ParticleShaderProgram particleProgram) {
        int dataOffset = 0;
        vertexArray.setVertexAttribPointer(dataOffset,
                particleProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, STRIDE);
        dataOffset += POSITION_COMPONENT_COUNT;

        vertexArray.setVertexAttribPointer(dataOffset,
                particleProgram.getColorAttributeLocation(),
                COLOR_COMPONENT_COUNT, STRIDE);
        dataOffset += COLOR_COMPONENT_COUNT;

        vertexArray.setVertexAttribPointer(dataOffset,
                particleProgram.getDirectionVectorAttributeLocation(),
                VECTOR_COMPONENT_COUNT, STRIDE);
        dataOffset += VECTOR_COMPONENT_COUNT;

        vertexArray.setVertexAttribPointer(dataOffset,
                particleProgram.getParticleStartTimeAttributeLocation(),
                PARTICLE_START_TIME_COMPONENT_COUNT, STRIDE);
    }

    // We now have a particle system in place. This system will let us add particles
    // up to a certain limit, recycle old particles, and efficiently locate the particles
    // next to each other in memory
    public void draw() {
        glDrawArrays(GL_POINTS, 0, currentParticleCount);
    }
}

