package buildcraft.lib.block;

import buildcraft.api.tiles.ITickable;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.World;

/**
 * Used in {@link BlockBCTile_Neptune#getTicker(World, BlockState, TileEntityType)} to decide creating ticker or not.
 * A block implements this should implement {@link ITileEntityProvider} and {@link ITickable}.
 * @param <T>
 */
public interface IBlockWithTickableTE<T extends ITickable> {
}
