package buildcraft.core.tile;

import buildcraft.core.marker.VolumeCache;
import buildcraft.core.marker.VolumeConnection;
import buildcraft.lib.tile.TileMarker;

public class TileMarkerVolume2 extends TileMarker<VolumeConnection, TileMarkerVolume2> {

    @Override
    public TileMarkerVolume2 getAsType() {
        return this;
    }

    @Override
    public VolumeCache getCache() {
        return VolumeCache.INSTANCE;
    }

    @Override
    public boolean tryConnectTo(TileMarkerVolume2 other) {
        VolumeConnection currentConnection = getCurrentConnection();
        VolumeConnection otherConnection = other.getCurrentConnection();
        if (currentConnection == null) {
            if (otherConnection == null) {
                // Create a new connection
                return VolumeConnection.tryCreateConnection(this, other);
            } else {// The other one has a connection
                return otherConnection.addMarker(getPos());
            }
        } else {// We have a connection
            if (otherConnection == null) {
                return currentConnection.addMarker(other.getPos());
            } else {// The other one has a connection
                return currentConnection.mergeWith(otherConnection);
            }
        }
    }
}
