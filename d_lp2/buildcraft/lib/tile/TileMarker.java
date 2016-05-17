package buildcraft.lib.tile;

import java.util.List;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.marker.MarkerCache2;
import buildcraft.lib.marker.MarkerCache2.PerWorld2;
import buildcraft.lib.marker.MarkerConnection2;

public abstract class TileMarker<C extends MarkerConnection2<C>, T extends TileMarker<C, T>> extends TileBC_Neptune implements IDebuggable {
    public abstract MarkerCache2<? extends PerWorld2<C, T>> getCache();

    public PerWorld2<C, T> getLocalCache() {
        return getCache().getSubCache(worldObj);
    }

    public abstract T getAsType();

    public abstract boolean tryConnectTo(T other);

    public C getCurrentConnection() {
        return getLocalCache().getConnection(getPos());
    }

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
        C currentConnection = getCurrentConnection();
        if (currentConnection != null) {
            currentConnection.removeMarker(getPos());
        }
    }

    private void removeSelfFromCache() {
        getLocalCache().unloadMarker(getPos());
    }

    private void addSelfToCache() {
        getLocalCache().addMarker(getPos(), getAsType());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        C current = getCurrentConnection();
        if (current == null) {
            left.add("");
            left.add("No connection!");
        } else {
            current.getDebugInfo(getWorld(), getPos(), left);
        }
    }
}
