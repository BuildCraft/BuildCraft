package buildcraft.core.block;

import buildcraft.core.tile.TilePowerConsumerTester;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.AbstractBlock;
import net.minecraft.world.IBlockReader;

public class BlockPowerConsumerTester extends BlockBCTile_Neptune<TilePowerConsumerTester> {

    public BlockPowerConsumerTester(String idBC, AbstractBlock.Properties properties) {
        super(idBC, properties);
    }

    @Override
//    public TileBC_Neptune createTileEntity(World worldIn, IBlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return new TilePowerConsumerTester();
    }
}
