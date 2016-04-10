package buildcraft.core.guide.world;

import net.minecraft.util.Vec3;

public class WorldLabel {
    public final String label;
    public final double size;
    public final double offset;
    public final Vec3 position;

    public WorldLabel(String label, double size, double offset, Vec3 position) {
        this.label = label;
        this.size = size;
        this.offset = offset;
        this.position = position;
    }
}
