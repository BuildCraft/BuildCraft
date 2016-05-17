package buildcraft.core.tile;

import java.io.IOException;
import java.util.*;

import com.google.common.collect.ImmutableList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.core.BCCoreConfig;
import buildcraft.core.Box;
import buildcraft.core.LaserData;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.tile.MarkerCache;
import buildcraft.lib.tile.TileMarkerBase;

@Deprecated
public class TileMarkerVolume extends TileMarkerBase<TileMarkerVolume, MarkerConnectionVolume> implements ITileAreaProvider {
    @Deprecated
    public static class CacheVolume extends MarkerCache<TileMarkerVolume, MarkerConnectionVolume> {
        public CacheVolume() {
            super("bc:volume", MarkerConnectionVolume.LOADER);
        }

    }

    public static final int NET_SIGNALS_ON = 10;
    public static final int NET_SIGNALS_OFF = 11;
    public static final CacheVolume VOLUME_CACHE = new CacheVolume();

    public Box box = null;

    private boolean showSignals = false;
    public LaserData[] lasers = null;
    public LaserData[] signals = null;

    @Override
    public CacheVolume getCache() {
        return VOLUME_CACHE;
    }

    @Override
    public boolean canConnectTo(TileMarkerVolume other) {
        if (connected.size() >= 3) return false;
        EnumFacing directOffset = PositionUtil.getDirectFacingOffset(getPos(), other.getPos());
        if (directOffset == null) return false;

        if (!areBoxesAddable(other, directOffset.getAxis())) return false;
        // for (BlockPos alreadyConnected : connected.keySet()) {
        // EnumFacing offset = PositionUtil.getDirectFacingOffset(getPos(), alreadyConnected);
        // if (offset != null && offset.getAxis() == directOffset.getAxis()) return false;
        // }
        int diff = MathHelper.floor_double(Math.sqrt(other.getPos().distanceSq(getPos())));
        if (diff > BCCoreConfig.markerMaxDistance) return false;
        for (int i = 1; i < diff; i++) {
            BlockPos inBetween = getPos().offset(directOffset, i);
            TileMarkerVolume inBetweenTile = getCacheForSide().get(inBetween);
            if (inBetweenTile != null && inBetweenTile != other) {
                return false;
            }
        }
        return true;
    }

    private boolean areBoxesAddable(TileMarkerVolume other, Axis axis) {
        regenBox();
        other.regenBox();
        if (box == null) {
            return true;
        } else {
            Box otherBox = other.box;
            if (otherBox == null) otherBox = new Box(other);
            BlockPos thisSize = box.size();
            BlockPos otherSize = otherBox.size();
            int addX = (thisSize.getX() > 1 ? 1 : 0) + (otherSize.getX() > 1 ? 1 : 0);
            int addY = (thisSize.getY() > 1 ? 1 : 0) + (otherSize.getY() > 1 ? 1 : 0);
            int addZ = (thisSize.getZ() > 1 ? 1 : 0) + (otherSize.getZ() > 1 ? 1 : 0);
            if (addX == 2 || (axis == Axis.X && addX == 1)) return false;
            if (addY == 2 || (axis == Axis.Y && addY == 1)) return false;
            if (addZ == 2 || (axis == Axis.Z && addZ == 1)) return false;
        }
        return true;
    }

    @Override
    public List<TileMarkerVolume> getValidConnections() {
        if (connected.size() >= 3) return ImmutableList.of();
        Set<Axis> taken = EnumSet.noneOf(EnumFacing.Axis.class);
        for (BlockPos other : connected) {
            EnumFacing offset = PositionUtil.getDirectFacingOffset(getPos(), other);
            if (offset != null) {
                taken.add(offset.getAxis());
            }
        }

        final Map<BlockPos, TileMarkerVolume> cache = getCacheForSide();
        List<TileMarkerVolume> valids = new ArrayList<>();

        for (EnumFacing face : EnumFacing.values()) {
            if (taken.contains(face.getAxis())) continue;
            for (int i = 1; i < BCCoreConfig.markerMaxDistance; i++) {
                BlockPos toTry = getPos().offset(face, i);
                TileMarkerVolume other = cache.get(toTry);
                if (other == null) continue;
                if (!areBoxesAddable(other, face.getAxis())) break;
                if (!other.areBoxesAddable(this, face.getAxis())) break;
                valids.add(other);
                break;
            }
        }
        return valids;
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
    @SideOnly(Side.CLIENT)
    public LaserType getPossibleLaserType() {
        return BuildCraftLaserManager.MARKER_VOLUME_POSSIBLE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("");
        if (box == null) {
            left.add("No box");
        } else {
            left.add("Box:");
            left.add("   min = " + box.min());
            left.add("   max = " + box.max());
        }
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
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("showSignals", showSignals);
        if (box != null) {
            NBTTagCompound boxNbt = new NBTTagCompound();
            box.writeToNBT(boxNbt);
            nbt.setTag("box", boxNbt);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        showSignals = nbt.getBoolean("showSignals");
        if (nbt.hasKey("box")) {
            NBTTagCompound boxNbt = nbt.getCompoundTag("box");
            box = new Box();
            box.initialize(boxNbt);
        }
    }

    private void regenBox() {
        boolean before = isActiveForRender();
        Box old = box;
        if (connected.size() > 0) {
            box = new Box();
            for (TileMarkerVolume connectedTo : gatherAllLoadedConnections()) {
                box.extendToEncompass(connectedTo.getPos());
            }
        } else {
            box = null;
        }
        if (!Objects.equals(old, box)) {
            sendNetworkUpdate(NET_RENDER_DATA);
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
        for (TileMarkerVolume connectedTo : gatherAllLoadedConnections()) {
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
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
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
                regenBox();
            }
        }
    }
}
