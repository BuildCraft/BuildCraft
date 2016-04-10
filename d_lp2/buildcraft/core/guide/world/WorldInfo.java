package buildcraft.core.guide.world;

import net.minecraft.util.math.Vec3d;

public class WorldInfo {
    public final String schematic;
    public final WorldLabel[] labels;
    public final Vec3d cameraPos;
    public final Vec3d cameraFacing;

    public WorldInfo(String schematic, WorldLabel[] labels, Vec3d cameraPos, Vec3d cameraFacing) {
        this.schematic = schematic;
        this.labels = labels;
        this.cameraPos = cameraPos;
        this.cameraFacing = cameraFacing;
    }

    public byte[] getSchematic() {
        return new byte[0];
    }
}
