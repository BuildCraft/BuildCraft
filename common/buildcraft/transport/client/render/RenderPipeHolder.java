package buildcraft.transport.client.render;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.api.transport.neptune.IPluggableDynamicRenderer;
import buildcraft.api.transport.neptune.PipePluggable;

import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.tile.TilePipeHolder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RenderPipeHolder extends FastTESR<TilePipeHolder> {

    @Override
    public void renderTileEntityFast(TilePipeHolder pipe, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("pipe");

        Minecraft.getMinecraft().mcProfiler.startSection("wire");
        renderWire(pipe, x, y, z, vb);

        Minecraft.getMinecraft().mcProfiler.endStartSection("pluggable");
        renderPluggables(pipe, x, y, z, partialTicks, vb);

        Minecraft.getMinecraft().mcProfiler.endStartSection("contents");
        renderContents(pipe, x, y, z, partialTicks, vb);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    private static void renderWire(TilePipeHolder pipe, double x, double y, double z, VertexBuffer vb) {
        BiConsumer<AxisAlignedBB, MapColor> renderAABB = (AABB, mapColor) -> {
            GlStateManager.disableTexture2D();
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();
            int colorValue = mapColor.colorValue;
            float r = ((colorValue >> 16) & 0xFF) / 255.0F;
            float g = ((colorValue >> 8) & 0xFF) / 255.0F;
            float b = ((colorValue >> 0) & 0xFF) / 255.0F;
            GlStateManager.color(r, g, b);
            vertexbuffer.setTranslation(x, y, z);
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_NORMAL);
            vertexbuffer.pos(AABB.minX, AABB.maxY, AABB.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
            vertexbuffer.pos(AABB.maxX, AABB.maxY, AABB.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
            vertexbuffer.pos(AABB.maxX, AABB.minY, AABB.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
            vertexbuffer.pos(AABB.minX, AABB.minY, AABB.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
            vertexbuffer.pos(AABB.minX, AABB.minY, AABB.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
            vertexbuffer.pos(AABB.maxX, AABB.minY, AABB.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
            vertexbuffer.pos(AABB.maxX, AABB.maxY, AABB.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
            vertexbuffer.pos(AABB.minX, AABB.maxY, AABB.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
            vertexbuffer.pos(AABB.minX, AABB.minY, AABB.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.maxX, AABB.minY, AABB.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.maxX, AABB.minY, AABB.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.minX, AABB.minY, AABB.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.minX, AABB.maxY, AABB.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.maxX, AABB.maxY, AABB.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.maxX, AABB.maxY, AABB.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.minX, AABB.maxY, AABB.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.minX, AABB.minY, AABB.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.minX, AABB.maxY, AABB.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.minX, AABB.maxY, AABB.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.minX, AABB.minY, AABB.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.maxX, AABB.minY, AABB.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.maxX, AABB.maxY, AABB.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.maxX, AABB.maxY, AABB.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
            vertexbuffer.pos(AABB.maxX, AABB.minY, AABB.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
            tessellator.draw();
            vertexbuffer.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.enableTexture2D();
            GlStateManager.resetColor();
        };
        pipe.getWireManager().wiresByPart.forEach((enumWirePart, enumDyeColor) -> {
            renderAABB.accept(enumWirePart.boundingBox, enumDyeColor.getMapColor());
        });
    }

    private static void renderPluggables(TilePipeHolder pipe, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable plug = pipe.getPluggable(face);
            if (plug == null) {
                continue;
            }
            IPluggableDynamicRenderer dynRenderer = plug.getDynamicRenderer();
            if (dynRenderer == null) {
                continue;
            }
            dynRenderer.render(x, y, z, partialTicks, vb);
        }
    }

    private void renderContents(TilePipeHolder pipe, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        Pipe p = pipe.getPipe();
        if (p != null && p.flow != null) {
            if (p.flow instanceof PipeFlowItems) {
                PipeFlowRendererItems.INSTANCE.render((PipeFlowItems) p.flow, x, y, z, partialTicks, vb);
            }
        }
    }
}
