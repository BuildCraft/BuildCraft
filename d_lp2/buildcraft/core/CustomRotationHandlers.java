package buildcraft.core;

import net.minecraft.block.*;
import net.minecraft.block.BlockDoor.EnumDoorHalf;
import net.minecraft.block.BlockDoor.EnumHingePosition;
import net.minecraft.block.BlockLever.EnumOrientation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.blocks.CustomRotationHelper;

public class CustomRotationHandlers {
    /* Player friendly rotations- these only rotate through sides that are touching (only 90 degree changes, in any
     * axis), rather than jumping around. */
    private static final EnumFacing[] HORIZONTALS = { EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH };
    private static final EnumFacing[] ALL_SIDES = { EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.UP };
    private static final EnumOrientation[] LEVER_FACES = new EnumOrientation[8];

    static {
        int lever = 0;
        for (EnumFacing face : ALL_SIDES) {
            if (face == EnumFacing.DOWN) {
                LEVER_FACES[lever++] = EnumOrientation.DOWN_Z;
                LEVER_FACES[lever++] = EnumOrientation.DOWN_X;
            } else if (face == EnumFacing.UP) {
                LEVER_FACES[lever++] = EnumOrientation.UP_Z;
                LEVER_FACES[lever++] = EnumOrientation.UP_X;
            } else {
                LEVER_FACES[lever++] = EnumOrientation.forFacings(face, null);
            }
        }
    }

    public static void init() {
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockButton.class, CustomRotationHandlers::rotateButton);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockDoor.class, CustomRotationHandlers::rotateDoor);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockPistonBase.class, CustomRotationHandlers::rotatePiston);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockLever.class, CustomRotationHandlers::rotateLever);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockChest.class, CustomRotationHandlers::rotateChest);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockBed.class, CustomRotationHandlers::rotateBed);
    }

    private static <T> int getOrdinal(T side, T[] array) {
        for (int i = 0; i < array.length; i++) {
            if (side == array[i]) return i;
        }
        return 0;
    }

    private static EnumActionResult rotateDoor(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockDoor) {// Just check to make sure we have the right block...
            EnumDoorHalf half = state.getValue(BlockDoor.HALF);
            if (half == EnumDoorHalf.UPPER) {
                EnumHingePosition hinge = state.getValue(BlockDoor.HINGE);
                if (hinge == EnumHingePosition.LEFT) hinge = EnumHingePosition.RIGHT;
                else hinge = EnumHingePosition.LEFT;
                world.setBlockState(pos, state.withProperty(BlockDoor.HINGE, hinge));
            } else {// Lower
                EnumFacing oldFacing = state.getValue(BlockDoor.FACING);
                int ordinal = getOrdinal(oldFacing, HORIZONTALS) + 1;
                world.setBlockState(pos, state.withProperty(BlockDoor.FACING, HORIZONTALS[ordinal % 4]));
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateButton(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockButton) {// Just check to make sure we have the right block...
            BlockButton button = (BlockButton) state.getBlock();
            EnumFacing currentSide = state.getValue(BlockDirectional.FACING);
            int ord = getOrdinal(currentSide, ALL_SIDES);
            for (int i = 1; i < 6; i++) {
                int next = (ord + i) % 6;
                EnumFacing toTry = ALL_SIDES[next];
                if (button.canPlaceBlockOnSide(world, pos, toTry)) {
                    world.setBlockState(pos, state.withProperty(BlockDirectional.FACING, toTry));
                    return EnumActionResult.SUCCESS;
                }
            }
            return EnumActionResult.FAIL;
        } else {
            return EnumActionResult.PASS;
        }
    }

    private static EnumActionResult rotatePiston(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockPistonBase) {// Just check to make sure we have the right block...
            boolean extended = state.getValue(BlockPistonBase.EXTENDED);
            if (extended) return EnumActionResult.FAIL;
            EnumFacing currentSide = state.getValue(BlockDirectional.FACING);
            int ord = getOrdinal(currentSide, ALL_SIDES) + 1;
            world.setBlockState(pos, state.withProperty(BlockDirectional.FACING, ALL_SIDES[ord % 6]));
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateLever(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockLever) {
            BlockLever lever = (BlockLever) state.getBlock();
            EnumOrientation currentSide = state.getValue(BlockLever.FACING);
            int ord = getOrdinal(currentSide, LEVER_FACES);
            for (int i = 1; i < 8; i++) {
                int next = (ord + i) % 8;
                EnumOrientation toTry = LEVER_FACES[next];
                if (lever.canPlaceBlockOnSide(world, pos, toTry.getFacing())) {
                    world.setBlockState(pos, state.withProperty(BlockLever.FACING, toTry));
                    return EnumActionResult.SUCCESS;
                }
            }
            return EnumActionResult.FAIL;
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateChest(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        return EnumActionResult.PASS;// TODO
    }

    private static EnumActionResult rotateBed(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        return EnumActionResult.PASS;// TODO (We can rotate the base)
    }
}
