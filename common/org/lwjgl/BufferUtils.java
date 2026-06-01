// STUB(R.Chen): LWJGL 2 BufferUtils → use java.nio.ByteBuffer.allocateDirect() directly in LWJGL 3.
package org.lwjgl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BufferUtils {
    public static ByteBuffer createByteBuffer(int size) { return ByteBuffer.allocateDirect(size); }
    public static FloatBuffer createFloatBuffer(int size) { return ByteBuffer.allocateDirect(size * 4).asFloatBuffer(); }
    public static IntBuffer createIntBuffer(int size) { return ByteBuffer.allocateDirect(size * 4).asIntBuffer(); }
}
