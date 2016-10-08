package buildcraft.lib.client.render;

import java.lang.reflect.Field;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderUtil {
    private static final Field fldBuffer, fldIsDrawing;
    private static boolean manuallyStarted = false;

    static {
        try {
            fldBuffer = TileEntityRendererDispatcher.class.getDeclaredField("batchBuffer");
            fldBuffer.setAccessible(true);
            fldIsDrawing = TileEntityRendererDispatcher.class.getDeclaredField("drawingBatch");
            fldIsDrawing.setAccessible(true);
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    private static Tessellator getVbInternal() {
        try {
            return (Tessellator) fldBuffer.get(TileEntityRendererDispatcher.instance);
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    private static boolean getIsDrawningInternal() {
        try {
            return (boolean) fldIsDrawing.get(TileEntityRendererDispatcher.instance);
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    public static VertexBuffer getTesrBuffer() {
        Tessellator tess = getVbInternal();
        boolean isDrawing = getIsDrawningInternal();
        if (!isDrawing) {
            manuallyStarted = true;
            TileEntityRendererDispatcher.instance.preDrawBatch();
        }
        return tess.getBuffer();
    }

    public static void finishTesrBatch() {
        if (manuallyStarted) {
            TileEntityRendererDispatcher.instance.drawBatch(0);
            manuallyStarted = false;
        }
    }
}
