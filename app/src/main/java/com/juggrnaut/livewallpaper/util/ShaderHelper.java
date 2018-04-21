package com.juggrnaut.livewallpaper.util;


import android.opengl.GLES20;

import com.juggrnaut.livewallpaper.BuildConfig;

public class ShaderHelper {

    private static final String TAG = ShaderHelper.class.getSimpleName();

    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    public static int compileShader(int type, String shaderCode) {
        int shaderObjectId = GLES20.glCreateShader(type);
        if (shaderObjectId == 0) {
            LogUtil.i(TAG, "Could not create new shader.");
            return 0;
        }
        GLES20.glShaderSource(shaderObjectId, shaderCode);
        GLES20.glCompileShader(shaderObjectId);

        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        LogUtil.i(TAG, "Results of compiling source:\n" + shaderCode + "\n:" + GLES20.glGetShaderInfoLog(shaderObjectId));

        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shaderObjectId);

            LogUtil.i(TAG, "Compilation of shader failed.");

            return 0;
        }

        return shaderObjectId;
    }

    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        int programObjectId = GLES20.glCreateProgram();
        if (programObjectId == 0) {
            LogUtil.i(TAG, "Could not create new program");
            return 0;
        }

        GLES20.glAttachShader(programObjectId, vertexShaderId);
        GLES20.glAttachShader(programObjectId, fragmentShaderId);

        GLES20.glLinkProgram(programObjectId);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);

        LogUtil.i(TAG, "Results of linking program:\n" + GLES20.glGetProgramInfoLog(programObjectId));

        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(programObjectId);
            LogUtil.i(TAG, "Linking of program failed.");
            return 0;
        }

        return programObjectId;
    }

    public static boolean validateProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);
        int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        LogUtil.i(TAG, "Results of validating program: " + validateStatus[0]
                + "\nLog:" + GLES20.glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        int program;

        // Compile the shaders.
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        // Link them into a shader program.
        program = linkProgram(vertexShader, fragmentShader);

        if (BuildConfig.DEBUG) {
            validateProgram(program);
        }
        return program;
    }
}
