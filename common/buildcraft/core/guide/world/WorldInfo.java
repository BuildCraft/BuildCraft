package buildcraft.core.guide.world;

import net.minecraft.util.Vec3;

public class WorldInfo {
    public final String schematic;
    public final WorldLabel[] labels;
    public final Vec3 cameraPos;
    public final Vec3 cameraFacing;

    public WorldInfo(String schematic, WorldLabel[] labels, Vec3 cameraPos, Vec3 cameraFacing) {
        this.schematic = schematic;
        this.labels = labels;
        this.cameraPos = cameraPos;
        this.cameraFacing = cameraFacing;
    }

    public byte[] getSchematic() {
        return new byte[0];
    }
}
