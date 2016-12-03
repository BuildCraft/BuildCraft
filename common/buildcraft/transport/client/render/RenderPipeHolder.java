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
import buildcraft.transport.wire.EnumWireBetween;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.animation.FastTESR;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class RenderPipeHolder extends FastTESR<TilePipeHolder> {
    private static Map<EnumDyeColor, SpriteHolderRegistry.SpriteHolder> wireSprites = new EnumMap<>(EnumDyeColor.class);
    public static Map<Triple<AxisAlignedBB, EnumDyeColor, Boolean>, Integer> wiresRenderingCache = new HashMap<>();

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

    private static void renderWire(boolean powered, int light1, int light2, double[][] poses, double[][] texes, AxisAlignedBB bb, EnumDyeColor color) {
        Triple<AxisAlignedBB, EnumDyeColor, Boolean> key = Triple.of(bb, color, powered);
        if(!wiresRenderingCache.containsKey(key)) {
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vb = tessellator.getBuffer();
            vb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            int index = GlStateManager.glGenLists(1);
            GlStateManager.glNewList(index, GL11.GL_COMPILE);
            TextureAtlasSprite sprite = wireSprites.get(color).getSprite();
            if(powered) {
                light1 = light2 = 240;
            }
            int c = powered ? 255 : 120;
            for(int i = 0; i < 4 * 6; i++) {
                double[] pos = poses[i];
                double[] tex = texes[i];
                vb.pos(pos[0], pos[1], pos[2]).color(c, c, c, 255).tex(sprite.getInterpolatedU(tex[0]), sprite.getInterpolatedV(tex[1])).lightmap(light1, light2).endVertex();
            }
            tessellator.draw();
            GL11.glEndList();
            wiresRenderingCache.put(key, index);
        }
        GlStateManager.callList(wiresRenderingCache.get(key));
    }

    private static void renderWires(TilePipeHolder pipe, double x, double y, double z, VertexBuffer vb) {
        int combinedLight = pipe.getWorld().getCombinedLight(pipe.getPipePos(), 0);
        int light1 = combinedLight >> 16 & 65535;
        int light2 = combinedLight & 65535;
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        for(Map.Entry<EnumWirePart, EnumDyeColor> partColor : pipe.getWireManager().parts.entrySet()) {
            EnumWirePart part = partColor.getKey();
            EnumDyeColor color = partColor.getValue();
            renderWire(pipe.getWireManager().isPowered(part), light1, light2, part.poses, part.texes, part.boundingBox, color);
        }
        for(Map.Entry<EnumWireBetween, EnumDyeColor> betweenColor : pipe.getWireManager().betweens.entrySet()) {
            EnumWireBetween between = betweenColor.getKey();
            EnumDyeColor color = betweenColor.getValue();
            renderWire(pipe.getWireManager().isPowered(between.parts[0]), light1, light2, between.poses, between.texes, between.boundingBox, color);
        }
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
    }

    private static void renderPluggables(TilePipeHolder pipe, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        for(EnumFacing face : EnumFacing.VALUES) {
            PipePluggable plug = pipe.getPluggable(face);
            if(plug == null) {
                continue;
            }
            IPluggableDynamicRenderer dynRenderer = plug.getDynamicRenderer();
            if(dynRenderer == null) {
                continue;
            }
            dynRenderer.render(x, y, z, partialTicks, vb);
        }
    }

    private void renderContents(TilePipeHolder pipe, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        Pipe p = pipe.getPipe();
        if(p != null && p.flow != null) {
            if(p.flow instanceof PipeFlowItems) {
                PipeFlowRendererItems.INSTANCE.render((PipeFlowItems) p.flow, x, y, z, partialTicks, vb);
            } else if(p.flow instanceof PipeFlowFluids) {

                TilePipeHolder pipe2 = BCLibProxy.getProxy().getServerTile(pipe);

                if(pipe2.getPipe() != null && pipe2.getPipe().flow instanceof PipeFlowFluids) {
                    p = pipe2.getPipe();
                }

                PipeFlowRendererFluids.INSTANCE.render((PipeFlowFluids) p.flow, x, y, z, partialTicks, vb);
            }
        }
    }
}
