package buildcraft.lib.tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.Constants;

import buildcraft.core.lib.utils.NBTUtils;

public abstract class TileMarkerBase<T extends TileMarkerBase<T>> extends TileBuildCraft_BC8 {
    protected List<T> connectedAndLoaded = new ArrayList<>();
    protected List<BlockPos> connectedNotLoaded = new ArrayList<>();
    protected List<BlockPos> allConnected = new ArrayList<>();

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
        if (!canConnectTo(other) || !other.canConnectTo(thisType)) return false;
        connectedAndLoaded.add(other);
        other.connectedAndLoaded.add(thisType);
        onConnect(other);
        return true;
    }

    protected abstract void onConnect(T other);

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
        for (BlockPos pos : connectedNotLoaded) {
            list.appendTag(NBTUtils.writeBlockPos(pos));
        }
        for (T type : connectedAndLoaded) {
            list.appendTag(NBTUtils.writeBlockPos(type.getPos()));
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
}
