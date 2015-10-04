/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.transport;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.api.core.EnumColor;
import buildcraft.api.transport.pluggable.PipePluggable;

public interface IPipeTile extends IInjectable {
    enum PipeType {
        ITEM,
        FLUID,
        POWER,
        STRUCTURE
    }

    PipeType getPipeType();

    World getWorld();

    int x();

    int y();

    int z();

    /** True if the pipe is connected to the block/pipe in the specific direction
     * 
     * @param with
     * @return true if connect */
    boolean isPipeConnected(EnumFacing with);

    Block getNeighborBlock(EnumFacing dir);

    TileEntity getNeighborTile(EnumFacing dir);

    IPipe getNeighborPipe(EnumFacing dir);

    IPipe getPipe();

    int getPipeColor();

    PipePluggable getPipePluggable(EnumFacing direction); // Now in IPluggableProvider

    boolean hasPipePluggable(EnumFacing direction); // Now in IPluggableProvider

    boolean hasBlockingPluggable(EnumFacing direction);

    void scheduleNeighborChange();

    void scheduleRenderUpdate();

    // For compatibility with BC 6.2.x and below
    int injectItem(ItemStack stack, boolean doAdd, EnumFacing from, EnumColor color);

    @Deprecated
    // Now in IInjectable
    int injectItem(ItemStack stack, boolean doAdd, EnumFacing from);
}
