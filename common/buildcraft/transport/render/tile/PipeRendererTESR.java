/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.render.tile;

import com.google.common.base.Throwables;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.api.core.BCLog;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.*;

public class PipeRendererTESR extends TileEntitySpecialRenderer<TileGenericPipe> {
    public PipeRendererTESR() {}

    @SuppressWarnings("unchecked")
    @Override
    public void renderTileEntityAt(TileGenericPipe pipe, double x, double y, double z, float f, int argumentthatisalwaysminusone) {
        if (BuildCraftCore.render == RenderMode.NoDynamic) {
            return;
        }

        if (pipe.pipe == null) {
            return;
        }

        renderPluggables(pipe, x, y, z);

        IPipeTile.PipeType pipeType = pipe.getPipeType();

        try {
            if (pipeType == IPipeTile.PipeType.ITEM) {
                PipeRendererItems.renderItemPipe((Pipe<PipeTransportItems>) pipe.pipe, x, y, z, f);
            } else if (pipeType == IPipeTile.PipeType.FLUID) {
                PipeRendererFluids.renderFluidPipe((Pipe<PipeTransportFluids>) pipe.pipe, x, y, z);
            } else if (pipeType == IPipeTile.PipeType.POWER) {
                PipeRendererPower.renderPowerPipe((Pipe<PipeTransportPower>) pipe.pipe, x, y, z);
            } /* else if (pipeType == PipeType.STRUCTURE) { // no object to render in a structure pipe; } */
        } catch (Throwable t) {
            BCLog.logger.warn("A crash! Oh no!", t);
            throw Throwables.propagate(t);
        }
    }

    private void renderPluggables(TileGenericPipe pipe, double x, double y, double z) {
        TileEntityRendererDispatcher.instance.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

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

        if (connections > 1 || connections == 0) {
            return false;
        }

        return targetOrientation.getOpposite() == direction;
    }

}
