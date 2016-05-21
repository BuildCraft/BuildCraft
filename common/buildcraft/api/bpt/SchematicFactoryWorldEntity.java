package buildcraft.api.bpt;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public interface SchematicFactoryWorldEntity<E extends Entity> {
    SchematicEntity createFromWorld(World world, E entity);
}
