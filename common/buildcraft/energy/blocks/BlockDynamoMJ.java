package buildcraft.energy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import buildcraft.api.blocks.ICustomRotationHandler;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.energy.tile.TileDynamoMJ;

public class BlockDynamoMJ extends BlockBCTile_Neptune implements ICustomRotationHandler {

    public BlockDynamoMJ(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(World world, BlockState state) {
        return new TileDynamoMJ();
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullBlock(BlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(BlockView world, BlockState state, BlockPos pos, Direction side) {
        BlockEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            if (side == engine.getCurrentDirection().getOpposite()) {
                return BlockFaceShape.SOLID;
            } else {
                return BlockFaceShape.UNDEFINED;
            }
        }
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @Deprecated
    public boolean isSideSolid(BlockState base_state, BlockView world, BlockPos pos, Direction side) {
        BlockEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            return side == engine.getCurrentDirection().getOpposite();
        }
        return false;
    }

    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        if (world.isRemote) return;
        BlockEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            engine.rotateIfInvalid();
        }
    }

    // ICustomRotationHandler

    @Override
    public ActionResult attemptRotation(World world, BlockPos pos, BlockState state, Direction sideWrenched) {
        BlockEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            return engine.attemptRotation();
        }
        return ActionResult.FAIL;
    }
}
