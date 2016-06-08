package buildcraft.builders.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
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
import buildcraft.builders.tile.TileArchitect_Neptune;
import buildcraft.lib.block.BlockBCTile_Neptune;

public class BlockArchitect_Neptune extends BlockBCTile_Neptune {
    public static final IProperty<EnumFacing> PROP_FACING = BuildCraftProperties.BLOCK_FACING;
    public static final IProperty<Boolean> PROP_VALID = BuildCraftProperties.VALID;

    private static final int META_VALID_INDEX = 4;

    public BlockArchitect_Neptune(Material material, String id) {
        super(material, id);
        IBlockState defaultState = getDefaultState();
        defaultState = defaultState.withProperty(PROP_FACING, EnumFacing.NORTH);
        defaultState = defaultState.withProperty(PROP_VALID, Boolean.TRUE);
        setDefaultState(defaultState);
    }

    // BlockState

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PROP_FACING, PROP_VALID);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        state = state.withProperty(PROP_VALID, Boolean.valueOf((meta & META_VALID_INDEX) == 0));
        return state.withProperty(PROP_FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int valid = state.getValue(PROP_VALID) ? 0 : META_VALID_INDEX;
        return state.getValue(PROP_FACING).getHorizontalIndex() | valid;
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
