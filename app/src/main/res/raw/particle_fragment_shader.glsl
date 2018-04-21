precision mediump float;

uniform sampler2D u_TextureUnit;
varying vec3 v_Color;
varying float v_ElapsedTime;
void main()
{
    float xDistance = 0.5 - gl_PointCoord.x;
    float yDistance = 0.5 - gl_PointCoord.y;
    float distanceFromCenter = sqrt(xDistance * xDistance + yDistance * yDistance);

   /*Each point will be rendered with fragments that range from 0 to 1 on each axis
     relative to gl_PointCoord,so that places the center of the point at (0.5, 0.5),
     with 0.5 units of room on each side. In other words, we can say that the radius
     of the point is also 0.5. To draw a circle, all we need to do is draw only the
     fragments that lie within that radius.*/

   // TODO: We change particle shape from here
    if (distanceFromCenter > 0.3) {
        discard;
    } else {
        gl_FragColor = vec4(v_Color / v_ElapsedTime, 1.0) ;

        /*
        gl_FragColor = vec4(v_Color / v_ElapsedTime, 1.0) *
          texture2D(u_TextureUnit, gl_PointCoord);
        */

        // TIPS:
        // If you want to use texture ie texture.png then use the above code and discard
        // fragment if distanceFromCenter is greater then 0.5 in particle_fragment_shader.glsl
    }

    /* This is a somewhat expensive way of drawing a point as a circle, but it works.
       The way this works is that for each fragment, we calculate the distance to
       the center of the point with Pythagoras’s theorem.3 If that distance is greater
       than the radius of 0.5, then the current fragment is not part of the circle and
       we use the special keyword discard to tell OpenGL to forget about this fragment.*/

    /* This will draw a texture on the point using gl_PointCoord for the texture coordinates.
       The texture color will be multiplied with the point color, so the points
       will be colored the same way as before.*/

}