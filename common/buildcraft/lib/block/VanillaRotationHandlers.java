package buildcraft.lib.block;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.block.*;
import net.minecraft.block.BlockDoor.EnumDoorHalf;
import net.minecraft.block.BlockDoor.EnumHingePosition;
import net.minecraft.block.BlockLever.EnumOrientation;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.blocks.CustomRotationHelper;

public class VanillaRotationHandlers {
    /* Player friendly rotations- these only rotate through sides that are touching (only 90 degree changes, in any
     * axis), rather than jumping around. */
    private static final EnumFacing[] HORIZONTALS = { EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH };
    private static final EnumFacing[] ALL_SIDES = { EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.UP };
    private static final EnumFacing[] TORCH_FACES = { EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.UP };
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

    public static EnumFacing[] getAllSidesArray() {
        return Arrays.copyOf(ALL_SIDES, 6, EnumFacing[].class);
    }

    public static void fmlInit() {
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockButton.class, VanillaRotationHandlers::rotateButton);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockDoor.class, VanillaRotationHandlers::rotateDoor);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockPistonBase.class, VanillaRotationHandlers::rotatePiston);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockLever.class, VanillaRotationHandlers::rotateLever);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockTorch.class, VanillaRotationHandlers::rotateTorch);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockChest.class, VanillaRotationHandlers::rotateChest);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockBed.class, VanillaRotationHandlers::rotateBed);
    }

    public static <T> int getOrdinal(T side, T[] array) {
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
            IProperty<EnumFacing> prop = BlockDirectional.FACING;
            return rotateEnumFacing(world, pos, state, prop, ALL_SIDES);
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
            IProperty<EnumOrientation> prop = BlockLever.FACING;
            return rotateAnyTypeAuto(world, pos, state, prop, LEVER_FACES, EnumOrientation::getFacing);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateTorch(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockTorch) {// Just check to make sure we have the right block...
            IProperty<EnumFacing> prop = BlockTorch.FACING;
            Predicate<EnumFacing> tester = toTry -> world.isSideSolid(pos.offset(toTry.getOpposite()), toTry);
            return rotateAnyTypeManual(world, pos, state, prop, TORCH_FACES, tester);
        } else {
            return EnumActionResult.PASS;
        }
    }

    private static EnumActionResult rotateChest(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        return EnumActionResult.PASS;// TODO (Limited rotation for double chests though)
    }

    private static EnumActionResult rotateBed(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        return EnumActionResult.PASS;// TODO (We can rotate the bed. Lookup what can be changed though)
    }

    public static EnumActionResult rotateEnumFacing(World world, BlockPos pos, IBlockState state, IProperty<EnumFacing> prop, EnumFacing[] possible) {
        return rotateAnyTypeAuto(world, pos, state, prop, possible, f -> f);
    }

    public static <T extends Comparable<T>> EnumActionResult rotateAnyTypeAuto
    //@formatter:off
        (
            World world,
            BlockPos pos,
            IBlockState state,
            IProperty<T> prop,
            T[] possible,
            Function<T, EnumFacing> mapper
        )
    //@formatter:on
    {
        Predicate<T> tester = toTry -> state.getBlock().canPlaceBlockOnSide(world, pos, mapper.apply(toTry));
        return rotateAnyTypeManual(world, pos, state, prop, possible, tester);
    }

    public static <T extends Comparable<T>> EnumActionResult rotateAnyTypeManual
    //@formatter:off
        (
            World world,
            BlockPos pos,
            IBlockState state,
            IProperty<T> prop,
            T[] possible,
            Predicate<T> canPlace
        )
    //@formatter:on
    {
        T current = state.getValue(prop);
        int ord = getOrdinal(current, possible);
        for (int i = 1; i < possible.length; i++) {
            int next = (ord + i) % possible.length;
            T toTry = possible[next];
            if (canPlace.test(toTry)) {
                world.setBlockState(pos, state.withProperty(prop, toTry));
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.FAIL;
    }
}
