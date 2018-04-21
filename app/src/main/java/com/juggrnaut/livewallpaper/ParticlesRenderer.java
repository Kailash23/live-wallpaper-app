package com.juggrnaut.livewallpaper;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.GL_LESS;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;
import static android.opengl.Matrix.transposeM;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView.Renderer;
import android.os.SystemClock;
import android.util.Log;

import com.juggrnaut.livewallpaper.objects.ParticleShooter;
import com.juggrnaut.livewallpaper.objects.ParticleSystem;
import com.juggrnaut.livewallpaper.programs.ParticleShaderProgram;
import com.juggrnaut.livewallpaper.util.Geometry;
import com.juggrnaut.livewallpaper.util.LoggerConfig;
import com.juggrnaut.livewallpaper.util.MatrixHelper;
import com.juggrnaut.livewallpaper.util.TextureHelper;

/**
 * Particles in action.
 */
public class ParticlesRenderer implements Renderer {

    // We defined here standard variables for the Android context and our
    // matrices, and we have our particle shader, system, and three particle
    // shooters. We also have a variable for the global start time and a standard constructor.

    private static final String TAG = "ParticlesRenderer";
    private final Context context;
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewMatrixForSkybox = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] tempMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];
    private final float[] it_modelViewMatrix = new float[16];

    private final float[] modelViewProjectionMatrix = new float[16];



    private ParticleShaderProgram particleProgram;
    private ParticleSystem particleSystem;
    private ParticleShooter redParticleShooter;
    private ParticleShooter greenParticleShooter;
    private ParticleShooter blueParticleShooter;

    private long globalStartTime;
    private int particleTexture;

    private float xRotation, yRotation;

    private float xOffset, yOffset;

    private long frameStartTimeMs;
    private long startTimeMs;
    private int frameCount;

    public ParticlesRenderer(Context context) {
        this.context = context;
    }

    public void handleTouchDrag(float deltaX, float deltaY) {
        xRotation += deltaX / 16f;
        yRotation += deltaY / 16f;

        if (yRotation < -90) {
            yRotation = -90;
        } else if (yRotation > 90) {
            yRotation = 90;
        }

        // Setup view matrix
        updateViewMatrices();
    }

    public void handleOffsetsChanged(float xOffset, float yOffset) {
        // Offsets range from 0 to 1.
        this.xOffset = (xOffset - 0.5f) * 2.5f;
        this.yOffset = (yOffset - 0.5f) * 2.5f;
        updateViewMatrices();
    }

    private void updateViewMatrices() {
        setIdentityM(viewMatrix, 0);
        rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f);
        rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f);
        System.arraycopy(viewMatrix, 0, viewMatrixForSkybox, 0, viewMatrix.length);

        // We want the translation to apply to the regular view matrix, and not
        // the skybox.
        translateM(viewMatrix, 0, 0 - xOffset, -1.5f - yOffset, -5f);

    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //We set the clear color to black,

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);


        particleProgram = new ParticleShaderProgram(context);
        //Initialize our particle shader program.

        particleSystem = new ParticleSystem(10000);
        // Initialize a new particle system with a maximum limit of
        // ten thousand particles

        globalStartTime = System.nanoTime();
        // We set the global start time to the current system time using
        // System.nanoTime() as the base.
        // We want the particle system to run on a floating point
        // time basis so that when the particle system is initialized, the current
        // time will be 0.0 and a particle created at that time will have a
        // creation time of 0.0.


        final Geometry.Vector particleDirection = new Geometry.Vector(0f, 0.5f, 0f);

        // We’ve set things up so that each particle fountain has an angle
        // variance of 5 degrees and a speed variance of 1 unit.

        // TODO: Angle and speed from here
        final float angleVarianceInDegrees = 15f;
        final float speedVariance = 1f;

        // The next part of the method sets up our three particle fountains. Each fountain
        // is represented by a particle shooter, and each shooter will shoot its particles
        // in the direction of particleDirection, or straight up along the y-axis. We’ve aligned
        // the three fountains from left to right, and we’ve set the colors so that the first
        // one is red, the second is green, and the third is blue.

        // TODO: Change color from here
        redParticleShooter = new ParticleShooter(
                // TODO: Position of fountain can be changed from here
                new Geometry.Point(-0.9f, 0f, 3f),
                particleDirection,
                Color.rgb(156, 55, 94),
                angleVarianceInDegrees,
                speedVariance);

        greenParticleShooter = new ParticleShooter(
                new Geometry.Point(-0.4f, 0f, 3f),
                particleDirection,
                Color.rgb(11, 105, 164),
                angleVarianceInDegrees,
                speedVariance);

        blueParticleShooter = new ParticleShooter(
                new Geometry.Point(0.1f, 0f, 3f),
                particleDirection,
                Color.rgb(218, 209, 99),
                angleVarianceInDegrees,
                speedVariance);

        particleTexture = TextureHelper.loadTexture(context, R.drawable.particle_texture);


    }

    // This is a standard definition, with a regular perspective projection and a view
    // matrix that pushes things down and into the distance.

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);

        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width
                / (float) height, 1f, 100f);
        updateViewMatrices();
    }

    private void limitFrameRate(int framesPerSecond) {
        long elapsedFrameTimeMs = SystemClock.elapsedRealtime() - frameStartTimeMs;
        // TODO: FPS can be adjusted here (Set to MAX FPS)
        long expectedFrameTimeMs = 10 / framesPerSecond;
        long timeToSleepMs = expectedFrameTimeMs - elapsedFrameTimeMs;

        if (timeToSleepMs > 0) {
            SystemClock.sleep(timeToSleepMs);
        }
        frameStartTimeMs = SystemClock.elapsedRealtime();
    }

    private void logFrameRate() {
        if (LoggerConfig.ON) {
            long elapsedRealtimeMs = SystemClock.elapsedRealtime();
            double elapsedSeconds = (elapsedRealtimeMs - startTimeMs) / 1000.0;

            if (elapsedSeconds >= 1.0) {
                Log.v(TAG, frameCount / elapsedSeconds + "fps");
                startTimeMs = SystemClock.elapsedRealtime();
                frameCount = 0;
            }
            frameCount++;
        }
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        // TODO: FPS can be changed here
        limitFrameRate(60);
        logFrameRate();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        drawParticles();
    }


    private void drawParticles() {

        float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;
        // Five seconds later, a new particle will have a creation time of 5.0. To
        // do this, we can take the difference between the current system time and
        // globalStartTime, and since System.nanoTime() returns the time in nanoseconds,
        // we’ll just need to divide the difference by 1 trillion to convert this into seconds.

        redParticleShooter.addParticles(particleSystem, currentTime, 2);
        greenParticleShooter.addParticles(particleSystem, currentTime, 6);
        blueParticleShooter.addParticles(particleSystem, currentTime, 2);
        // Each time a new frame is drawn, we calculate the current time and pass it
        // into the shader. That will tell the shader how far each particle has moved
        // since it was created. We also generate five new particles for each fountain.
        // and then we draw the particles with the particle shader program.

        setIdentityM(modelMatrix, 0);
        updateMvpMatrix();

        // If we imagine our three particle streams
        // as a fireworks fountain, like the one we’d see at a fireworks show, then we’d
        // expect the particles to give off light; and the more of them there are, the
        // brighter things should be. One of the ways that we can reproduce this effect
        // is by using additive blending.

        // GL_ONE is just a placeholder for 1, and since multiplying anything by 1 results
        //in the same number, the equation can be simplified as follows:
        //output = source fragment + destination fragment

        // the fragments from our fragment shader will be added to the fragments already on the screen,
        // and that’s how we get additive blending.

        // TODO: Change particle blending effect
        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        particleProgram.useProgram();
        particleProgram.setUniforms(modelViewProjectionMatrix, currentTime, particleTexture);
        particleSystem.bindData(particleProgram);
        particleSystem.draw();

        glDisable(GL_BLEND);
        glDepthMask(true);
    }

    private void updateMvpMatrix() {
        multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        invertM(tempMatrix, 0, modelViewMatrix, 0);
        transposeM(it_modelViewMatrix, 0, tempMatrix, 0);
        multiplyMM(
                modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
    }

}