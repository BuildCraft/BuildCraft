package buildcraft.lib.guide.block;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBlockGuidePageMapper {
    /** @param world The world object to query
     * @param pos The position to query
     * @param state The state of the block
     * @return What to append to the block location (So, returning "quartz" would give a complete resource location of
     *         "modname:guide/block/quartz.md") */
    String getFor(World world, BlockPos pos, IBlockState state);

    /** @return A complete list of all the possible pages that can be returned by
     *         {@link #getFor(World, BlockPos, IBlockState)} */
    List<String> getAllPossiblePages();
}
