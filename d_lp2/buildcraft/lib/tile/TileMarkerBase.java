package buildcraft.lib.tile;

import java.io.IOException;
import java.util.*;

import com.google.common.collect.ImmutableList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;
import buildcraft.lib.misc.NBTUtils;

@Deprecated
public abstract class TileMarkerBase<T extends TileMarkerBase<T, C>, C extends MarkerConnection<T, C>> extends TileBC_Neptune implements IDebuggable {
    protected C currentConnection;

    /** Generic helper method for getting this as a type of "T" cleanly. Love you generics, but sometimes...
     * 
     * @return (T) this; */
    protected abstract T getAsType();

    /** @return A cache for storing ALL markers in the world. If you use {@link #createCache(String)} then your markers
     *         will be connectable with the marker connector */
    public abstract MarkerCache<T, C> getCache();

    public MarkerCache<T, C>.PerWorld getCacheForSide() {
        return getCache().getCache(worldObj);
    }

    /** Checks to see if this can connect to the other type. */
    public abstract boolean canConnectTo(T other);

    /** @return True if this has lasers being emitted, or any other reason you want. Activates the surrounding "glow"
     *         parts for the block model. */
    public abstract boolean isActiveForRender();

    /** @return The type override. By default this will use the MARKER_POSSIBLE in the BC Core class
     *         BuildCraftLaserManager. Return a non-null value to use something else. */
    @SideOnly(Side.CLIENT)
    public abstract LaserType getPossibleLaserType();

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    /** @return A list of all the possible valid connections that this marker could make. This may be empty, but never
     *         null. */
    public List<T> getValidConnections() {
        List<T> list = new ArrayList<>();
        for (T t : getCacheForSide().tiles.values()) {
            if (canConnectTo(t) && t.canConnectTo(getAsType())) list.add(t);
        }
        return list;
    }

    /** Attempts to connect the two markers together.
     * 
     * @return True if they were connected, false if not. */
    public final boolean tryConnectTo(T other) {
        T thisType = getAsType();

        if (this.currentConnection == other.currentConnection) return false;// They were already connected
        if (!canConnectTo(other) || !other.canConnectTo(thisType)) return false;

        connect(other);

        return true;
    }

    private void connect(T other) {
        T thisType = getAsType();
        connected.add(other.getPos());
        other.connected.add(getPos());

        thisType.onConnect(other);
        other.onConnect(thisType);

        if (!worldObj.isRemote) {
            sendNetworkUpdate(NET_RENDER_DATA);
            other.sendNetworkUpdate(NET_RENDER_DATA);
        }
    }

    /** Disconnects this from the other one, if it was connected to the other one. */
    public final void disconnect(T other) {
        T thisType = getAsType();
        BlockPos otherPos = other.getPos();

        if (connected.contains(otherPos)) {
            Set<T> all = gatherAllLoadedConnections();

            connected.remove(otherPos);
            other.connected.remove(getPos());

            for (T existing : all) {
                existing.onDisconnect(existing == other ? thisType : other);
            }

            if (!worldObj.isRemote) {
                for (T existing : all) {
                    existing.sendNetworkUpdate(NET_RENDER_DATA);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("Connections:");
        Set<BlockPos> gathered = gatherAllConnections();
        Map<BlockPos, T> all = new HashMap<>();
        for (BlockPos p : gathered) {
            all.put(p, getCacheForSide().get(p));
        }
        List<BlockPos> list = new ArrayList<>(all.keySet());
        Collections.sort(list);
        for (BlockPos pos : list) {
            T value = all.get(pos);
            String s = " - " + pos + " [";
            if (value == null) {
                s += TextFormatting.RED + "U";
            } else {
                s += TextFormatting.GREEN + "L";
            }
            if (pos.equals(getPos())) {
                s += TextFormatting.BLACK + "S";
            } else if (connected.contains(pos)) {
                s += TextFormatting.YELLOW + "D";
            } else {
                s += TextFormatting.AQUA + "I";
            }
            s += getTypeInfo(pos, value);
            s += TextFormatting.RESET + "]";
            left.add(s);
        }
    }

    @SideOnly(Side.CLIENT)
    protected String getTypeInfo(BlockPos pos, T value) {
        return "";
    }

    public final Set<BlockPos> gatherAllConnections() {
        Set<T> loadedMarkers = gatherAllLoadedConnections();
        Set<BlockPos> positions = new HashSet<>();
        for (T marker : loadedMarkers) {
            positions.add(marker.getPos());
            positions.addAll(marker.connected);
        }
        return positions;
    }

    /** Gathers ALL tiles that are connected, directly or indirectly, to this marker tile. */
    public final Set<T> gatherAllLoadedConnections() {
        Set<T> set = new HashSet<>();
        set.add(getAsType());
        addAllConnections(set);
        return set;
    }

    final void addAllConnections(Set<T> set) {
        for (BlockPos to : connected) {
            T connectedTo = getCacheForSide().get(to);
            if (connectedTo != null) {
                if (set.add(connectedTo)) connectedTo.addAllConnections(set);
            }
        }
    }

    protected abstract void onConnect(T other);

    protected abstract void onDisconnect(T other);

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        removeSelfFromCache();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        disconnectFromOthers();
        removeSelfFromCache();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        addSelfToCache();
    }

    @Override
    public void validate() {
        super.validate();
        if (hasWorldObj()) addSelfToCache();
    }

    @Override
    public void onRemove() {
        super.onRemove();
        disconnectFromOthers();
    }

    protected void disconnectFromOthers() {
        for (BlockPos p : ImmutableList.copyOf(connected)) {
            T other = getCacheForSide().get(p);
            if (other != null) disconnect(other);
        }
    }

    private void removeSelfFromCache() {
        getCacheForSide().remove(this.getPos());
    }

    private void addSelfToCache() {
        T thisType = getAsType();
        getCacheForSide().put(this.getPos(), thisType);
        attemptConnection();
    }

    private void attemptConnection() {
        Set<T> toConnect = new HashSet<>();
        for (BlockPos p : connected) {
            T value = getCacheForSide().get(p);
            toConnect.add(value);
        }
        for (T other : toConnect) {
            if (other == null) continue;
            connect(other);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (BlockPos pos : connected) {
            list.appendTag(NBTUtils.writeBlockPos(pos));
        }
        nbt.setTag("connected", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = nbt.getTagList("connected", Constants.NBT.TAG_INT_ARRAY);
        for (int i = 0; i < list.tagCount(); i++) {
            BlockPos pos = NBTUtils.readBlockPos(list.get(i));
            connected.add(pos);
        }
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                byte numConnected = (byte) connected.size();
                buffer.writeByte(numConnected);
                for (BlockPos connectedTo : connected) {
                    buffer.writeBlockPos(connectedTo);
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                byte numConnected = buffer.readByte();
                Map<BlockPos, T> existing = new HashMap<>();
                for (BlockPos p : connected) {
                    existing.put(p, getCacheForSide().get(p));
                }
                connected.clear();
                for (int i = 0; i < numConnected; i++) {
                    BlockPos connectedTo = buffer.readBlockPos();
                    if (existing.containsKey(connectedTo)) {
                        connected.add(connectedTo);
                        existing.remove(connectedTo);
                    } else {
                        connected.add(connectedTo);
                    }
                }
                for (T nowInvalid : existing.values()) {
                    if (nowInvalid != null) {
                        nowInvalid.onDisconnect(getAsType());
                        onDisconnect(nowInvalid);
                    }
                }
                attemptConnection();
            }
        }
    }
}
