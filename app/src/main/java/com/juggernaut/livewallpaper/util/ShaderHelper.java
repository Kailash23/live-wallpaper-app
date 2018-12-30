package com.juggernaut.livewallpaper.util;


import android.opengl.GLES20;

import com.juggernaut.livewallpaper.BuildConfig;

public class ShaderHelper {

    private static final String TAG = ShaderHelper.class.getSimpleName();

    /**
     * Now that we’ve read in the shader source from our files, the next step is to
     * compile each shader.
     */

    // boilerplate code
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    public static int compileShader(int type, String shaderCode) {


        /*
           The first thing we should do is create a new shader object and check if the
           creation was successful.

           We create a new shader object with a call to glCreateShader() and store the ID
           of that object in shaderObjectId. The type can be GL_VERTEX_SHADER for a vertex
           shader, or GL_FRAGMENT_SHADER for a fragment shader.
         */
        int shaderObjectId = GLES20.glCreateShader(type);
        if (shaderObjectId == 0) {
            LogUtil.i(TAG, "Could not create new shader.");
            return 0;
        }


        /*
            Once we have a valid shader object, we call glShaderSource(shaderObjectId, shaderCode)
            to upload the source code. This call tells OpenGL to read in the source code
            defined in the String shaderCode and associate it with the shader object referred to
            by shaderObjectId.
         */
        GLES20.glShaderSource(shaderObjectId, shaderCode);

        // to compile the shader
        GLES20.glCompileShader(shaderObjectId);

        /*
            Retrieving the Compilation Status the following code to check if OpenGL was able to
            successfully compile the shader.
         */


        int[] compileStatus = new int[1];

        /*
           To check whether the compile failed or succeeded, we first create a new int
           array with a length of 1 and call it compileStatus. We then call glGetShaderiv(shader-
           ObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0). This tells OpenGL to read the
           compile status associated with shaderObjectId and write it to the 0th element of
           compileStatus.
         */
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        LogUtil.i(TAG, "Results of compiling source:\n" + shaderCode + "\n:" + GLES20.glGetShaderInfoLog(shaderObjectId));

        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shaderObjectId);

            LogUtil.i(TAG, "Compilation of shader failed.");

            return 0;
        }
        /*
          If it’s 0, then compilation failed.
          We no longer need the shader object in that case, so we tell OpenGL to delete
          it and return 0 to the calling code. If the compilation succeeded, then our
          shader object is valid and we can use it in our code.
         */
        return shaderObjectId;
    }

    /**
     * We know that the vertex shader calculates the final position of each vertex
     * on the screen. We also know that when OpenGL groups these vertices into
     * points, lines, and triangles and breaks them down into fragments, it will then
     * ask the fragment shader for the final color of each fragment. The vertex and
     * fragment shaders cooperate together to generate the final image on the screen.
     */

    // Creating a New Program Object and Attaching Shaders
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        int programObjectId = GLES20.glCreateProgram();
        if (programObjectId == 0) {
            LogUtil.i(TAG, "Could not create new program");
            return 0;
        }

        /*
          attach our shaders Using glAttachShader(), we attach both our vertex shader and our fragment
          shader to the program object
         */

        GLES20.glAttachShader(programObjectId, vertexShaderId);
        GLES20.glAttachShader(programObjectId, fragmentShaderId);

        GLES20.glLinkProgram(programObjectId);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);

        /*
          We first create a new int array to hold the result. We then call glGetProgramiv(programObjectId,
          GLES20.GL_LINK_STATUS, linkStatus, 0) to store the result in this array. We’ll
          also check the program info log so that if something went wrong or if OpenGL
          has anything interesting to say about our program, we’ll see it in Android’s log
          output:
         */

        LogUtil.i(TAG, "Results of linking program:\n" + GLES20.glGetProgramInfoLog(programObjectId));

        /*
          We now need to check the link status: if it’s 0, that means that the link failed
          and we can’t use this program object, so we should delete it and return 0 to
          the calling code:
         */
        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(programObjectId);
            LogUtil.i(TAG, "Linking of program failed.");
            return 0;
        }

        return programObjectId;
    }

    public static void validateProgram(int programObjectId) {

        GLES20.glValidateProgram(programObjectId);
        int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        LogUtil.i(TAG, "Results of validating program: " + validateStatus[0]
                + "\nLog:" + GLES20.glGetProgramInfoLog(programObjectId));

    }

    /**
     * compileShader()
     * The compileShader(shaderCode) method takes in source code for a shader and
     * the shader’s type. The type can be GL_VERTEX_SHADER for a vertex shader, or
     * GL_FRAGMENT_SHADER for a fragment shader. If OpenGL was able to successfully
     * compile the shader, then this method will return the shader object
     * ID to the calling code. Otherwise it will return zero.
     *
     * compileVertexShader()
     * The compileVertexShader(shaderCode) method is a helper method that calls
     * compileShader() with shader type GL_VERTEX_SHADER.
     *
     * compileFragmentShader()
     * The compileVertexShader(shaderCode) method is a helper method that calls
     * compileShader() with shader type GL_FRAGMENT_SHADER.
     */

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
