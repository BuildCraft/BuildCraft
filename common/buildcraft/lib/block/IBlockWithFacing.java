package buildcraft.lib.block;

import net.minecraft.util.EnumFacing;

/** Marker interface used by {@link BlockBCBase_Neptune} to automatically add an {@link EnumFacing} property to blocks,
 * and go to and from meta. */
public interface IBlockWithFacing {
    default boolean canPlacedVertical() {
        return false;
    }
}
