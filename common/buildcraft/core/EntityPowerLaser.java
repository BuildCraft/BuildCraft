package buildcraft.core;

import net.minecraft.src.World;
import buildcraft.api.core.Position;

public class EntityPowerLaser extends EntityLaser {

	public EntityPowerLaser(World world) {
		super(world);
	}

	public EntityPowerLaser(World world, Position head, Position tail) {
		super(world, head, tail);
	}

}
