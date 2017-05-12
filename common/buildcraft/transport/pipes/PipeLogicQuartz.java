/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.lib.TileBuffer;
import buildcraft.transport.Pipe;
import buildcraft.transport.TravelingItem;

public abstract class PipeLogicQuartz extends PipeLogicIron{
    public PipeLogicQuartz(Pipe<?> pipe) {

        super(pipe);
    }

    private EnumFacing getInverse(EnumFacing facing){
        switch(facing){
            case DOWN:
                return EnumFacing.UP;
            case UP:
                return EnumFacing.DOWN;
            case EAST:
                return EnumFacing.WEST;
            case WEST:
                return EnumFacing.EAST;
            case NORTH:
                return EnumFacing.SOUTH;
            case SOUTH:
                return EnumFacing.NORTH;
            default:
                return null;
        }
    }

    public void switchPosition(TravelingItem item) {
        int meta = pipe.container.getBlockMetadata();
        EnumFacing bounceback = null;
        if (item != null) {
            bounceback = getInverse(item.input);
        }


        for (int i = meta + 1; i <= meta + 6; ++i) {
            EnumFacing facing = EnumFacing.getFront(i % 6);
            if ((bounceback != null && !facing.equals(bounceback) || bounceback == null) && setFacing(facing)) {
                return;
            }
        }
    }

    public void onBlockPlaced() {
        setFacing(null);
        switchPosition(null);
    }
}
