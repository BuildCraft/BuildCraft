// STUB(R.Chen): Forge EntityJoinWorldEvent — compile shim.
package net.minecraftforge.event.entity;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityJoinWorldEvent {
    private final Entity entity;
    private final World world;

    public EntityJoinWorldEvent(Entity entity, World world) {
        this.entity = entity;
        this.world = world;
    }

    public Entity getEntity() { return entity; }
    public World getWorld() { return world; }
}
