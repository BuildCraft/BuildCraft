package buildcraft.api.core;

import java.util.List;
import buildcraft.api.core.BlockIndex;

/**
 * To be implemented by TileEntities able to provide a path on the world, typically BuildCraft path markers.
 */
public interface IPathProvider {
	List<BlockIndex> getPath();
}
