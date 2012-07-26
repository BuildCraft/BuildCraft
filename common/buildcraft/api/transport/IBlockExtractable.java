package buildcraft.api.transport;

import net.minecraft.src.World;

/**
 * Implemented by blocks that may want to suppress connections from wooden pipes.
 */
public interface IBlockExtractable {
	boolean mayExtract(World world, int x, int y, int z);
}
