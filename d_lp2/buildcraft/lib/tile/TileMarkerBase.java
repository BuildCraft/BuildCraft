package buildcraft.lib.tile;

import java.util.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.core.lib.utils.NBTUtils;

public abstract class TileMarkerBase<T extends TileMarkerBase<T>> extends TileBuildCraft_BC8 {
    protected Set<T> connectedAndLoaded = new HashSet<>();
    protected Set<BlockPos> connectedNotLoaded = new HashSet<>();
    protected Set<BlockPos> allConnected = new HashSet<>();

    /** Generic helper method for getting this as a type of "T" cleanly. Love you generics, but sometimes...
     * 
     * @return (T) this; */
    protected abstract T getAsType();

    /** @return A cache for storing ALL markers in the world. Normally this should be a hash map so lookups shouldn't be
     *         too long. */
    public abstract Map<BlockPos, T> getCache();

    /** Checks to see if this can connect to the other type. */
    public abstract boolean canConnectTo(T other);

    /** @return A list of all the possible valid connections that this marker could make. This may be empty, but never
     *         null. */
    public final List<T> getValidConnections() {
        List<T> list = new ArrayList<>();
        for (T t : getCache().values()) {
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
        getCache().remove(this.getPos());
        for (T t : connectedAndLoaded) {
            t.connectedAndLoaded.remove(this);
            t.connectedNotLoaded.add(getPos());
        }
    }

    private void addSelfToCache() {
        T thisType = getAsType();
        getCache().put(this.getPos(), thisType);
        attemptConnection();
    }

    private void attemptConnection() {
        T thisType = getAsType();
        Iterator<BlockPos> iter = connectedNotLoaded.iterator();
        while (iter.hasNext()) {
            BlockPos potential = iter.next();
            T type = getCache().get(potential);
            if (type != null) {
                iter.remove();
                connectedAndLoaded.add(type);
                type.connectedNotLoaded.remove(getPos());
                type.connectedAndLoaded.add(thisType);
            }
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList list = new NBTTagList();
        for (BlockPos pos : allConnected) {
            list.appendTag(NBTUtils.writeBlockPos(pos));
        }
        compound.setTag("connected", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        NBTTagList list = compound.getTagList("connected", Constants.NBT.TAG_INT_ARRAY);
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
                    allConnected.add(connectedTo);
                    connectedNotLoaded.add(connectedTo);
                }
                attemptConnection();
            }
        }
    }
}
