package buildcraft.lib.tile;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

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

public abstract class TileMarkerBase<T extends TileMarkerBase<T>> extends TileBC_Neptune implements IDebuggable {
    public static final List<MarkerCache<?>> CACHES = new ArrayList<>();

    protected Map<BlockPos, T> connected = new HashMap<>();

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

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

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
        if (connected.containsKey(other.getPos())) return false;// They were already connected
        if (!canConnectTo(other) || !other.canConnectTo(thisType)) return false;

        connect(other);

        return true;
    }

    private void connect(T other) {
        T thisType = getAsType();
        connected.put(other.getPos(), other);
        other.connected.put(getPos(), thisType);

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

        if (connected.containsKey(otherPos)) {
            Set<T> all = gatherAllConnections();

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
        Set<T> gathered = gatherAllConnections();
        Map<BlockPos, T> all = new HashMap<>(connected);
        for (T to : gathered) {
            all.put(to.getPos(), to);
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
            } else if (connected.containsKey(pos)) {
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

    /** Gathers ALL tiles that are connected, directly or indirectly, to this marker tile. */
    public final Set<T> gatherAllConnections() {
        Set<T> set = new HashSet<>();
        set.add(getAsType());
        addAllConnections(set);
        return set;
    }

    final void addAllConnections(Set<T> set) {
        for (T connectedTo : connected.values()) {
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
        for (T other : ImmutableList.copyOf(connected.values())) {
            if (other != null) disconnect(other);
        }
    }

    private void removeSelfFromCache() {
        getCacheForSide().remove(this.getPos());
        for (T t : connected.values()) {
            if (t != null) {
                t.connected.put(getPos(), null);
            }
        }
    }

    private void addSelfToCache() {
        T thisType = getAsType();
        getCacheForSide().put(this.getPos(), thisType);
        attemptConnection();
    }

    private void attemptConnection() {
        Set<T> toConnect = new HashSet<>();
        for (Entry<BlockPos, T> entry : connected.entrySet()) {
            T value = entry.getValue();
            if (value != null) continue;
            value = getCacheForSide().get(entry.getKey());
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
        for (BlockPos pos : connected.keySet()) {
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
            connected.put(pos, null);
        }
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                byte numConnected = (byte) connected.size();
                buffer.writeByte(numConnected);
                for (BlockPos connectedTo : connected.keySet()) {
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
                Map<BlockPos, T> existing = new HashMap<>(connected);
                connected.clear();
                for (int i = 0; i < numConnected; i++) {
                    BlockPos connectedTo = buffer.readBlockPos();
                    if (existing.containsKey(connectedTo)) {
                        connected.put(connectedTo, existing.get(connectedTo));
                        existing.remove(connectedTo);
                    } else {
                        connected.put(connectedTo, null);
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
