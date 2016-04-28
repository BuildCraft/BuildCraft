package buildcraft.lib.tile;

import java.util.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;

public abstract class TileMarkerBase<T extends TileMarkerBase<T>> extends TileBuildCraft_BC8 {
    public static final List<MarkerCache<?>> CACHES = new ArrayList<>();

    protected Set<T> connectedAndLoaded = new HashSet<>();
    protected Set<BlockPos> connectedNotLoaded = new HashSet<>();
    protected Set<BlockPos> allConnected = new HashSet<>();

    public static <T extends TileMarkerBase<T>> MarkerCache<T> createCache(String name) {
        MarkerCache<T> cache = new MarkerCache<>(name);
        CACHES.add(cache);
        return cache;
    }

    /** Generic helper method for getting this as a type of "T" cleanly. Love you generics, but sometimes...
     * 
     * @return (T) this; */
    protected abstract T getAsType();

    /** @return A cache for storing ALL markers in the world. If you use {@link #createCache(String)} then your markers
     *         will be connectable with the marker connector */
    public abstract MarkerCache<T> getCache();

    public Map<BlockPos, T> getCacheForSide() {
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

    /** @return A list of all the possible valid connections that this marker could make. This may be empty, but never
     *         null. */
    public List<T> getValidConnections() {
        List<T> list = new ArrayList<>();
        for (T t : getCacheForSide().values()) {
            if (canConnectTo(t) && t.canConnectTo(getAsType())) list.add(t);
        }
        return list;
    }

    /** Attempts to connect the two markers together.
     * 
     * @return True if they were connected, false if not. */
    public final boolean tryConnectTo(T other) {
        T thisType = getAsType();
        if (allConnected.contains(other.getPos())) return false;// They were already connected
        if (!canConnectTo(other) || !other.canConnectTo(thisType)) return false;
        connectedAndLoaded.add(other);
        other.connectedAndLoaded.add(thisType);
        allConnected.add(other.getPos());
        other.allConnected.add(getPos());
        onConnect(other);
        other.onConnect(thisType);
        if (!worldObj.isRemote) {
            sendNetworkUpdate(NET_RENDER_DATA);
            other.sendNetworkUpdate(NET_RENDER_DATA);
        }
        return true;
    }

    /** Disconnects this from the other one, if it was connected to the other one. */
    public final void disconnect(T other) {
        T thisType = getAsType();
        if (connectedAndLoaded.contains(other)) {
            allConnected.remove(other.getPos());
            other.allConnected.remove(getPos());
            connectedAndLoaded.remove(other);
            other.connectedAndLoaded.remove(thisType);
            onDisconnect(other);
            other.onDisconnect(thisType);
            if (!worldObj.isRemote) {
                sendNetworkUpdate(NET_RENDER_DATA);
                other.sendNetworkUpdate(NET_RENDER_DATA);
            }
        }
    }

    /** Gathers ALL tiles that are connected, directly or indirectly, to this marker tile. */
    public final Set<T> gatherAllConnections() {
        Set<T> set = new HashSet<>();
        addAllConnections(set);
        return set;
    }

    final void addAllConnections(Set<T> set) {
        for (T connectedTo : connectedAndLoaded) {
            if (set.add(connectedTo)) connectedTo.addAllConnections(set);
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

    private void removeSelfFromCache() {
        getCacheForSide().remove(this.getPos());
        for (T t : connectedAndLoaded) {
            t.connectedAndLoaded.remove(this);
            t.connectedNotLoaded.add(getPos());
        }
    }

    private void addSelfToCache() {
        T thisType = getAsType();
        getCacheForSide().put(this.getPos(), thisType);
        attemptConnection();
    }

    private void attemptConnection() {
        T thisType = getAsType();
        Iterator<BlockPos> iter = connectedNotLoaded.iterator();
        while (iter.hasNext()) {
            BlockPos potential = iter.next();
            T type = getCacheForSide().get(potential);
            if (type != null) {
                iter.remove();
                connectedAndLoaded.add(type);
                type.connectedNotLoaded.remove(getPos());
                type.connectedAndLoaded.add(thisType);
            }
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (BlockPos pos : allConnected) {
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
            connectedNotLoaded.add(pos);
            allConnected.add(pos);
        }
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                byte connected = (byte) allConnected.size();
                buffer.writeByte(connected);
                for (BlockPos connectedTo : allConnected) {
                    buffer.writeBlockPos(connectedTo);
                }
                BCLog.logger.info("Wrote connected data for " + getPos() + " [" + allConnected.size() + "]");
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) {
        super.readPayload(id, buffer, side);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                byte connected = buffer.readByte();
                allConnected.clear();
                connectedAndLoaded.clear();
                connectedNotLoaded.clear();
                for (int i = 0; i < connected; i++) {
                    BlockPos connectedTo = buffer.readBlockPos();
                    BCLog.logger.info("Read a connected block pos (" + getPos() + " -> " + connectedTo + ")");
                    allConnected.add(connectedTo);
                    connectedNotLoaded.add(connectedTo);
                }
                attemptConnection();
            }
        }
    }
}
