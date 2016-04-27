package buildcraft.core.tile;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.core.Box;
import buildcraft.core.LaserData;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.tile.TileMarkerBase;

public class TileMarkerVolume extends TileMarkerBase<TileMarkerVolume> implements ITileAreaProvider {
    public static final int NET_SIGNALS_ON = 10;
    public static final int NET_SIGNALS_OFF = 11;
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
    public boolean isActiveForRender() {
        return showSignals || box != null;
    }

    @Override
    protected void onConnect(TileMarkerVolume other) {
        regenBox();
    }

    @Override
    protected void onDisconnect(TileMarkerVolume other) {
        regenBox();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        showSignals = compound.getBoolean("showSignals");
        regenBox();
    }

    private void regenBox() {
        boolean before = isActiveForRender();
        if (allConnected.size() > 0) {
            box = new Box(getPos(), getPos());
            for (TileMarkerVolume connectedTo : gatherAllConnections()) {
                box.extendToEncompass(connectedTo.getPos());
            }
        } else {
            box = null;
        }
        if (before != isActiveForRender()) {
            redrawBlock();
        }
    }

    public void updateSignals() {
        boolean before = isActiveForRender();
        if (!worldObj.isRemote) {
            if (worldObj.isBlockLoaded(getPos())) {
                showSignals = worldObj.isBlockIndirectlyGettingPowered(pos) > 0;
                sendNetworkUpdate(showSignals ? NET_SIGNALS_ON : NET_SIGNALS_OFF);
            }
        } else {
            if (showSignals) {
                signals = null;// TODO!
            } else {
                signals = null;
            }
        }
        if (before != isActiveForRender()) {
            redrawBlock();
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
        for (TileMarkerVolume connectedTo : gatherAllConnections()) {
            worldObj.destroyBlock(connectedTo.getPos(), true);
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

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeBoolean(showSignals);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) {
        super.readPayload(id, buffer, side);
        if (side == Side.CLIENT) {
            if (id == NET_SIGNALS_ON) {
                showSignals = true;
                updateSignals();
            } else if (id == NET_SIGNALS_OFF) {
                showSignals = false;
                updateSignals();
            } else if (id == NET_RENDER_DATA) {
                showSignals = buffer.readBoolean();
                updateSignals();
            }
        }
    }
}
