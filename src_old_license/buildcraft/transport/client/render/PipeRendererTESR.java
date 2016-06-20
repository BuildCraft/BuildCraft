/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.client.render;

import com.google.common.base.Throwables;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.client.model.PipeModelCacheWire;
import buildcraft.transport.client.model.PipeModelCacheWire.PipeWireKey;

public class PipeRendererTESR extends TileEntitySpecialRenderer<TileGenericPipe> {
    public PipeRendererTESR() {}

    @SuppressWarnings("unchecked")
    @Override
    public void renderTileEntityAt(TileGenericPipe pipe, double x, double y, double z, float f, int argumentthatisalwaysminusone) {
        if (BuildCraftCore.render == RenderMode.NoDynamic) {
            return;
        }

        if (pipe.pipe == null) return;

        if (pipe.pipe.container == null) return;
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("pipe");

        Minecraft.getMinecraft().mcProfiler.startSection("wire");
        renderWire(pipe, x, y, z);
        Minecraft.getMinecraft().mcProfiler.endStartSection("pluggable");
        renderPluggables(pipe, x, y, z);
        Minecraft.getMinecraft().mcProfiler.endStartSection("contents");

        if (pipe.pipe.transport != null) {
            try {
                PipeTransportRenderer renderer = PipeTransportRenderer.RENDERER_MAP.get(pipe.pipe.transport.getClass());
                if (renderer != null) {
                    renderer.render(pipe.pipe, x, y, z, f);
                }
            } catch (Throwable t) {
                BCLog.logger.warn("A crash! Oh no!", t);
                throw Throwables.propagate(t);
            } finally {
                Minecraft.getMinecraft().mcProfiler.endSection();
                Minecraft.getMinecraft().mcProfiler.endSection();
                Minecraft.getMinecraft().mcProfiler.endSection();
            }
        }
    }

    private static void renderWire(TileGenericPipe pipe, double x, double y, double z) {
        PipeWireKey key = new PipeWireKey(pipe.renderState);

        RenderHelper.disableStandardItemLighting();
        TileEntityRendererDispatcher.instance.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        PipeModelCacheWire.cacheAll.renderDisplayList(key);
        GL11.glPopMatrix();

        RenderHelper.enableStandardItemLighting();
    }

    private void renderPluggables(TileGenericPipe pipe, double x, double y, double z) {
        TileEntityRendererDispatcher.instance.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        for (EnumFacing direction : EnumFacing.VALUES) {
            PipePluggable pluggable = pipe.getPipePluggable(direction);
            if (pluggable != null && pluggable.getDynamicRenderer() != null) {
                pluggable.getDynamicRenderer().renderDynamicPluggable(pipe.getPipe(), direction, pluggable, x, y, z);
            }
        }
    }

    public boolean isOpenOrientation(PipeRenderState state, EnumFacing direction) {
        int connections = 0;

        EnumFacing targetOrientation = null;

        for (EnumFacing o : EnumFacing.VALUES) {
            if (state.pipeConnectionMatrix.isConnected(o)) {

                connections++;

                if (connections == 1) {
                    targetOrientation = o;
                }
            }
        }

        if (connections > 1 || targetOrientation == null) {
            return false;
        }

        return targetOrientation.getOpposite() == direction;
    }

}
