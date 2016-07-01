package buildcraft.factory.block;

import buildcraft.factory.tile.TileMiningWell;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockMiningWell extends BlockBCTile_Neptune implements IBlockWithFacing {
    public BlockMiningWell(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileMiningWell();
    }
}
