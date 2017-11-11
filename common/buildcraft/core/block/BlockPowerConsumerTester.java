package buildcraft.core.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.core.tile.TilePowerConsumerTester;

public class BlockPowerConsumerTester extends BlockBCTile_Neptune {

    public BlockPowerConsumerTester(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(World worldIn, IBlockState state) {
        return new TilePowerConsumerTester();
    }
}
