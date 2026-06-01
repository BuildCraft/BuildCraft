// STUB(R.Chen): Forge GetCollisionBoxesEvent — compile shim.
package net.minecraftforge.event.world;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class GetCollisionBoxesEvent {
    private final World world;
    private final Entity entity;
    private final Box aabb;
    private final List<Box> collisionBoxes;

    public GetCollisionBoxesEvent(World world, Entity entity, Box aabb, List<Box> collisionBoxes) {
        this.world = world;
        this.entity = entity;
        this.aabb = aabb;
        this.collisionBoxes = collisionBoxes;
    }

    public World getWorld() { return world; }
    public Entity getEntity() { return entity; }
    public Box getAABB() { return aabb; }
    public List<Box> getCollisionBoxesList() { return collisionBoxes; }
}
