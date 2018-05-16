/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
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
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

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
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockTrapDoor.class, VanillaRotationHandlers::rotateTrapDoor);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockStairs.class, VanillaRotationHandlers::rotateStairs);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockSkull.class, VanillaRotationHandlers::rotateSkull);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockBanner.BlockBannerHanging.class, VanillaRotationHandlers::rotateHangingBanner);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockWallSign.class, VanillaRotationHandlers::rotateWallSign);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockBanner.BlockBannerStanding.class, VanillaRotationHandlers::rotateStandingBanner);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BlockStandingSign.class, VanillaRotationHandlers::rotateStandingSign);
    }

    public static <T> int getOrdinal(T side, T[] array) {
        for (int i = 0; i < array.length; i++) {
            if (side == array[i]) return i;
        }
        return 0;
    }

    private static EnumActionResult rotateDoor(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockDoor) {
            BlockPos upperPos, lowerPos;
            IBlockState upperState, lowerState;

            if (state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER) {
                upperPos = pos;
                upperState = state;
                lowerPos = upperPos.down();
                lowerState = world.getBlockState(lowerPos);
                if (!(lowerState.getBlock() instanceof BlockDoor)) {
                    return EnumActionResult.PASS;
                }
            } else {
                lowerPos = pos;
                lowerState = state;
                upperPos = lowerPos.up();
                upperState = world.getBlockState(upperPos);
                if (!(upperState.getBlock() instanceof BlockDoor)) {
                    return EnumActionResult.PASS;
                }
            }

            if (lowerState.getValue(BlockDoor.FACING) == ROTATE_HORIZONTAL.get(0)) {
                BlockDoor.EnumHingePosition hinge = upperState.getValue(BlockDoor.HINGE);
                if (hinge == BlockDoor.EnumHingePosition.LEFT) {
                    hinge = BlockDoor.EnumHingePosition.RIGHT;
                } else {
                    hinge = BlockDoor.EnumHingePosition.LEFT;
                }
                world.setBlockState(upperPos, upperState.withProperty(BlockDoor.HINGE, hinge));
            }

            return rotateOnce(world, lowerPos, lowerState, BlockTrapDoor.FACING, ROTATE_HORIZONTAL);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateButton(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockButton) {
            return rotateEnumFacing(world, pos, state, BlockButton.FACING, ROTATE_FACING);
        }
        return EnumActionResult.PASS;
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
            return rotateAnyTypeManual(world, pos, state, BlockTorch.FACING, ROTATE_TORCH, tester);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateChest(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockChest) {
            BlockPos otherPos = null;
            for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
                BlockPos candidate = pos.offset(facing);
                if (world.getBlockState(candidate).getBlock() == state.getBlock()) {
                    otherPos = candidate;
                    break;
                }
            }

            if (otherPos != null) {
                IBlockState otherState = world.getBlockState(otherPos);
                EnumFacing facing = state.getValue(BlockChest.FACING);
                if (otherState.getValue(BlockChest.FACING) == facing) {
                    world.setBlockState(pos, state.withProperty(BlockChest.FACING, facing.getOpposite()));
                    world.setBlockState(otherPos, otherState.withProperty(BlockChest.FACING, facing.getOpposite()));
                    return EnumActionResult.SUCCESS;
                }
            }

            return rotateOnce(world, pos, state, BlockChest.FACING, ROTATE_HORIZONTAL);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateTrapDoor(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockTrapDoor) {

            if (state.getValue(BlockTrapDoor.FACING) == ROTATE_HORIZONTAL.get(0)) {
                BlockTrapDoor.DoorHalf half = state.getValue(BlockTrapDoor.HALF);
                if (half == BlockTrapDoor.DoorHalf.TOP) {
                    half = BlockTrapDoor.DoorHalf.BOTTOM;
                } else {
                    half = BlockTrapDoor.DoorHalf.TOP;
                }
                state = state.withProperty(BlockTrapDoor.HALF, half);
            }

            return rotateOnce(world, pos, state, BlockTrapDoor.FACING, ROTATE_HORIZONTAL);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateStairs(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockStairs) {

            if (state.getValue(BlockStairs.FACING) == ROTATE_HORIZONTAL.get(0)) {
                BlockStairs.EnumHalf half = state.getValue(BlockStairs.HALF);
                if (half == BlockStairs.EnumHalf.TOP) {
                    half = BlockStairs.EnumHalf.BOTTOM;
                } else {
                    half = BlockStairs.EnumHalf.TOP;
                }
                state = state.withProperty(BlockStairs.HALF, half);
            }

            return rotateOnce(world, pos, state, BlockStairs.FACING, ROTATE_HORIZONTAL);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateSkull(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockSkull) {

            if (state.getValue(BlockSkull.FACING).getAxis().isVertical()) {
                TileEntity tile = world.getTileEntity(pos);
                if (tile instanceof TileEntitySkull) {
                    TileEntitySkull tileSkull = (TileEntitySkull) tile;

                    int rot = ObfuscationReflectionHelper.getPrivateValue(TileEntitySkull.class, tileSkull, "skullRotation", "field_" + "145910_i");
                    rot = (rot + 1) % 16;

                    tileSkull.setSkullRotation(rot);
                    tileSkull.markDirty();
                    world.notifyBlockUpdate(pos, state, state, 3);

                    return EnumActionResult.SUCCESS;
                }
                return EnumActionResult.PASS;
            }

            return rotateOnce(world, pos, state, BlockSkull.FACING, ROTATE_HORIZONTAL);
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateHangingBanner(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockBanner.BlockBannerHanging) {
            return rotateAnyTypeManual(world, pos, state, BlockBanner.FACING, ROTATE_HORIZONTAL, toTry -> world.getBlockState(pos.offset(toTry.getOpposite())).getMaterial().isSolid());
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateWallSign(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockWallSign) {
            return rotateAnyTypeManual(world, pos, state, BlockWallSign.FACING, ROTATE_HORIZONTAL, toTry -> world.getBlockState(pos.offset(toTry.getOpposite())).getMaterial().isSolid());
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateStandingBanner(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockBanner.BlockBannerStanding) {
            world.setBlockState(pos, state.withProperty(BlockBanner.ROTATION, (state.getValue(BlockBanner.ROTATION) + 1) % 16));
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    private static EnumActionResult rotateStandingSign(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (state.getBlock() instanceof BlockStandingSign) {
            world.setBlockState(pos, state.withProperty(BlockStandingSign.ROTATION, (state.getValue(BlockStandingSign.ROTATION) + 1) % 16));
            return EnumActionResult.SUCCESS;
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
