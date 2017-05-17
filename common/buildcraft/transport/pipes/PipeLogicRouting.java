/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.lib.TileBuffer;
import buildcraft.transport.Pipe;
import buildcraft.transport.TravelingItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public abstract class PipeLogicRouting {

    protected final Pipe<?> pipe;
    protected boolean lastPower = false;

    public PipeLogicRouting(Pipe<?> pipe) {
        this.pipe = pipe;
    }

    public void switchOnRedstone() {
        boolean currentPower = pipe.container.getWorld().isBlockIndirectlyGettingPowered(pipe.container.getPos()) > 0;

        if (currentPower != lastPower) {
            switchPosition(null);
            lastPower = currentPower;
        }
    }

    protected void switchPosition(TravelingItem item) {
        int meta = pipe.container.getBlockMetadata();

        for (int i = meta + 1; i <= meta + 6; ++i) {
            EnumFacing facing = EnumFacing.getFront(i % 6);
            if (setFacing(facing)) {
                return;
            }
        }
    }

    protected boolean isValidFacing(EnumFacing side) {
        if (!pipe.container.isPipeConnected(side)) {
            return false;
        }

        if (side == null) {
            return true;
        }

        TileBuffer[] tileBuffer = pipe.container.getTileCache();

        if (tileBuffer == null) {
            return true;
        } else if (!tileBuffer[side.ordinal()].exists()) {
            return true;
        }

        TileEntity tile = tileBuffer[side.ordinal()].getTile();
        return isValidOutputTile(tile);
    }

    protected boolean isValidOutputTile(TileEntity tile) {
        return !(tile instanceof IInventory && ((IInventory) tile).getInventoryStackLimit() == 0) && isValidConnectingTile(tile);
    }

    protected abstract boolean isValidConnectingTile(TileEntity tile);

    public void initialize() {
        lastPower = pipe.container.getWorld().isBlockIndirectlyGettingPowered(pipe.container.getPos()) > 0;
    }

    public void onBlockPlaced() {
        setFacing(null);
        switchPosition(null);
    }

    public boolean setFacing(EnumFacing facing) {
        IBlockState state = pipe.container.getWorld().getBlockState(pipe.container.getPos());
        int ordinal = facing == null ? 6 : facing.ordinal();
        int oldOrdinal = state.getValue(BuildCraftProperties.GENERIC_PIPE_DATA);

        if (ordinal != oldOrdinal && isValidFacing(facing)) {
            pipe.container.getWorld().setBlockState(pipe.container.getPos(), state.withProperty(BuildCraftProperties.GENERIC_PIPE_DATA, ordinal));
            pipe.container.scheduleRenderUpdate();
            return true;
        }
        return false;
    }

    public boolean blockActivated(EntityPlayer entityplayer, EnumPipePart side) {
        Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
        if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, pipe.container.getPos())) {
            if (side == EnumPipePart.CENTER || !pipe.getContainer().isPipeConnected(side.face)) {
                switchPosition(null);
            } else {
                setFacing(side.face);
            }
            pipe.container.scheduleRenderUpdate();
            ((IToolWrench) equipped).wrenchUsed(entityplayer, pipe.container.getPos());

            return true;
        }

        return false;
    }

    public EnumFacing getOutputDirection() {
        return EnumFacing.getFront(pipe.container.getBlockMetadata());
    }

    public boolean outputOpen(EnumFacing to) {
        return to.ordinal() == pipe.container.getBlockMetadata();
    }
}
