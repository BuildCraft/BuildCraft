// TODO(R.Chen): blocked by lib.block.BlockBCTile_Neptune (not yet migrated to Fabric 1.20.1)
package buildcraft.core.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.core.tile.TilePowerConsumerTester;

public class BlockPowerConsumerTester extends BlockBCTile_Neptune {

    public BlockPowerConsumerTester(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(World worldIn, BlockState state) {
        return new TilePowerConsumerTester();
    }
}
