package ct.buildcraft.api.tiles;

import ct.buildcraft.api.core.IAreaProvider;

import net.minecraft.core.BlockPos;

/** Used for more fine-grained control of whether or not a machine connects to the provider here. */
public interface ITileAreaProvider extends IAreaProvider {
    boolean isValidFromLocation(BlockPos pos);
}
