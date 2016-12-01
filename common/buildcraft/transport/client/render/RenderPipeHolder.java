package buildcraft.transport.client.render;

import buildcraft.api.transport.neptune.EnumWirePart;
import buildcraft.api.transport.neptune.IPluggableDynamicRenderer;
import buildcraft.api.transport.neptune.PipePluggable;
import buildcraft.lib.BCLibProxy;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.tile.TilePipeHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.animation.FastTESR;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class RenderPipeHolder extends FastTESR<TilePipeHolder> {

    private static Map<EnumDyeColor, SpriteHolderRegistry.SpriteHolder> wireSprites = new EnumMap<>(EnumDyeColor.class);

    static {
        for(EnumDyeColor color : EnumDyeColor.values()) {
            wireSprites.put(color, SpriteHolderRegistry.getHolder("buildcrafttransport:wires/" + color.getName()));
        }
    }

    @Override
    public void renderTileEntityFast(TilePipeHolder pipe, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("pipe");

        Minecraft.getMinecraft().mcProfiler.startSection("wire");
        renderWires(pipe, x, y, z, vb);

        Minecraft.getMinecraft().mcProfiler.endStartSection("pluggable");
        renderPluggables(pipe, x, y, z, partialTicks, vb);

        Minecraft.getMinecraft().mcProfiler.endStartSection("contents");
        renderContents(pipe, x, y, z, partialTicks, vb);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    private static void renderWires(TilePipeHolder pipe, double x, double y, double z, VertexBuffer vb) {
        BiConsumer<Pair<AxisAlignedBB, EnumWirePart>, EnumDyeColor> renderAABB = (wireInfo, color) -> {
            EnumWirePart part = wireInfo.getRight();
            AxisAlignedBB bb = wireInfo.getLeft();
            TextureAtlasSprite sprite = wireSprites.get(color).getSprite();
            Vec3d size = new Vec3d(bb.maxX - bb.minX, bb.maxY - bb.minY, bb.maxZ - bb.minZ).scale(32);
            int combinedLight = pipe.getWorld().getCombinedLight(pipe.getPipePos(), 0);
            int light1 = combinedLight >> 16 & 65535;
            int light2 = combinedLight & 65535;
            if(pipe.getWireManager().isPowered(part)) {
                light1 = light2 = 240;
            }
            float c = pipe.getWireManager().isPowered(part) ? 1 : 0.5F;
            vb.setTranslation(x, y, z);
            vb.pos(bb.minX, bb.maxY, bb.minZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(0/*      */), sprite.getInterpolatedV(0/*      */)).lightmap(light1, light2).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.minZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(size.xCoord), sprite.getInterpolatedV(0/*      */)).lightmap(light1, light2).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.minZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(size.xCoord), sprite.getInterpolatedV(size.yCoord)).lightmap(light1, light2).endVertex();
            vb.pos(bb.minX, bb.minY, bb.minZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(0/*      */), sprite.getInterpolatedV(size.yCoord)).lightmap(light1, light2).endVertex();
            vb.pos(bb.minX, bb.minY, bb.maxZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(0/*      */), sprite.getInterpolatedV(0/*      */)).lightmap(light1, light2).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.maxZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(size.xCoord), sprite.getInterpolatedV(0/*      */)).lightmap(light1, light2).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(size.xCoord), sprite.getInterpolatedV(size.yCoord)).lightmap(light1, light2).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.maxZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(0/*      */), sprite.getInterpolatedV(size.yCoord)).lightmap(light1, light2).endVertex();
            vb.pos(bb.minX, bb.minY, bb.minZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(0/*      */), sprite.getInterpolatedV(0/*      */)).lightmap(light1, light2).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.minZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(size.xCoord), sprite.getInterpolatedV(0/*      */)).lightmap(light1, light2).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.maxZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(size.xCoord), sprite.getInterpolatedV(size.zCoord)).lightmap(light1, light2).endVertex();
            vb.pos(bb.minX, bb.minY, bb.maxZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(0/*      */), sprite.getInterpolatedV(size.zCoord)).lightmap(light1, light2).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.maxZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(0/*      */), sprite.getInterpolatedV(0/*      */)).lightmap(light1, light2).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(size.xCoord), sprite.getInterpolatedV(0/*      */)).lightmap(light1, light2).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.minZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(size.xCoord), sprite.getInterpolatedV(size.zCoord)).lightmap(light1, light2).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.minZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(0/*      */), sprite.getInterpolatedV(size.zCoord)).lightmap(light1, light2).endVertex();
            vb.pos(bb.minX, bb.minY, bb.maxZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(0/*      */), sprite.getInterpolatedV(0/*      */)).lightmap(light1, light2).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.maxZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(size.yCoord), sprite.getInterpolatedV(0/*      */)).lightmap(light1, light2).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.minZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(size.yCoord), sprite.getInterpolatedV(size.zCoord)).lightmap(light1, light2).endVertex();
            vb.pos(bb.minX, bb.minY, bb.minZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(0/*      */), sprite.getInterpolatedV(size.zCoord)).lightmap(light1, light2).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.minZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(0/*      */), sprite.getInterpolatedV(0/*      */)).lightmap(light1, light2).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.minZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(size.yCoord), sprite.getInterpolatedV(0/*      */)).lightmap(light1, light2).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(size.yCoord), sprite.getInterpolatedV(size.zCoord)).lightmap(light1, light2).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.maxZ).color(c, c, c, 1).tex(sprite.getInterpolatedU(0/*      */), sprite.getInterpolatedV(size.zCoord)).lightmap(light1, light2).endVertex();
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        };
        pipe.getWireManager().parts.forEach((part, color) -> renderAABB.accept(Pair.of(part.boundingBox, part), color));
        pipe.getWireManager().betweens.forEach((between, color) -> renderAABB.accept(Pair.of(between.boundingBox, between.parts[0]), color));
        vb.setTranslation(0, 0, 0);
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
            } else if (p.flow instanceof PipeFlowFluids) {

                TilePipeHolder pipe2 = BCLibProxy.getProxy().getServerTile(pipe);

                if (pipe2.getPipe() != null && pipe2.getPipe().flow instanceof PipeFlowFluids) {
                    p = pipe2.getPipe();
                }

                PipeFlowRendererFluids.INSTANCE.render((PipeFlowFluids) p.flow, x, y, z, partialTicks, vb);
            }
        }
    }
}
