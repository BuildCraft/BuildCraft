package buildcraft.lib.guide.world;

import net.minecraft.util.math.Vec3d;

public class WorldLabel {
    public final String label;
    public final double size;
    public final double offset;
    public final Vec3d position;

    public WorldLabel(String label, double size, double offset, Vec3d position) {
        this.label = label;
        this.size = size;
        this.offset = offset;
        this.position = position;
    }
}
