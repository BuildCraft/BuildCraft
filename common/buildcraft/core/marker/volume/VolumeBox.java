package buildcraft.core.marker.volume;

import buildcraft.lib.misc.data.Box;
import net.minecraft.util.math.BlockPos;

public class VolumeBox {
    public final Box box;
    public String player = null;
    public BlockPos oldMin = null, oldMax = null;
    public BlockPos held = null;
    public double dist = 0;

    public VolumeBox(BlockPos at) {
        box = new Box(at, at);
    }

    public VolumeBox(Box box, String player) {
        this.box = box;
        this.player = player;
    }

    public boolean isEditing() {
        return player != null;
    }

    public void resetEditing() {
        oldMin = oldMax = null;
        held = null;
        dist = 0;
    }
}
