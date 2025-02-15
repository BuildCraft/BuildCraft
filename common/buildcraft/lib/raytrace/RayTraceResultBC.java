package buildcraft.lib.raytrace;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class RayTraceResultBC extends BlockHitResult {
    /** From 1.12.2.
     * Used to determine what sub-segment is hit */
    public int subHit = -1;
    public final Direction sideHit;
    private final BlockHitResult delegate;

    private RayTraceResultBC(@NotNull BlockHitResult delegate, int subHit) {
        super(delegate.getLocation(), delegate.getDirection(), delegate.getBlockPos(), delegate.isInside());
        this.delegate = delegate;
        this.subHit = subHit;
        this.sideHit = delegate.getDirection();
    }

    public boolean isEmpty() {
        return delegate == null;
    }

    public static RayTraceResultBC fromMcHitResult(@NotNull HitResult hitResult) {
        if (!(hitResult instanceof BlockHitResult)) {
            return null;
        }
        return new RayTraceResultBC((BlockHitResult) hitResult, -1);
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
    public Vec3 getLocation() {
        return this.delegate.getLocation();
    }
}
