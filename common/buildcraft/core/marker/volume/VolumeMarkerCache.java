package buildcraft.core.marker.volume;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.BlockPos;

import buildcraft.lib.misc.data.Box;

public class VolumeMarkerCache {
    // FIXME!
    public static final VolumeMarkerCache SERVER_INSTANCE = new VolumeMarkerCache();

    public VolumeBox currentlyEditing;
    public Box renderCache = new Box();
    public BlockPos held;
    public double dist;

    public final List<VolumeBox> boxes = new ArrayList<>();

    public VolumeBox getBoxAt(BlockPos pos) {
        for (VolumeBox box : boxes) {
            if (box.box.contains(pos)) {
                return box;
            }
        }
        return null;
    }

    public VolumeBox addBox(BlockPos pos) {
        VolumeBox box = new VolumeBox(pos);
        boxes.add(box);
        return box;
    }

    public static class VolumeBox {
        public final Box box;

        public VolumeBox(BlockPos at) {
            box = new Box(at, at);
        }
    }
}
