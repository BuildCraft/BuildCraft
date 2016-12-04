package buildcraft.api.bpt;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Defines a way to read a block as a schematic from the world
 * 
 * @since 10 Apr 2016 */
public interface SchematicFactoryWorldBlock {
    /** Create a schematic from the world
     * 
     * @throws SchematicException If you could not handle the block and tile entity at the location, perhaps because it
     *             was invalid or wasn't your own at the time of reading. */
    SchematicBlock createFromWorld(World world, BlockPos pos) throws SchematicException;
}
