/*
We’ll storea direction vector and a creation time for each particle; with the
creation time,we can figure out how much time has elapsed since the particle
was created,and then we can use the elapsed time with the direction vector and
the position to figure out the particle’s current position. We’ll use a
floating-point number to store the time, with 0.0 representing when we began running our particle
system.
*/

/*
We’ll also need four attributes corresponding to the particle’s
properties: position,color, direction vector, and creation time.
*/

uniform mat4 u_Matrix;
uniform float u_Time;

attribute vec3 a_Position;
attribute vec3 a_Color;
attribute vec3 a_DirectionVector;
attribute float a_ParticleStartTime;

varying vec3 v_Color;
varying float v_ElapsedTime;

/*
We’llneed to use the color and the elapsed time in the fragment
shader as well, sowe’ve also created two varyings for these two variables
*/
void main()
{
    v_Color = a_Color;
    //We first send the color on to the fragment shader

    v_ElapsedTime = u_Time - a_ParticleStartTime;
    /* we calculate how much time has elapsed since this particle was
       created and send that on to the fragment shader as well*/

    float gravityFactor = v_ElapsedTime * v_ElapsedTime / 9.8;
    /* This will calculate an accelerating gravity factor by applying the gravitational
       acceleration formula and squaring the elapsed time; we also divide things by
       8 to dampen the effect. The number 8 is arbitrary: we could use any other
       number that also makes things look good on the screen. Now we need to apply
       the gravity to our current position. */

     /* Now we can see that each particle slows down as it moves upward, and
        eventually it starts falling back down toward earth. We can still improve the
        look: some of the darker particles overlap the brighter ones. */

    vec3 currentPosition = a_Position + (a_DirectionVector * v_ElapsedTime);
    currentPosition.y -= gravityFactor;

    gl_Position = u_Matrix * vec4(currentPosition, 1.0);
    /* To calculate the current position of the particle, we multiply the direction vector with the
       elapsed time and add that to the position. The more time elapses, the further
       the particle will go  */

    /* It’s important to ensure that we don’t accidentally mess up the w component
       when doing our math, so we use 3-component vectors to represent the position
       and the direction, converting to a full 4-component vector only when we need
       to multiply it with u_Matrix. This ensures that our math above only affects the
       x, y, and z components. */

    gl_PointSize = 20.0;
    /* We project the particle with the matrix, and
       since we’re rendering the particle as a point,
       we set the point size to 15 pixels.*/
}