// STUB(R.Chen): Minecraft 1.12 GLAllocation — removed. TODO: use MemoryUtil or GlStateManager directly.
package net.minecraft.client.renderer;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

public class GLAllocation {
    public static FloatBuffer createDirectFloatBuffer(int capacity) {
        return BufferUtils.createFloatBuffer(capacity);
    }
    public static int generateDisplayLists(int range) { return 0; }
}
