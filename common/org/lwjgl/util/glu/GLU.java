// STUB(R.Chen): LWJGL 2 GLU — removed in LWJGL 3. TODO: use modern projection math directly.
package org.lwjgl.util.glu;

public class GLU {
    public static void gluPerspective(float fovY, float aspect, float zNear, float zFar) {
        // TODO(R.Chen): compute perspective matrix manually or via org.joml.Matrix4f
    }

    public static void gluLookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        // TODO(R.Chen): use modern look-at matrix
    }
}
