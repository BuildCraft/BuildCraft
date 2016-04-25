package buildcraft.core.tile;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.core.Box;
import buildcraft.core.LaserData;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.tile.TileMarkerBase;

public class TileMarkerVolume extends TileMarkerBase<TileMarkerVolume> implements ITileAreaProvider {
    public static final Map<BlockPos, TileMarkerVolume> VOLUME_CACHE = new HashMap<>();

    public Box box = null;

    private boolean showSignals = false;
    public LaserData[] lasers = null;
    public LaserData[] signals = null;

    @Override
    public Map<BlockPos, TileMarkerVolume> getCache() {
        return VOLUME_CACHE;
    }

    @Override
    public boolean canConnectTo(TileMarkerVolume other) {
        if (allConnected.size() >= 3) return false;
        Axis diff = getDiff(other);
        if (diff == null) return false;
        for (BlockPos pos : allConnected) {
            if (diff == PositionUtil.getAxisDifference(getPos(), pos)) return false;
        }
        return true;
    }

    private Axis getDiff(TileMarkerVolume other) {
        return other == null ? null : PositionUtil.getAxisDifference(getPos(), other.getPos());
    }

    @Override
    public TileMarkerVolume getAsType() {
        return this;
    }

    @Override
    protected void onConnect(TileMarkerVolume other) {
        regenBox();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        regenBox();
    }
    
    @Override
    public void onLoad() {
        super.onLoad();
        updateSignals();
    }

    private void regenBox() {
        if (allConnected.size() > 0) {
            box = new Box(getPos(), getPos());
            for (BlockPos connectedTo : allConnected) {
                box.extendToEncompass(connectedTo);
            }
        } else {
            box = null;
        }
    }

    public void updateSignals() {
        if (!worldObj.isRemote) {
            showSignals = worldObj.isBlockIndirectlyGettingPowered(pos) > 0;
            sendNetworkUpdate();
        }
    }

    // ITileAreaProvider

    @Override
    public BlockPos min() {
        return box == null ? getPos() : box.min();
    }

    @Override
    public BlockPos max() {
        return box == null ? getPos() : box.max();
    }

    @Override
    public void removeFromWorld() {
        if (worldObj.isRemote) return;
        for (BlockPos connectedTo : allConnected) {
            worldObj.destroyBlock(connectedTo, true);
        }
    }

    @Override
    public boolean isValidFromLocation(BlockPos pos) {
        if (box == null) regenBox();
        if (box == null) return false;
        if (box.contains(pos)) return false;
        for (BlockPos p : PositionUtil.getCorners(box.min(), box.max())) {
            if (PositionUtil.isNextTo(p, pos)) return true;
        }
        return false;
    }
}
