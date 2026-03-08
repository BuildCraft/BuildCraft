package buildcraft.lib.raytrace;

import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;

public class RayTraceResultBC extends BlockRayTraceResult {
    /** From 1.12.2.
     * Used to determine what sub-segment is hit */
    public int subHit = -1;
    public final Direction sideHit;
    private final BlockRayTraceResult delegate;

    private RayTraceResultBC(@Nonnull BlockRayTraceResult delegate, int subHit) {
        super(delegate.getLocation(), delegate.getDirection(), delegate.getBlockPos(), delegate.isInside());
        this.delegate = delegate;
        this.subHit = subHit;
        this.sideHit = delegate.getDirection();
    }

    public boolean isEmpty() {
        return delegate == null;
    }

    public static RayTraceResultBC fromMcHitResult(@Nonnull RayTraceResult hitResult) {
        if (!(hitResult instanceof BlockRayTraceResult)) {
            return null;
        }
        return new RayTraceResultBC((BlockRayTraceResult) hitResult, -1);
    }

    @Override
    public double distanceTo(Entity p_82449_) {
        return this.delegate.distanceTo(p_82449_);
    }

    @Override
    public Type getType() {
        return this.delegate.getType();
    }

    @Override
    public Vector3d getLocation() {
        return this.delegate.getLocation();
    }
}
