/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.lib.misc.collect.OrderedEnumMap;
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

import java.util.function.Function;
import java.util.function.Predicate;

public class VanillaRotationHandlers {
    /* Player friendly rotations- these only rotate through sides that are touching (only 90 degree changes, in any
     * axis), rather than jumping around. */
    public static final OrderedEnumMap<EnumFacing> ROTATE_HORIZONTAL, ROTATE_FACING, ROTATE_TORCH;
    public static final OrderedEnumMap<EnumOrientation> ROTATE_LEVER;

    static {
        EnumFacing e = EnumFacing.EAST, w = EnumFacing.WEST;
        EnumFacing u = EnumFacing.UP, d = EnumFacing.DOWN;
        EnumFacing n = EnumFacing.NORTH, s = EnumFacing.SOUTH;
        ROTATE_HORIZONTAL = new OrderedEnumMap<>(EnumFacing.class, e, s, w, n);
        ROTATE_FACING = new OrderedEnumMap<>(EnumFacing.class, e, s, d, w, n, u);
        ROTATE_TORCH = new OrderedEnumMap<>(EnumFacing.class, e, s, w, n, u);

        EnumOrientation[] leverFaces = new EnumOrientation[8];
        int index = 0;
        for (EnumFacing face : ROTATE_FACING.getOrder()) {
            if (face == EnumFacing.DOWN) {
                leverFaces[index++] = EnumOrientation.DOWN_Z;
                leverFaces[index++] = EnumOrientation.DOWN_X;
            } else if (face == EnumFacing.UP) {
                leverFaces[index++] = EnumOrientation.UP_Z;
                leverFaces[index++] = EnumOrientation.UP_X;
            } else {
                leverFaces[index++] = EnumOrientation.forFacings(face, null);
            }
        }
        ROTATE_LEVER = new OrderedEnumMap<>(EnumOrientation.class, leverFaces);
    }

    public static void fmlInit() {
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockButton.class, VanillaRotationHandlers::rotateButton);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockDoor.class, VanillaRotationHandlers::rotateDoor);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockPistonBase.class, VanillaRotationHandlers::rotatePiston);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockLever.class, VanillaRotationHandlers::rotateLever);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockFenceGate.class, VanillaRotationHandlers::rotateGate);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockRedstoneDiode.class, VanillaRotationHandlers::rotateDiode);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockTorch.class, VanillaRotationHandlers::rotateTorch);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockChest.class, VanillaRotationHandlers::rotateChest);
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
                rotateOnce(world, pos, state, BlockDoor.FACING, ROTATE_HORIZONTAL);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateButton(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockButton) {// Just check to make sure we have the right block...
            IProperty<EnumFacing> prop = BlockDirectional.FACING;
            return rotateEnumFacing(world, pos, state, prop, ROTATE_FACING);
        } else {
            return EnumActionResult.PASS;
        }
    }

    private static EnumActionResult rotatePiston(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockPistonBase) {// Just check to make sure we have the right block...
            boolean extended = state.getValue(BlockPistonBase.EXTENDED);
            if (extended) return EnumActionResult.FAIL;
            return rotateOnce(world, pos, state, BlockDirectional.FACING, ROTATE_FACING);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateLever(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockLever) {
            return rotateAnyTypeAuto(world, pos, state, BlockLever.FACING, ROTATE_LEVER, EnumOrientation::getFacing);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateGate(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockFenceGate) {
            return rotateOnce(world, pos, state, BlockFenceGate.FACING, ROTATE_HORIZONTAL);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateDiode(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockRedstoneDiode) {
            return rotateOnce(world, pos, state, BlockRedstoneDiode.FACING, ROTATE_HORIZONTAL);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateTorch(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockTorch) {// Just check to make sure we have the right block...
            IProperty<EnumFacing> prop = BlockTorch.FACING;
            Predicate<EnumFacing> tester = toTry -> world.isSideSolid(pos.offset(toTry.getOpposite()), toTry);
            return rotateAnyTypeManual(world, pos, state, prop, ROTATE_TORCH, tester);
        } else {
            return EnumActionResult.PASS;
        }
    }

    private static EnumActionResult rotateChest(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        return EnumActionResult.PASS;// TODO (Limited rotation for double chests though)
    }

    public static EnumActionResult rotateEnumFacing(World world, BlockPos pos, IBlockState state, IProperty<EnumFacing> prop, OrderedEnumMap<EnumFacing> possible) {
        return rotateAnyTypeAuto(world, pos, state, prop, possible, f -> f);
    }

    public static <E extends Enum<E> & Comparable<E>> EnumActionResult rotateOnce
    //@formatter:off
        (
            World world,
            BlockPos pos,
            IBlockState state,
            IProperty<E> prop,
            OrderedEnumMap<E> possible
        )
    //@formatter:on
    {
        E current = state.getValue(prop);
        current = possible.next(current);
        world.setBlockState(pos, state.withProperty(prop, current));
        return EnumActionResult.SUCCESS;
    }

    public static <E extends Enum<E> & Comparable<E>> EnumActionResult rotateAnyTypeAuto
    //@formatter:off
        (
            World world,
            BlockPos pos,
            IBlockState state,
            IProperty<E> prop,
            OrderedEnumMap<E> possible,
            Function<E, EnumFacing> mapper
        )
    //@formatter:on
    {
        Predicate<E> tester = toTry -> state.getBlock().canPlaceBlockOnSide(world, pos, mapper.apply(toTry));
        return rotateAnyTypeManual(world, pos, state, prop, possible, tester);
    }

    public static <E extends Enum<E> & Comparable<E>> EnumActionResult rotateAnyTypeManual
    //@formatter:off
        (
            World world,
            BlockPos pos,
            IBlockState state,
            IProperty<E> prop,
            OrderedEnumMap<E> possible,
            Predicate<E> canPlace
        )
    //@formatter:on
    {
        E current = state.getValue(prop);
        for (int i = possible.getOrderLength(); i > 1; i--) {
            current = possible.next(current);
            if (canPlace.test(current)) {
                world.setBlockState(pos, state.withProperty(prop, current));
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.FAIL;
    }
}
