package ct.buildcraft.api.transport;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IStripesHandlerBlock {

    /** @param world
     * @param pos
     * @param direction
     * @param player
     * @param activator
     * @return True if this broke a block, false otherwise (note that this handler MUST NOT return false if it has
     *         changed the world in any way) */
    boolean handle(Level world, BlockPos pos, Direction direction, Player player, IStripesActivator activator);
}
