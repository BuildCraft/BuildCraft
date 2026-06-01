// STUB(R.Chen): Minecraft 1.12 ScaledResolution — removed in 1.20. TODO: use Window.getScaleFactor().
package net.minecraft.client.gui;

import net.minecraft.client.MinecraftClient;

public class ScaledResolution {
    private final int scaleFactor;
    private final int scaledWidth;
    private final int scaledHeight;

    public ScaledResolution(MinecraftClient mc) {
        this.scaleFactor = (int) mc.getWindow().getScaleFactor();
        this.scaledWidth = mc.getWindow().getScaledWidth();
        this.scaledHeight = mc.getWindow().getScaledHeight();
    }

    public int getScaleFactor() { return scaleFactor; }
    public int getScaledWidth() { return scaledWidth; }
    public int getScaledHeight() { return scaledHeight; }
    public double getScaledWidth_double() { return scaledWidth; }
    public double getScaledHeight_double() { return scaledHeight; }
}
