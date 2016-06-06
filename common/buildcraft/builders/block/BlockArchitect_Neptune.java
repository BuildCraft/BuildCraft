package buildcraft.builders.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.builders.tile.TileArchitect_Neptune;
import buildcraft.lib.block.BlockBCTile_Neptune;

public class BlockArchitect_Neptune extends BlockBCTile_Neptune {
    public static final BuildCraftProperty<EnumFacing> PROP_FACING = BuildCraftProperties.BLOCK_FACING;

    public BlockArchitect_Neptune(Material material, String id) {
        super(material, id);
        setDefaultState(getDefaultState().withProperty(PROP_FACING, EnumFacing.NORTH));
    }

    // BlockState

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PROP_FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        return state.withProperty(PROP_FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PROP_FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        EnumFacing face = state.getValue(PROP_FACING);
        face = mirror.mirror(face);
        return state.withProperty(PROP_FACING, face);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        EnumFacing face = state.getValue(PROP_FACING);
        face = rot.rotate(face);
        return state.withProperty(PROP_FACING, face);
    }

    // Others

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        EnumFacing orientation = placer.getHorizontalFacing();
        world.setBlockState(pos, state.withProperty(PROP_FACING, orientation.getOpposite()));
        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileArchitect_Neptune();
    }
}
