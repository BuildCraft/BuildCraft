package buildcraft.core.lib.block;

import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileBuildCraft_BC8 extends TileEntity {

    /** Checks to see if this tile can update. The base implementation only checks to see if it has a world. */
    public boolean cannotUpdate() {
        return !hasWorldObj();
    }
    
    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket() {
        return null;
    }
}
