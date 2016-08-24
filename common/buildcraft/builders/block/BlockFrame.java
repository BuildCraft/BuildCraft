package buildcraft.builders.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.rotation.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BlockFrame extends BlockBCBase_Neptune {
    public static final BuildCraftProperty<Boolean> CONNECTED_UP = BuildCraftProperties.CONNECTED_UP;
    public static final BuildCraftProperty<Boolean> CONNECTED_DOWN = BuildCraftProperties.CONNECTED_DOWN;
    public static final BuildCraftProperty<Boolean> CONNECTED_EAST = BuildCraftProperties.CONNECTED_EAST;
    public static final BuildCraftProperty<Boolean> CONNECTED_WEST = BuildCraftProperties.CONNECTED_WEST;
    public static final BuildCraftProperty<Boolean> CONNECTED_NORTH = BuildCraftProperties.CONNECTED_NORTH;
    public static final BuildCraftProperty<Boolean> CONNECTED_SOUTH = BuildCraftProperties.CONNECTED_SOUTH;

    public static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(4 / 16D, 4 / 16D, 4 / 16D, 12 / 16D, 12 / 16D, 12 / 16D);
    public static final AxisAlignedBB CONNECTION_AABB = new AxisAlignedBB(4 / 16D, 0 / 16D, 4 / 16D, 12 / 16D, 4 / 16D, 12 / 16D);

    public BlockFrame(Material material, String id) {
        super(material, id);
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(CONNECTED_UP);
        properties.add(CONNECTED_DOWN);
        properties.add(CONNECTED_EAST);
        properties.add(CONNECTED_WEST);
        properties.add(CONNECTED_NORTH);
        properties.add(CONNECTED_SOUTH);
    }

    private boolean isConnected(IBlockAccess world, BlockPos pos, EnumFacing side) {
        Block block = world.getBlockState(pos.offset(side)).getBlock();
        return block instanceof BlockFrame || block instanceof BlockQuarry;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state
                .withProperty(CONNECTED_UP, isConnected(world, pos, EnumFacing.UP))
                .withProperty(CONNECTED_DOWN, isConnected(world, pos, EnumFacing.DOWN))
                .withProperty(CONNECTED_EAST, isConnected(world, pos, EnumFacing.EAST))
                .withProperty(CONNECTED_WEST, isConnected(world, pos, EnumFacing.WEST))
                .withProperty(CONNECTED_NORTH, isConnected(world, pos, EnumFacing.NORTH))
                .withProperty(CONNECTED_SOUTH, isConnected(world, pos, EnumFacing.SOUTH))
                ;
    }

    @Override
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
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        EnumFacing[] facings = Stream.of(EnumFacing.values()).filter(facing -> isConnected(world, pos, facing)).toArray(EnumFacing[]::new);
        if(facings.length == 1) {
            return side != facings[0];
        } else if(facings.length == 2 && facings[0] == facings[1].getOpposite()) {
            return side != facings[0] && side != facings[1];
        }
        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        AxisAlignedBB result = BASE_AABB;
        for(EnumFacing facing : Stream.of(EnumFacing.values()).filter(side -> isConnected(world, pos, side)).toArray(EnumFacing[]::new)) {
            result = result.union(RotationUtils.rotateAABB(CONNECTION_AABB, facing));
        }
        return result;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity) {
        List<AxisAlignedBB> boxes = new ArrayList<>();
        boxes.add(BASE_AABB);
        for(EnumFacing facing : Stream.of(EnumFacing.values()).filter(side -> isConnected(world, pos, side)).toArray(EnumFacing[]::new)) {
            boxes.add(RotationUtils.rotateAABB(CONNECTION_AABB, facing));
        }
        for(AxisAlignedBB box : boxes) {
            AxisAlignedBB boxWithOffset = box.offset(pos);
            if(entityBox.intersectsWith(boxWithOffset)) {
                collidingBoxes.add(boxWithOffset);
            }
        }
    }
}
