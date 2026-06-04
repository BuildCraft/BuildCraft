// STUB(R.Chen): EntityArrow → PersistentProjectileEntity shim for 1.12.2 compat
package net.minecraft.entity.projectile;

public abstract class EntityArrow extends PersistentProjectileEntity {
    public enum PickupStatus {
        DISALLOWED, ALLOWED, CREATIVE_ONLY;
    }

    protected EntityArrow(net.minecraft.entity.EntityType<? extends PersistentProjectileEntity> type, net.minecraft.world.World world) {
        super(type, world);
    }
}
