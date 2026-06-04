// STUB(R.Chen): Minecraft 1.12 TileEntityRendererDispatcher — renamed to BlockEntityRenderDispatcher in 1.20.
package net.minecraft.client.renderer.tileentity;

import net.minecraft.block.entity.BlockEntity;

public class TileEntityRendererDispatcher {
    public static final TileEntityRendererDispatcher instance = new TileEntityRendererDispatcher();

    public void preDrawBatch() {}
    public void drawBatch(float partialTicks) {}

    public void render(BlockEntity blockEntity, double x, double y, double z, float partialTicks) {
        // TODO(R.Chen): use MinecraftClient.getInstance().getBlockEntityRenderDispatcher().render(...)
    }

    public void render(BlockEntity blockEntity, double x, double y, double z, float partialTicks, int destroyStage) {
        // TODO(R.Chen): migrate to BlockEntityRenderDispatcher
    }
}
