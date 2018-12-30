package com.juggernaut.livewallpaper.wallpaper;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;


import com.juggernaut.livewallpaper.ParticlesRenderer;
import com.juggernaut.livewallpaper.util.LoggerConfig;

public class GLWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }
    public class GLEngine extends Engine {
        private static final String TAG = "GLEngine";

        private WallpaperGLSurfaceView glSurfaceView;
        private ParticlesRenderer particlesRenderer;
        private boolean rendererSet;


        @SuppressLint("ObsoleteSdkInt")
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            if (LoggerConfig.ON) {
                Log.d(TAG, "onCreate(" + surfaceHolder + ")");
            }
            glSurfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);

            // Check if the system supports OpenGL ES 2.0.
            ActivityManager activityManager =
                    (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

            assert activityManager != null;
            ConfigurationInfo configurationInfo = activityManager
                    .getDeviceConfigurationInfo();

            final boolean supportsEs2 =
                    configurationInfo.reqGlEsVersion >= 0x20000
                            // Check for emulator.
                            || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                            && (Build.FINGERPRINT.startsWith("generic")
                            || Build.FINGERPRINT.startsWith("unknown")
                            || Build.MODEL.contains("google_sdk")
                            || Build.MODEL.contains("Emulator")
                            || Build.MODEL.contains("Android SDK built for x86")));

            particlesRenderer = new ParticlesRenderer(GLWallpaperService.this);

            if (supportsEs2) {
                glSurfaceView.setEGLContextClientVersion(2);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    glSurfaceView.setPreserveEGLContextOnPause(true);
                }
                glSurfaceView.setRenderer(particlesRenderer);
                rendererSet = true;
            } else {

                Toast.makeText(GLWallpaperService.this,
                        "This device does not support OpenGL ES 2.0.",
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (LoggerConfig.ON) {
                Log.d(TAG, "onVisibilityChanged(" + visible + ")");
            }
            if (rendererSet) {
                if (visible) {
                    glSurfaceView.onResume();
                } else {
                    glSurfaceView.onPause();
                }
            }
        }
        @Override
        public void onOffsetsChanged(final float xOffset, final float yOffset,
                                     float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            glSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    particlesRenderer.handleOffsetsChanged(xOffset, yOffset);
                }
            });
        }
        @Override
        public void onDestroy() {
            super.onDestroy();
            if (LoggerConfig.ON) {
                Log.d(TAG, "onDestroy()");
            }
            glSurfaceView.onWallpaperDestroy();
        }
        class WallpaperGLSurfaceView extends GLSurfaceView {
            private static final String TAG = "WallpaperGLSurfaceView";
            WallpaperGLSurfaceView(Context context) {
                super(context);

                if (LoggerConfig.ON) {
                    Log.d(TAG, "WallpaperGLSurfaceView(" + context + ")");
                }
            }
            @Override
            public SurfaceHolder getHolder() {
                if (LoggerConfig.ON) {
                    Log.d(TAG, "getHolder(): returning " + getSurfaceHolder());
                }
                return getSurfaceHolder();
            }
            public void onWallpaperDestroy() {
                if (LoggerConfig.ON) {
                    Log.d(TAG, "onWallpaperDestroy()");
                }
                super.onDetachedFromWindow();
            }
        }
    }
}
