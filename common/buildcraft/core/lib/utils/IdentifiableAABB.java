package buildcraft.core.lib.utils;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class IdentifiableAABB<T> extends AxisAlignedBB {
    public final T identifier;

    public IdentifiableAABB(AxisAlignedBB bb, T identifier) {
        this(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, identifier);
    }

    public IdentifiableAABB(Vec3 min, Vec3 max, T identifier) {
        this(min.xCoord, min.yCoord, min.zCoord, max.xCoord, max.yCoord, max.zCoord, identifier);
    }

    public IdentifiableAABB(double x1, double y1, double z1, double x2, double y2, double z2, T identifier) {
        super(x1, y1, z1, x2, y2, z2);
        this.identifier = identifier;
    }
}
