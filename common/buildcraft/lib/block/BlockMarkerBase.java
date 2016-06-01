package buildcraft.lib.block;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.tile.TileMarker;

public abstract class BlockMarkerBase extends BlockBCTile_Neptune implements ICustomRotationHandler {
    private static final Map<EnumFacing, AxisAlignedBB> BOUNDING_BOXES = new EnumMap<>(EnumFacing.class);
    private static final EnumFacing[] ALL_SIDES = VanillaRotationHandlers.getAllSidesArray();

    static {
        double halfWidth = 0.1;
        double h = 0.65;
        // Little variables to make reading a *bit* more sane
        final double nw = 0.5 - halfWidth;
        final double pw = 0.5 + halfWidth;
        final double ih = 1 - h;
        BOUNDING_BOXES.put(EnumFacing.DOWN, new AxisAlignedBB(nw, ih, nw, pw, 1, pw));
        BOUNDING_BOXES.put(EnumFacing.UP, new AxisAlignedBB(nw, 0, nw, pw, h, pw));
        BOUNDING_BOXES.put(EnumFacing.SOUTH, new AxisAlignedBB(nw, nw, 0, pw, pw, h));
        BOUNDING_BOXES.put(EnumFacing.NORTH, new AxisAlignedBB(nw, nw, ih, pw, pw, 1));
        BOUNDING_BOXES.put(EnumFacing.EAST, new AxisAlignedBB(0, nw, nw, h, pw, pw));
        BOUNDING_BOXES.put(EnumFacing.WEST, new AxisAlignedBB(ih, nw, nw, 1, pw, pw));
    }

    public BlockMarkerBase(Material material, String id) {
        super(material, id);

        IBlockState defaultState = getDefaultState();
        defaultState = defaultState.withProperty(BuildCraftProperties.BLOCK_FACING_6, EnumFacing.UP);
        defaultState = defaultState.withProperty(BuildCraftProperties.ACTIVE, false);
        setDefaultState(defaultState);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BuildCraftProperties.BLOCK_FACING_6, BuildCraftProperties.ACTIVE);
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
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileMarker) {
            TileMarker<?> marker = (TileMarker<?>) tile;
            state = state.withProperty(BuildCraftProperties.ACTIVE, marker.isActiveForRender());
        }
        return state;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
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

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState state = getDefaultState();
        state = state.withProperty(BuildCraftProperties.BLOCK_FACING_6, facing);
        return state;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return world.isSideSolid(pos.offset(side.getOpposite()), side);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
        if (state.getBlock() != this) {
            return;
        }
        EnumFacing sideOn = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
        if (!canPlaceBlockOnSide(world, pos, sideOn)) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public EnumActionResult attemptRotation(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockMarkerBase) {// Just check to make sure we have the right block...
            IProperty<EnumFacing> prop = BuildCraftProperties.BLOCK_FACING_6;
            return VanillaRotationHandlers.rotateEnumFacing(world, pos, state, prop, ALL_SIDES);
        } else {
            return EnumActionResult.PASS;
        }
    }
}
