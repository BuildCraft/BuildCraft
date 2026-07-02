/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.lib.block;

import java.util.function.Function;
import java.util.function.Predicate;

import ct.buildcraft.api.blocks.CustomRotationHelper;
import ct.buildcraft.api.blocks.ICustomRotationHandler;
import ct.buildcraft.lib.misc.collect.OrderedEnumMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.StoneButtonBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WoodButtonBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;

public class VanillaRotationHandlers {
    /* Player friendly rotations- these only rotate through sides that are touching (only 90 degree changes, in any
     * axis), rather than jumping around. */
    public static final OrderedEnumMap<Direction> ROTATE_HORIZONTAL, ROTATE_FACING, ROTATE_TORCH, ROTATE_HOPPER;

    static {
        Direction e = Direction.EAST, w = Direction.WEST;
        Direction u = Direction.UP, d = Direction.DOWN;
        Direction n = Direction.NORTH, s = Direction.SOUTH;
        ROTATE_HORIZONTAL = new OrderedEnumMap<>(Direction.class, e, s, w, n);
        ROTATE_FACING = new OrderedEnumMap<>(Direction.class, e, s, d, w, n, u);
        ROTATE_TORCH = new OrderedEnumMap<>(Direction.class, e, s, w, n, u);
        ROTATE_HOPPER = new OrderedEnumMap<>(Direction.class, e, s, w, n, d);



    }

    public static void fmlInit() {
        CustomRotationHelper.INSTANCE.registerHandler(StoneButtonBlock.class, VanillaRotationHandlers::rotateButton);
        CustomRotationHelper.INSTANCE.registerHandler(WoodButtonBlock.class, VanillaRotationHandlers::rotateButton);
        CustomRotationHelper.INSTANCE.registerHandler(TripWireHookBlock.class, VanillaRotationHandlers::rotateTripWireHook);
        CustomRotationHelper.INSTANCE.registerHandler(DoorBlock.class, VanillaRotationHandlers::rotateDoor);
        CustomRotationHelper.INSTANCE.registerHandler(PistonBaseBlock.class, VanillaRotationHandlers::rotatePiston);
//        CustomRotationHelper.INSTANCE.registerHandler(LeverBlock.class, VanillaRotationHandlers::rotateLever);
        CustomRotationHelper.INSTANCE.registerHandler(ShulkerBoxBlock.class, VanillaRotationHandlers::rotateShulkerBox);
        CustomRotationHelper.INSTANCE.registerHandler(DispenserBlock.class, getHandlerFreely(DispenserBlock.class));
        CustomRotationHelper.INSTANCE.registerHandler(ObserverBlock.class, getHandlerFreely(ObserverBlock.class));
        CustomRotationHelper.INSTANCE.registerHandler(EndRodBlock.class, getHandlerFreely(EndRodBlock.class));
        CustomRotationHelper.INSTANCE.registerHandler(FenceGateBlock.class, getHandlerHorizontalFreely(FenceGateBlock.class));
        CustomRotationHelper.INSTANCE.registerHandler(RepeaterBlock.class, getHandlerHorizontalFreely(RepeaterBlock.class));
        CustomRotationHelper.INSTANCE.registerHandler(ComparatorBlock.class, getHandlerHorizontalFreely(ComparatorBlock.class));
        CustomRotationHelper.INSTANCE.registerHandler(PumpkinBlock.class, getHandlerHorizontalFreely(PumpkinBlock.class));
        CustomRotationHelper.INSTANCE.registerHandler(GlazedTerracottaBlock.class, getHandlerHorizontalFreely(GlazedTerracottaBlock.class));
        CustomRotationHelper.INSTANCE.registerHandler(AnvilBlock.class, getHandlerHorizontalFreely(AnvilBlock.class));
        CustomRotationHelper.INSTANCE.registerHandler(EnderChestBlock.class, getHandlerHorizontalFreely(EnderChestBlock.class));
        CustomRotationHelper.INSTANCE.registerHandler(FurnaceBlock.class, getHandlerHorizontalFreely(FurnaceBlock.class));
        CustomRotationHelper.INSTANCE.registerHandler(CocoaBlock.class, VanillaRotationHandlers::rotateCocoa);
//        CustomRotationHelper.INSTANCE.registerHandler(TorchBlock.class, VanillaRotationHandlers::rotateTorch);
        CustomRotationHelper.INSTANCE.registerHandler(LadderBlock.class, VanillaRotationHandlers::rotateLadder);
        CustomRotationHelper.INSTANCE.registerHandler(HopperBlock.class, VanillaRotationHandlers::rotateHopper);
        CustomRotationHelper.INSTANCE.registerHandler(ChestBlock.class, VanillaRotationHandlers::rotateChest);
        CustomRotationHelper.INSTANCE.registerHandler(TrapDoorBlock.class, VanillaRotationHandlers::rotateTrapDoor);
        CustomRotationHelper.INSTANCE.registerHandler(StairBlock.class, VanillaRotationHandlers::rotateStairs);
        CustomRotationHelper.INSTANCE.registerHandler(SkullBlock.class, VanillaRotationHandlers::rotateSkull);
        CustomRotationHelper.INSTANCE.registerHandler(WallBannerBlock.class, VanillaRotationHandlers::rotateHangingBanner);
        CustomRotationHelper.INSTANCE.registerHandler(WallSignBlock.class, VanillaRotationHandlers::rotateWallSign);
        CustomRotationHelper.INSTANCE.registerHandler(BannerBlock.class, VanillaRotationHandlers::rotateStandingBanner);
        CustomRotationHelper.INSTANCE.registerHandler(StandingSignBlock.class, VanillaRotationHandlers::rotateStandingSign);
    }

    public static <T> int getOrdinal(T side, T[] array) {
        for (int i = 0; i < array.length; i++) {
            if (side == array[i]) return i;
        }
        return 0;
    }

    private static InteractionResult rotateDoor(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof DoorBlock) {
            BlockPos upperPos, lowerPos;
            BlockState upperState, lowerState;

            if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                upperPos = pos;
                upperState = state;
                lowerPos = upperPos.below();
                lowerState = world.getBlockState(lowerPos);
                if (!(lowerState.getBlock() instanceof DoorBlock)) {
                    return InteractionResult.PASS;
                }
            } else {
                lowerPos = pos;
                lowerState = state;
                upperPos = lowerPos.above();
                upperState = world.getBlockState(upperPos);
                if (!(upperState.getBlock() instanceof DoorBlock)) {
                    return InteractionResult.PASS;
                }
            }

            if (lowerState.getValue(DoorBlock.FACING) == ROTATE_HORIZONTAL.get(0)) {
            	DoorHingeSide hinge = upperState.getValue(DoorBlock.HINGE);
                if (hinge == DoorHingeSide.LEFT) {
                    hinge = DoorHingeSide.RIGHT;
                } else {
                    hinge = DoorHingeSide.LEFT;
                }
                world.setBlockAndUpdate(upperPos, upperState.setValue(DoorBlock.HINGE, hinge));
            }

            return rotateOnce(world, lowerPos, lowerState, TrapDoorBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateButton(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof ButtonBlock) {
            return rotateDirection(world, pos, state, ButtonBlock.FACING, ROTATE_FACING);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateTripWireHook(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof TripWireHookBlock) {
            return rotateDirection(world, pos, state, TripWireHookBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotatePiston(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof PistonBaseBlock) {
            boolean extended = state.getValue(PistonBaseBlock.EXTENDED);
            if (extended) return InteractionResult.FAIL;
            return rotateOnce(world, pos, state, DirectionalBlock.FACING, ROTATE_FACING);
        }
        return InteractionResult.PASS;
    }
/*
    private static InteractionResult rotateLever(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof LeverBlock) {
            return rotateAnyTypeAuto(world, pos, state, LeverBlock.FACING, BlockStateProperties.ATTACH_FACE, EnumOrientation::getFacing);
        }
        return InteractionResult.PASS;
    }*/

    private static InteractionResult rotateHopper(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof HopperBlock) {
            return rotateOnce(world, pos, state, HopperBlock.FACING, ROTATE_HOPPER);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateShulkerBox(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof ShulkerBoxBlock) {
            return rotateOnce(world, pos, state, ShulkerBoxBlock.FACING, ROTATE_FACING);
        }
        return InteractionResult.PASS;
    }

    private static ICustomRotationHandler getHandlerFreely(Class<? extends Block> blockClass) {
        return (world, pos, state, sideWrenched) -> rotateFreely(world, pos, state, sideWrenched, blockClass);
    }

    private static InteractionResult rotateFreely(Level world, BlockPos pos, BlockState state, Direction sideWrenched, Class<? extends Block> blockClass) {
        if (blockClass.isInstance(state.getBlock())) {
            return rotateOnce(world, pos, state, DirectionalBlock.FACING, ROTATE_FACING);
        }
        return InteractionResult.PASS;
    }

    private static ICustomRotationHandler getHandlerHorizontalFreely(Class<? extends Block> blockClass) {
        return (world, pos, state, sideWrenched) -> rotateHorizontalFreely(world, pos, state, sideWrenched, blockClass);
    }

    private static InteractionResult rotateHorizontalFreely(Level world, BlockPos pos, BlockState state, Direction sideWrenched, Class<? extends Block> blockClass) {
        if (blockClass.isInstance(state.getBlock())) {
            return rotateOnce(world, pos, state, BlockStateProperties.HORIZONTAL_FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateCocoa(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof CocoaBlock) {
            return rotateAnyTypeManual(world, pos, state, CocoaBlock.FACING, ROTATE_HORIZONTAL, toTry -> ((CocoaBlock) state.getBlock()).canSurvive(state.setValue(CocoaBlock.FACING, toTry),world ,pos));
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateLadder(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof LadderBlock) {
            Predicate<Direction> tester = toTry -> {
                BlockPos offsetPos = pos.offset(toTry.getOpposite().getNormal());
                BlockState offsetState = world.getBlockState(offsetPos);
                return /*!offsetState &&*/ offsetState.isFaceSturdy(world, offsetPos, toTry)/*&& !BlockBCBase_Neptune.isExceptBlockForAttachWithPiston(offsetState.getBlock())*/;
            };
            return rotateAnyTypeManual(world, pos, state, LadderBlock.FACING, ROTATE_HORIZONTAL, tester);
        }
        return InteractionResult.PASS;
    }

 /*   private static InteractionResult rotateTorch(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof TorchBlock) {
            Predicate<Direction> tester = toTry -> {
                BlockPos offsetPos = pos.offset(toTry.getOpposite().getNormal());
                BlockState offsetState = world.getBlockState(offsetPos);

                if (toTry == Direction.UP && state.canSurvive(world, pos)) {
                    return true;
                } else if (toTry != Direction.UP && toTry != Direction.DOWN) {
                    return offsetState.isFaceSturdy(world, offsetPos, toTry)/*&& !BlockBCBase_Neptune.isExceptBlockForAttachWithPiston(offsetState.getBlock())*/;
/*                }
                return false;
            };
            return rotateAnyTypeManual(world, pos, state, TorchBlock., ROTATE_TORCH, tester);
        }
        return InteractionResult.PASS;
    }
    */

    private static InteractionResult rotateChest(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof ChestBlock) {
            BlockPos otherPos = null;
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                BlockPos candidate = pos.offset(facing.getNormal());
                if (world.getBlockState(candidate).getBlock() == state.getBlock()) {
                    otherPos = candidate;
                    break;
                }
            }

            if (otherPos != null) {
                BlockState otherState = world.getBlockState(otherPos);
                Direction facing = state.getValue(ChestBlock.FACING);
                if (otherState.getValue(ChestBlock.FACING) == facing) {
                    world.setBlockAndUpdate(pos, state.setValue(ChestBlock.FACING, facing.getOpposite()));
                    world.setBlockAndUpdate(otherPos, otherState.setValue(ChestBlock.FACING, facing.getOpposite()));
                    return InteractionResult.SUCCESS;
                }
            }

            return rotateOnce(world, pos, state, ChestBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateTrapDoor(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof TrapDoorBlock) {

            if (state.getValue(TrapDoorBlock.FACING) == ROTATE_HORIZONTAL.get(0)) {
                Half half = state.getValue(TrapDoorBlock.HALF);
                if (half == Half.TOP) 
                    half = Half.BOTTOM;
                state = state.setValue(BlockStateProperties.HALF, half);
            }

            return rotateOnce(world, pos, state, TrapDoorBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateStairs(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof StairBlock) {

            if (state.getValue(StairBlock.FACING) == ROTATE_HORIZONTAL.get(0)) {
            	Half half = state.getValue(StairBlock.HALF);
                if (half == Half.TOP)
                    half = Half.BOTTOM;
                state = state.setValue(BlockStateProperties.HALF, half);
            }

            return rotateOnce(world, pos, state, StairBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateSkull(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof SkullBlock) {

                BlockEntity tile = world.getBlockEntity(pos);
                if (tile instanceof SkullBlockEntity) {

                    int rot = state.getValue(SkullBlock.ROTATION);
                    rot = (rot + 1) % 16;

                    state.setValue(SkullBlock.ROTATION, rot);
                    world.setBlockAndUpdate(pos, state);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            

//            return rotateOnce(world, pos, state, SkullBlock.ROTATION, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateHangingBanner(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof WallBannerBlock) {
            return rotateAnyTypeManual(world, pos, state, WallBannerBlock.FACING, ROTATE_HORIZONTAL, toTry -> world.getBlockState(pos.offset(toTry.getOpposite().getNormal())).getMaterial().isSolid());
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateWallSign(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof WallSignBlock) {
            return rotateAnyTypeManual(world, pos, state, WallSignBlock.FACING, ROTATE_HORIZONTAL, toTry -> world.getBlockState(pos.offset(toTry.getOpposite().getNormal())).getMaterial().isSolid());
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateStandingBanner(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof BannerBlock) {
            world.setBlockAndUpdate(pos, state.setValue(BannerBlock.ROTATION, (state.getValue(BannerBlock.ROTATION) + 1) % 16));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateStandingSign(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof StandingSignBlock) {
            world.setBlockAndUpdate(pos, state.setValue(StandingSignBlock.ROTATION, (state.getValue(StandingSignBlock.ROTATION) + 1) % 16));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult rotateDirection(Level world, BlockPos pos, BlockState state, Property<Direction> prop, OrderedEnumMap<Direction> possible) {
        return rotateAnyTypeAuto(world, pos, state, prop, possible, f -> f);
    }

    public static <E extends Enum<E> & Comparable<E>> InteractionResult rotateOnce
        //@formatter:off
    (
        Level world,
        BlockPos pos,
        BlockState state,
        Property<E> prop,
        OrderedEnumMap<E> possible
    )
    //@formatter:on
    {
        E current = state.getValue(prop);
        current = possible.next(current);
        world.setBlockAndUpdate(pos, state.setValue(prop, current));
        return InteractionResult.SUCCESS;
    }

    public static <E extends Enum<E> & Comparable<E>> InteractionResult rotateAnyTypeAuto
        //@formatter:off
    (
        Level world,
        BlockPos pos,
        BlockState state,
        Property<E> prop,
        OrderedEnumMap<E> possible,
        Function<E, Direction> mapper
    )
    //@formatter:on
    {
        Predicate<E> tester = toTry -> Block.canSupportCenter(world, pos, mapper.apply(toTry));
        return rotateAnyTypeManual(world, pos, state, prop, possible, tester);
    }

    public static <E extends Enum<E> & Comparable<E>> InteractionResult rotateAnyTypeManual
        //@formatter:off
    (
        Level world,
        BlockPos pos,
        BlockState state,
        Property<E> prop,
        OrderedEnumMap<E> possible,
        Predicate<E> canPlace
    )
    //@formatter:on
    {
        E current = state.getValue(prop);
        for (int i = possible.getOrderLength(); i > 1; i--) {
            current = possible.next(current);
            if (canPlace.test(current)) {
                world.setBlockAndUpdate(pos, state.setValue(prop, current));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }
}
