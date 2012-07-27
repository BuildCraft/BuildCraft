package buildcraft.api.transport;

import net.minecraft.src.World;

/**
 * Implement and register with the PipeManager if you want to suppress connections from wooden pipes.
 */
public interface IExtractionHandler {
	boolean canExtractItems(World world, int i, int j, int k);
	boolean canExtractLiquids(World world, int i, int j, int k);
}
