package buildcraft.core.marker;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.Box;
import buildcraft.core.tile.TileMarkerVolume2;
import buildcraft.lib.marker.MarkerConnection2;

public class VolumeConnection extends MarkerConnection2<VolumeConnection> {
    private final Set<BlockPos> makup = new HashSet<>();
    private final Box box = new Box();

    public static boolean tryCreateConnection(TileMarkerVolume2 one, TileMarkerVolume2 other) {
        // Check validity
        return false;
    }

    @Override
    public VolumeCache getCache() {
        return VolumeCache.INSTANCE;
    }

    @Override
    public void removeMarker(BlockPos pos) {
        makup.remove(pos);
        createBox();
    }

    public boolean addMarker(BlockPos pos) {
        // Check validity
        return false;
    }

    public boolean mergeWith(VolumeConnection other) {
        // Check validity
        return false;
    }

    @Override
    public Collection<BlockPos> getMarkerPositions() {
        return makup;
    }

    private void createBox() {
        box.reset();
        for (BlockPos p : makup) {
            box.extendToEncompass(p);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInWorld() {

    }
}
