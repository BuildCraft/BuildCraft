/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoor.EnumDoorHalf;
import net.minecraft.block.BlockDoor.EnumHingePosition;
import net.minecraft.block.BlockEndRod;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockGlazedTerracotta;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLever.EnumOrientation;
import net.minecraft.block.BlockObserver;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.blocks.ICustomRotationHandler;

import buildcraft.lib.misc.collect.OrderedEnumMap;

public class VanillaRotationHandlers {
    /* Player friendly rotations- these only rotate through sides that are touching (only 90 degree changes, in any
     * axis), rather than jumping around. */
    public static final OrderedEnumMap<EnumFacing> ROTATE_HORIZONTAL, ROTATE_FACING, ROTATE_TORCH, ROTATE_HOPPER;
    public static final OrderedEnumMap<EnumOrientation> ROTATE_LEVER;

    static {
        EnumFacing e = EnumFacing.EAST, w = EnumFacing.WEST;
        EnumFacing u = EnumFacing.UP, d = EnumFacing.DOWN;
        EnumFacing n = EnumFacing.NORTH, s = EnumFacing.SOUTH;
        ROTATE_HORIZONTAL = new OrderedEnumMap<>(EnumFacing.class, e, s, w, n);
        ROTATE_FACING = new OrderedEnumMap<>(EnumFacing.class, e, s, d, w, n, u);
        ROTATE_TORCH = new OrderedEnumMap<>(EnumFacing.class, e, s, w, n, u);
        ROTATE_HOPPER = new OrderedEnumMap<>(EnumFacing.class, e, s, w, n, d);

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
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockTripWireHook.class, VanillaRotationHandlers::rotateTripWireHook);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockDoor.class, VanillaRotationHandlers::rotateDoor);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockPistonBase.class, VanillaRotationHandlers::rotatePiston);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockLever.class, VanillaRotationHandlers::rotateLever);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockShulkerBox.class, VanillaRotationHandlers::rotateShulkerBox);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockDispenser.class, getHandlerFreely(BlockDispenser.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockObserver.class, getHandlerFreely(BlockObserver.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockEndRod.class, getHandlerFreely(BlockEndRod.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockFenceGate.class, getHandlerHorizontalFreely(BlockFenceGate.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockRedstoneDiode.class, getHandlerHorizontalFreely(BlockRedstoneDiode.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockPumpkin.class, getHandlerHorizontalFreely(BlockPumpkin.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockGlazedTerracotta.class, getHandlerHorizontalFreely(BlockGlazedTerracotta.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockAnvil.class, getHandlerHorizontalFreely(BlockAnvil.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockEnderChest.class, getHandlerHorizontalFreely(BlockEnderChest.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockFurnace.class, getHandlerHorizontalFreely(BlockFurnace.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockCocoa.class, VanillaRotationHandlers::rotateCocoa);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockTorch.class, VanillaRotationHandlers::rotateTorch);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockLadder.class, VanillaRotationHandlers::rotateLadder);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockHopper.class, VanillaRotationHandlers::rotateHopper);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockChest.class, VanillaRotationHandlers::rotateChest);
        // TODO: DoubleChest, BannerHanging, BannerStanding, Bed, DoublePlant, EndPortalFrame (?), Stairs, TrapDoor, WallSign, StandingSign, Skull, CommandBlock (?)
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
        if (state.getBlock() instanceof BlockButton) {
            return rotateEnumFacing(world, pos, state, BlockButton.FACING, ROTATE_FACING);
        } else {
            return EnumActionResult.PASS;
        }
    }

    private static EnumActionResult rotateTripWireHook(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockTripWireHook) {
            return rotateEnumFacing(world, pos, state, BlockTripWireHook.FACING, ROTATE_HORIZONTAL);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotatePiston(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockPistonBase) {
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

    private static EnumActionResult rotateHopper(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockHopper) {
            return rotateOnce(world, pos, state, BlockHopper.FACING, ROTATE_HOPPER);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateShulkerBox(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockShulkerBox) {
            return rotateOnce(world, pos, state, BlockShulkerBox.FACING, ROTATE_FACING);
        }
        return EnumActionResult.PASS;
    }

    private static ICustomRotationHandler getHandlerFreely(Class<? extends Block> blockClass) {
        return (world, pos, state, sideWrenched) -> rotateFreely(world, pos, state, sideWrenched, blockClass);
    }

    private static EnumActionResult rotateFreely(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched, Class<? extends Block> blockClass) {
        if (blockClass.isInstance(state.getBlock())) {
            return rotateOnce(world, pos, state, BlockDirectional.FACING, ROTATE_FACING);
        }
        return EnumActionResult.PASS;
    }

    private static ICustomRotationHandler getHandlerHorizontalFreely(Class<? extends Block> blockClass) {
        return (world, pos, state, sideWrenched) -> rotateHorizontalFreely(world, pos, state, sideWrenched, blockClass);
    }

    private static EnumActionResult rotateHorizontalFreely(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched, Class<? extends Block> blockClass) {
        if (blockClass.isInstance(state.getBlock())) {
            return rotateOnce(world, pos, state, BlockHorizontal.FACING, ROTATE_HORIZONTAL);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateCocoa(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockCocoa) {
            return rotateAnyTypeManual(world, pos, state, BlockCocoa.FACING, ROTATE_HORIZONTAL, toTry -> ((BlockCocoa) state.getBlock()).canBlockStay(world, pos, state.withProperty(BlockCocoa.FACING, toTry)));
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateLadder(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockLadder) {
            Predicate<EnumFacing> tester = toTry -> {
                BlockPos offsetPos = pos.offset(toTry.getOpposite());
                IBlockState offsetState = world.getBlockState(offsetPos);
                return !offsetState.canProvidePower() && offsetState.getBlockFaceShape(world, offsetPos, toTry) == BlockFaceShape.SOLID && !BlockBCBase_Neptune.isExceptBlockForAttachWithPiston(offsetState.getBlock());
            };
            return rotateAnyTypeManual(world, pos, state, BlockLadder.FACING, ROTATE_HORIZONTAL, tester);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateTorch(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockTorch) {
            IProperty<EnumFacing> prop = BlockTorch.FACING;
            Predicate<EnumFacing> tester = toTry -> {
                BlockPos offsetPos = pos.offset(toTry.getOpposite());
                IBlockState offsetState = world.getBlockState(offsetPos);

                if (toTry == EnumFacing.UP && offsetState.getBlock().canPlaceTorchOnTop(state, world, offsetPos)) {
                    return true;
                } else if (toTry != EnumFacing.UP && toTry != EnumFacing.DOWN) {
                    return offsetState.getBlockFaceShape(world, offsetPos, toTry) == BlockFaceShape.SOLID && !BlockBCBase_Neptune.isExceptBlockForAttachWithPiston(offsetState.getBlock());
                }
                return false;
            };
            return rotateAnyTypeManual(world, pos, state, prop, ROTATE_TORCH, tester);
        } else {
            return EnumActionResult.PASS;
        }
    }

    private static EnumActionResult rotateChest(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockChest) {

            boolean doubleChest = false;
            for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
                if (world.getBlockState(pos.offset(facing)).getBlock() == state.getBlock()) {
                    doubleChest = true;
                    break;
                }
            }

            if (doubleChest) {
                // TODO: Limited rotation for double chests
            } else {
                return rotateOnce(world, pos, state, BlockChest.FACING, ROTATE_HORIZONTAL);
            }
        }
        return EnumActionResult.PASS;
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
