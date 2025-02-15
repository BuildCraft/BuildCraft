/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.lib.misc.collect.OrderedEnumMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.function.Function;
import java.util.function.Predicate;

public class VanillaRotationHandlers {
    /* Player friendly rotations- these only rotate through sides that are touching (only 90 degree changes, in any
     * axis), rather than jumping around. */
    public static final OrderedEnumMap<Direction> ROTATE_HORIZONTAL, ROTATE_FACING, ROTATE_TORCH, ROTATE_HOPPER;
    // Calen: not still used. we handle skull and level in #rotateSkull #rotateLever
//    public static final OrderedEnumMap<VoxelShape> ROTATE_LEVER;

    static {
        Direction e = Direction.EAST, w = Direction.WEST;
        Direction u = Direction.UP, d = Direction.DOWN;
        Direction n = Direction.NORTH, s = Direction.SOUTH;
        ROTATE_HORIZONTAL = new OrderedEnumMap<>(Direction.class, e, s, w, n);
        ROTATE_FACING = new OrderedEnumMap<>(Direction.class, e, s, d, w, n, u);
        ROTATE_TORCH = new OrderedEnumMap<>(Direction.class, e, s, w, n, u);
        ROTATE_HOPPER = new OrderedEnumMap<>(Direction.class, e, s, w, n, d);

//        EnumOrientation[] leverFaces = new EnumOrientation[8];
//        int index = 0;
//        for (EnumFacing face : ROTATE_FACING.getOrder()) {
//            if (face == EnumFacing.DOWN) {
//                leverFaces[index++] = EnumOrientation.DOWN_Z;
//                leverFaces[index++] = EnumOrientation.DOWN_X;
//            } else if (face == EnumFacing.UP) {
//                leverFaces[index++] = EnumOrientation.UP_Z;
//                leverFaces[index++] = EnumOrientation.UP_X;
//            } else {
//                leverFaces[index++] = EnumOrientation.forFacings(face, null);
//            }
//        }
//        ROTATE_LEVER = new OrderedEnumMap<>(EnumOrientation.class, leverFaces);
    }

    public static void fmlInit() {
        CustomRotationHelper.INSTANCE.registerHandlerForAll(ButtonBlock.class, VanillaRotationHandlers::rotateButton);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(TripWireHookBlock.class, VanillaRotationHandlers::rotateTripWireHook);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(DoorBlock.class, VanillaRotationHandlers::rotateDoor);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(PistonBaseBlock.class, VanillaRotationHandlers::rotatePiston);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(LeverBlock.class, VanillaRotationHandlers::rotateLever);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(ShulkerBoxBlock.class, VanillaRotationHandlers::rotateShulkerBox);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(DispenserBlock.class, getHandlerFreely(DispenserBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(ObserverBlock.class, getHandlerFreely(ObserverBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(EndRodBlock.class, getHandlerFreely(EndRodBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(FenceGateBlock.class, getHandlerHorizontalFreely(FenceGateBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(DiodeBlock.class, getHandlerHorizontalFreely(DiodeBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(PumpkinBlock.class, getHandlerHorizontalFreely(PumpkinBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(GlazedTerracottaBlock.class, getHandlerHorizontalFreely(GlazedTerracottaBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(AnvilBlock.class, getHandlerHorizontalFreely(AnvilBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(EnderChestBlock.class, getHandlerHorizontalFreely(EnderChestBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(FurnaceBlock.class, getHandlerHorizontalFreely(FurnaceBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(CocoaBlock.class, VanillaRotationHandlers::rotateCocoa);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(TorchBlock.class, VanillaRotationHandlers::rotateTorch);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(LadderBlock.class, VanillaRotationHandlers::rotateLadder);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(HopperBlock.class, VanillaRotationHandlers::rotateHopper);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(ChestBlock.class, VanillaRotationHandlers::rotateChest);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(TrapDoorBlock.class, VanillaRotationHandlers::rotateTrapDoor);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(StairBlock.class, VanillaRotationHandlers::rotateStairs);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(SkullBlock.class, VanillaRotationHandlers::rotateSkull);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(WallBannerBlock.class, VanillaRotationHandlers::rotateHangingBanner);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(WallSignBlock.class, VanillaRotationHandlers::rotateWallSign);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BannerBlock.class, VanillaRotationHandlers::rotateStandingBanner);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(StandingSignBlock.class, VanillaRotationHandlers::rotateStandingSign);
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
                world.setBlock(upperPos, upperState.setValue(DoorBlock.HINGE, hinge), Block.UPDATE_ALL);
            }

            return rotateOnce(world, lowerPos, lowerState, TrapDoorBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateButton(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof ButtonBlock) {
            return rotateEnumFacing(world, pos, state, ButtonBlock.FACING, ROTATE_FACING);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateTripWireHook(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof TripWireHookBlock) {
            return rotateEnumFacing(world, pos, state, TripWireHookBlock.FACING, ROTATE_HORIZONTAL);
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

    private static InteractionResult rotateLever(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof LeverBlock) {
//            return rotateAnyTypeAuto(world, pos, state, BlockLever.FACING, ROTATE_LEVER, EnumOrientation::getFacing);
            Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
            BlockState newState = state.setValue(HorizontalDirectionalBlock.FACING, direction.getClockWise());
            if (newState.canSurvive(world, pos)) {
                world.setBlock(pos, newState, Block.UPDATE_ALL);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.PASS;
    }

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
            return rotateOnce(world, pos, state, HorizontalDirectionalBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateCocoa(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof CocoaBlock) {
            return rotateAnyTypeManual(world, pos, state, CocoaBlock.FACING, ROTATE_HORIZONTAL, toTry -> ((CocoaBlock) state.getBlock()).canSurvive(state.setValue(CocoaBlock.FACING, toTry), world, pos));
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateLadder(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof LadderBlock) {
            Predicate<Direction> tester = toTry ->
            {
                BlockPos offsetPos = pos.relative(toTry.getOpposite());
                BlockState offsetState = world.getBlockState(offsetPos);
                return !offsetState.getBlock().isSignalSource(offsetState) &&
//                        offsetState.getBlockFaceShape(world, offsetPos, toTry) == BlockFaceShape.SOLID &&
                        state.setValue(LadderBlock.FACING, toTry).canSurvive(world, pos) &&
                        !BlockBCBase_Neptune.isExceptBlockForAttachWithPiston(offsetState.getBlock());
            };
            return rotateAnyTypeManual(world, pos, state, LadderBlock.FACING, ROTATE_HORIZONTAL, tester);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateTorch(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        Block b = state.getBlock();
        if (b instanceof WallTorchBlock || b instanceof RedstoneWallTorchBlock) {
            Predicate<Direction> tester = toTry ->
            {
                BlockPos offsetPos = pos.relative(toTry.getOpposite());
                BlockState offsetState = world.getBlockState(offsetPos);
//
//                if (toTry == EnumFacing.UP && offsetState.getBlock().canPlaceTorchOnTop(state, world, offsetPos)) {
//                    return true;
//                } else if (toTry != EnumFacing.UP && toTry != EnumFacing.DOWN) {
//                    return offsetState.getBlockFaceShape(world, offsetPos, toTry) == BlockFaceShape.SOLID && !BlockBCBase_Neptune.isExceptBlockForAttachWithPiston(offsetState.getBlock());
//                }
//                return false;
                return state.setValue(HorizontalDirectionalBlock.FACING, toTry).canSurvive(world, pos)
                        && !BlockBCBase_Neptune.isExceptBlockForAttachWithPiston(offsetState.getBlock());
            };
            return rotateAnyTypeManual(world, pos, state, HorizontalDirectionalBlock.FACING, ROTATE_TORCH, tester);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateChest(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof ChestBlock) {
            BlockPos otherPos = null;
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                BlockPos candidate = pos.relative(facing);
                if (world.getBlockState(candidate).getBlock() == state.getBlock()) {
                    otherPos = candidate;
                    break;
                }
            }

            if (otherPos != null) {
                BlockState otherState = world.getBlockState(otherPos);
                Direction facing = state.getValue(ChestBlock.FACING);
                if (otherState.getValue(ChestBlock.FACING) == facing) {
                    world.setBlock(pos, state.setValue(ChestBlock.FACING, facing.getOpposite()), Block.UPDATE_ALL);
                    world.setBlock(otherPos, otherState.setValue(ChestBlock.FACING, facing.getOpposite()), Block.UPDATE_ALL);
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
                if (half == Half.TOP) {
                    half = Half.BOTTOM;
                } else {
                    half = Half.TOP;
                }
                state = state.setValue(TrapDoorBlock.HALF, half);
            }

            return rotateOnce(world, pos, state, TrapDoorBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateStairs(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof StairBlock) {

            if (state.getValue(StairBlock.FACING) == ROTATE_HORIZONTAL.get(0)) {
                Half half = state.getValue(StairBlock.HALF);
                if (half == Half.TOP) {
                    half = Half.BOTTOM;
                } else {
                    half = Half.TOP;
                }

                state = state.setValue(StairBlock.HALF, half);
                state = state.rotate(world, pos, Rotation.CLOCKWISE_90);
            }

            return rotateOnce(world, pos, state, StairBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateSkull(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof SkullBlock) {

//            if (state.getValue(SkullBlock.ROTATION).getAxis().isVertical())
            if (true) {
                BlockEntity tile = world.getBlockEntity(pos);
                if (tile instanceof SkullBlockEntity) {
                    // 1.12.2 Old
//                    SkullBlockEntity tileSkull = (SkullBlockEntity) tile;
//
//                    int rot = ObfuscationReflectionHelper.getPrivateValue(SkullBlockEntity.class, tileSkull, "skullRotation", "field_" + "145910_i");
//                    rot = (rot + 1) % 16;
//
//                    tileSkull.setSkullRotation(rot);
//                    tileSkull.markDirty();
//                    world.sendBlockUpdated(pos, state, state, 3);

                    // Calen
                    BlockState s = world.getBlockState(pos);
                    int rot = s.getValue(SkullBlock.ROTATION);
                    rot = (rot + 1) % 16;
                    s.setValue(SkullBlock.ROTATION, rot);
                    world.setBlock(pos, s, Block.UPDATE_ALL);

                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
//            else {
//                return rotateOnce(world, pos, state, SkullBlock.ROTATION, ROTATE_HORIZONTAL);
//            }
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateHangingBanner(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof WallBannerBlock) {
            return rotateAnyTypeManual(world, pos, state, HorizontalDirectionalBlock.FACING, ROTATE_HORIZONTAL, toTry -> world.getBlockState(pos.relative(toTry.getOpposite())).isSolid());
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateWallSign(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof WallSignBlock) {
            return rotateAnyTypeManual(world, pos, state, WallSignBlock.FACING, ROTATE_HORIZONTAL, toTry -> world.getBlockState(pos.relative(toTry.getOpposite())).isSolid());
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateStandingBanner(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof BannerBlock) {
            world.setBlock(pos, state.setValue(BannerBlock.ROTATION, (state.getValue(BannerBlock.ROTATION) + 1) % 16), Block.UPDATE_ALL);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateStandingSign(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof StandingSignBlock) {
            world.setBlock(pos, state.setValue(StandingSignBlock.ROTATION, (state.getValue(StandingSignBlock.ROTATION) + 1) % 16), Block.UPDATE_ALL);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult rotateEnumFacing(Level world, BlockPos pos, BlockState state, Property<Direction> prop, OrderedEnumMap<Direction> possible) {
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
        world.setBlock(pos, state.setValue(prop, current), Block.UPDATE_ALL);
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
//        Predicate<E> tester = toTry -> state.getBlock().canPlaceBlockOnSide(world, pos, mapper.apply(toTry));
        Predicate<E> tester = toTry -> state.setValue(prop, toTry).canSurvive(world, pos);
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
                world.setBlock(pos, state.setValue(prop, current), Block.UPDATE_ALL);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }
}
