package com.juggrnaut.livewallpaper.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextResourceReader {
    // we’ve written the code for our shaders, the next step is to load them
    // into memory.

    //We’ve defined a method to read in text from a resource, readTextFileFromResource().
    //The way this will work is that we’ll call readTextFileFromResource() from our code,
    //and we’ll pass in the current Android context and the resource ID. The Android
    //context is required in order to access the resources. For example, to read in
    //the vertex shader, we might call the method as follows: readTextFileFromResource(
    //this.context, R.raw.simple_fragment_shader).
    public static String readTextFileFromResource(Context context, int resourceId) {
        StringBuilder body = new StringBuilder();
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body.toString();
    }

}
