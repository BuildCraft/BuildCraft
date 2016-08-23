package buildcraft.lib.tile;

import java.util.List;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.marker.MarkerSubCache;

public abstract class TileMarker<C extends MarkerConnection<C>> extends TileBC_Neptune implements IDebuggable {
    public abstract MarkerCache<? extends MarkerSubCache<C>> getCache();

    public MarkerSubCache<C> getLocalCache() {
        return getCache().getSubCache(worldObj);
    }

    /** @return True if this has lasers being emitted, or any other reason you want. Activates the surrounding "glow"
     *         parts for the block model. */
    public abstract boolean isActiveForRender();

    public C getCurrentConnection() {
        return getLocalCache().getConnection(getPos());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        getLocalCache().loadMarker(getPos(), this);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        getLocalCache().unloadMarker(getPos());
    }

    @Override
    public void invalidate() {
        super.invalidate();
        // getLocalCache().removeMarker(getPos());
    }

    @Override
    public void onRemove() {
        super.onRemove();
        getLocalCache().removeMarker(getPos());
    }

    protected void disconnectFromOthers() {
        C currentConnection = getCurrentConnection();
        if (currentConnection != null) {
            currentConnection.removeMarker(getPos());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        C current = getCurrentConnection();
        if (current == null) {
            left.add("");
            left.add("No connection!");
        } else {
            current.getDebugInfo(getPos(), left);
        }
    }
}
