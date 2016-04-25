package buildcraft.core.block;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.block.BlockBuildCraftTile_BC8;

public abstract class BlockMarkerBase extends BlockBuildCraftTile_BC8 {
    private static final Map<EnumFacing, AxisAlignedBB> BOUNDING_BOXES = new EnumMap<>(EnumFacing.class);

    static {
        double halfWidth = 0.1;
        double h = 0.65;
        // Little variables to make reading a *bit* more sane
        final double nw = 0.5 - halfWidth;
        final double pw = 0.5 + halfWidth;
        final double ih = 1 - h;
        BOUNDING_BOXES.put(EnumFacing.UP, new AxisAlignedBB(nw, ih, nw, pw, 1, pw));
        BOUNDING_BOXES.put(EnumFacing.DOWN, new AxisAlignedBB(nw, 0, nw, pw, h, pw));
        BOUNDING_BOXES.put(EnumFacing.NORTH, new AxisAlignedBB(nw, nw, 0, pw, pw, h));
        BOUNDING_BOXES.put(EnumFacing.SOUTH, new AxisAlignedBB(nw, nw, ih, pw, pw, 1));
        BOUNDING_BOXES.put(EnumFacing.WEST, new AxisAlignedBB(0, nw, nw, h, pw, pw));
        BOUNDING_BOXES.put(EnumFacing.EAST, new AxisAlignedBB(ih, nw, nw, 1, pw, pw));
    }

    public BlockMarkerBase(Material material, String id) {
        super(material, id);
        setDefaultState(getDefaultState().withProperty(BuildCraftProperties.BLOCK_FACING_6, EnumFacing.UP));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BuildCraftProperties.BLOCK_FACING_6);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BuildCraftProperties.BLOCK_FACING_6).getIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BuildCraftProperties.BLOCK_FACING_6, EnumFacing.getFront(meta));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, World world, BlockPos pos) {
        return null;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOXES.get(state.getValue(BuildCraftProperties.BLOCK_FACING_6));
    }
}
