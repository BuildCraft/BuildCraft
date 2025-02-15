package buildcraft.core.block;

import buildcraft.core.tile.TilePowerConsumerTester;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPowerConsumerTester extends BlockBCTile_Neptune<TilePowerConsumerTester> {

    public BlockPowerConsumerTester(String idBC, BlockBehaviour.Properties properties) {
        super(idBC, properties);
    }

    @Override
//    public TileBC_Neptune createTileEntity(World worldIn, IBlockState state)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TilePowerConsumerTester(pos, state);
    }
}
