package buildcraft.builders.render;

import net.minecraft.client.Minecraft;

import buildcraft.builders.TileBuilder;
import buildcraft.core.render.RenderBuilder;

public class RenderBuilderTile extends RenderBuilder<TileBuilder> {
    private static final float Z_OFFSET = 2049 / 2048.0F;

    @Override
    public void renderTileEntityAt(TileBuilder builder, double x, double y, double z, float f, int arg) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("builder");

        super.renderTileEntityAt(builder, x, y, z, f, arg);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
        //
        // bindTexture(TextureMap.locationBlocksTexture);
        // RenderEntityBlock.RenderInfo renderBox = new RenderEntityBlock.RenderInfo();
        //
        // GL11.glPushMatrix();
        //
        // GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        // GL11.glScalef(Z_OFFSET, Z_OFFSET, Z_OFFSET);
        // GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        //
        // renderBox.setRenderSingleSide(1);
        // renderBox.texture = BuildCraftBuilders.builderBlock.blockTopOn;
        // renderBox.light = builder.buildersInAction.size() > 0 ? 15 : 0;
        // RenderEntityBlock.INSTANCE.renderBlock(renderBox);
        //
        // GL11.glPopMatrix();
    }
}
