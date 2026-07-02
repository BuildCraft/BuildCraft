package ct.buildcraft.api.robots;

import net.minecraft.world.level.Level;

public interface IRobotRegistryProvider {
    IRobotRegistry getRegistry(Level level);
}
