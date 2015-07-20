/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.lib.TileBuffer;
import buildcraft.transport.Pipe;

public abstract class PipeLogicWood {

    protected final Pipe<?> pipe;

    public PipeLogicWood(Pipe<?> pipe) {
        this.pipe = pipe;
    }

    private void switchSource() {
        int meta = pipe.container.getBlockMetadata();
        EnumFacing newFacing = null;

        for (int i = meta + 1; i <= meta + 6; ++i) {
            EnumFacing facing = EnumFacing.getFront(i % 6);
            if (isValidFacing(facing)) {
                newFacing = facing;
                break;
            }
        }
        int ordinal = -1;

        if (newFacing == null) {
            ordinal = 6;// The old ForgeDirection.UNKNOWN ordinal
        } else {
            ordinal = newFacing.ordinal();
        }

        if (ordinal != meta) {
            IBlockState state = pipe.container.getWorld().getBlockState(pipe.container.getPos());
            pipe.container.getWorld().setBlockState(pipe.container.getPos(), state.withProperty(BuildCraftProperties.GENERIC_PIPE_DATA, ordinal));
            pipe.container.scheduleRenderUpdate();
        }
    }

    private void switchSourceIfNeeded() {
        int meta = pipe.container.getBlockMetadata();

        if (meta > 5) {
            switchSource();
        } else {
            EnumFacing facing = EnumFacing.getFront(meta);
            if (!isValidFacing(facing)) {
                switchSource();
            }
        }
    }

    private boolean isValidFacing(EnumFacing side) {
        TileBuffer[] tileBuffer = pipe.container.getTileCache();
        if (tileBuffer == null) {
            return true;
        }

        if (!tileBuffer[side.ordinal()].exists()) {
            return true;
        }

        if (pipe.container.hasBlockingPluggable(side)) {
            return false;
        }

        TileEntity tile = tileBuffer[side.ordinal()].getTile();
        return isValidConnectingTile(tile);
    }

    protected abstract boolean isValidConnectingTile(TileEntity tile);

    public void initialize() {
        if (!pipe.container.getWorld().isRemote) {
            switchSourceIfNeeded();
        }
    }

    public boolean blockActivated(EntityPlayer entityplayer) {
        Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
        if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, pipe.container.getPos())) {
            switchSource();
            ((IToolWrench) equipped).wrenchUsed(entityplayer, pipe.container.getPos());
            return true;
        }

        return false;
    }

    public void onNeighborBlockChange(int blockId) {
        if (!pipe.container.getWorld().isRemote) {
            switchSourceIfNeeded();
        }
    }
}
