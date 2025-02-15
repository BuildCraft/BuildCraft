package buildcraft.lib.block;

import buildcraft.api.tiles.ITickable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Used in {@link BlockBCTile_Neptune#getTicker(Level, BlockState, BlockEntityType)} to decide creating ticker or not.
 * A block implements this should implement {@link EntityBlock} and {@link ITickable}.
 * @param <T>
 */
public interface IBlockWithTickableTE<T extends ITickable> {
}
