package ct.buildcraft.robotics.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface IBlockFilter {
    boolean matches(Level level, BlockPos pos);
}
