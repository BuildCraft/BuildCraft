package buildcraft.builders.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.builders.BuildersGuis;
import buildcraft.builders.tile.TileLibrary_Neptune;
import buildcraft.lib.block.BlockBCTile_Neptune;

public class BlockLibrary_Neptune extends BlockBCTile_Neptune {
    private static final BuildCraftProperty<EnumFacing> PROP_FACING = BuildCraftProperties.BLOCK_FACING;

    public BlockLibrary_Neptune(Material material, String id) {
        super(material, id);
        setDefaultState(getDefaultState().withProperty(PROP_FACING, EnumFacing.NORTH));
    }

    // IBlockState

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PROP_FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        meta |= state.getValue(PROP_FACING).getHorizontalIndex() & 3;
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        state = state.withProperty(PROP_FACING, EnumFacing.getHorizontal(meta & 3));
        return state;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        EnumFacing facing = state.getValue(PROP_FACING);
        state = state.withProperty(PROP_FACING, rot.rotate(facing));
        return state;
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        EnumFacing facing = state.getValue(PROP_FACING);
        state = state.withProperty(PROP_FACING, mirror.mirror(facing));
        return state;
    }

    // Others

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        EnumFacing orientation = placer.getHorizontalFacing();
        world.setBlockState(pos, state.withProperty(PROP_FACING, orientation.getOpposite()));
        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileLibrary_Neptune();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        BuildersGuis.LIBRARY.openGUI(player, pos);
        return true;
    }
}
