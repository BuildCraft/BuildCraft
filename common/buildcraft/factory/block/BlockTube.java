package buildcraft.factory.block;

import buildcraft.lib.block.BlockBCBase_Neptune;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockTube extends BlockBCBase_Neptune {
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(4 / 16D, 0 / 16D, 4 / 16D, 12 / 16D, 16 / 16D, 12 / 16D);

    public BlockTube(Material material, String id) {
        super(material, id);
        setBlockUnbreakable();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }
}
