package buildcraft.factory.block;

import buildcraft.lib.block.BlockBCBase_Neptune;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class BlockTube extends BlockBCBase_Neptune {
    public BlockTube(Material material, String id) {
        super(material, id);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
}
