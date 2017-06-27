/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import javax.vecmath.Point2d;
import javax.vecmath.Point2i;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings("WeakerAccess")
public class Filling {
    private static List<Item> itemBlocks = new ArrayList<>();
    private static final List<Point2d> TRIANGLE_POINTS = Arrays.asList(
        new Point2d(1, 0.5),
        new Point2d(0.25, 0.9330127018922194),
        new Point2d(0.25, 0.06698729810778076)
    );
    private static final List<Point2d> PENTAGON_POINTS = Arrays.asList(
        new Point2d(1, 0.5),
        new Point2d(0.6545084971874737, 0.9755282581475768),
        new Point2d(0.09549150281252633, 0.7938926261462367),
        new Point2d(0.09549150281252627, 0.2061073738537635),
        new Point2d(0.6545084971874736, 0.02447174185242318)
    );
    private static final List<Point2d> HEXAGON_POINTS = Arrays.asList(
        new Point2d(1, 0.5),
        new Point2d(0.75, 0.9330127018922193),
        new Point2d(0.25, 0.9330127018922194),
        new Point2d(0, 0.5),
        new Point2d(0.25, 0.06698729810778076),
        new Point2d(0.75, 0.06698729810778048)
    );
    private static final List<Point2d> OCTAGON_POINTS = Arrays.asList(
        new Point2d(1, 0.5),
        new Point2d(0.8535533905932737, 0.8535533905932737),
        new Point2d(0.5, 1),
        new Point2d(0.14644660940672627, 0.8535533905932737),
        new Point2d(0, 0.5),
        new Point2d(0.14644660940672616, 0.14644660940672627),
        new Point2d(0.5, 0),
        new Point2d(0.8535533905932737, 0.14644660940672616)
    );

    static {
        StreamSupport.stream(Item.REGISTRY.spliterator(), false)
            .filter(item -> item instanceof ItemBlock || item instanceof ItemBlockSpecial)
            .forEach(Filling::addItemBlock);
        addItemBlock(
            Items.BED,
            Items.OAK_DOOR,
            Items.SPRUCE_DOOR,
            Items.BIRCH_DOOR,
            Items.JUNGLE_DOOR,
            Items.ACACIA_DOOR,
            Items.DARK_OAK_DOOR,
            Items.IRON_DOOR,
            Items.SKULL,
            Items.SIGN
        );
    }

    public static void addItemBlock(Item... items) {
        itemBlocks.addAll(Arrays.asList(items));
    }

    public static List<Item> getItemBlocks() {
        return new ArrayList<>(itemBlocks);
    }

    public static Class<? extends IParameter> getNextParameterClass(List<IParameter> parameters) {
        if (parameters.size() == 0) {
            return EnumParameterPattern.class;
        }
        EnumParameterPattern parameterPattern = (EnumParameterPattern) parameters.get(0);
        if (parameterPattern == EnumParameterPattern.TRIANGLE) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
            }
            if (parameters.size() == 2) {
                return EnumParameterAxis.class;
            }
        }
        if (parameterPattern == EnumParameterPattern.SQUARE) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
            }
            if (parameters.size() == 2) {
                EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
                if (parameterType == EnumParameterType.EMPTY) {
                    return EnumParameterAxis.class;
                }
            }
        }
        if (parameterPattern == EnumParameterPattern.PENTAGON) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
            }
            if (parameters.size() == 2) {
                return EnumParameterAxis.class;
            }
        }
        if (parameterPattern == EnumParameterPattern.HEXAGON) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
            }
            if (parameters.size() == 2) {
                return EnumParameterAxis.class;
            }
        }
        if (parameterPattern == EnumParameterPattern.OCTAGON) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
            }
            if (parameters.size() == 2) {
                return EnumParameterAxis.class;
            }
        }
        if (parameterPattern == EnumParameterPattern.CIRCLE) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
            }
            if (parameters.size() == 2) {
                return EnumParameterAxis.class;
            }
        }
        if (parameterPattern == EnumParameterPattern.FRAME) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
            }
        }
        if (parameterPattern == EnumParameterPattern.SPHERE) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
            }
        }
        return null;
    }

    public static boolean[][][] generateFillingPlanByFunction(BlockPos size,
                                                              Function<BlockPos, Boolean> function) {
        boolean[][][] fillingPlan = new boolean[size.getX()][size.getY()][size.getZ()];
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    fillingPlan[x][y][z] = function.apply(new BlockPos(x, y, z));
                }
            }
        }
        return fillingPlan;
    }

    public static boolean[][][] generateFillingPlanByFunctionInAxis(BlockPos size,
                                                                    EnumFacing.Axis axis,
                                                                    BiFunction<Point2i, Point2i, Boolean> function) {
        switch (axis) {
            case X:
                return generateFillingPlanByFunction(size, pos -> function.apply(
                    new Point2i(pos.getY(), pos.getZ()),
                    new Point2i(size.getY(), size.getZ())
                ));
            case Y:
                return generateFillingPlanByFunction(size, pos -> function.apply(
                    new Point2i(pos.getX(), pos.getZ()),
                    new Point2i(size.getX(), size.getZ())
                ));
            case Z:
                return generateFillingPlanByFunction(size, pos -> function.apply(
                    new Point2i(pos.getX(), pos.getY()),
                    new Point2i(size.getX(), size.getY())
                ));
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static boolean[][][] generateFillingPlanByFunctionInAxis(BlockPos size,
                                                                    EnumFacing.Axis axis,
                                                                    BiConsumer<Point2i, boolean[][]> function) {
        Point2i flatSize;
        switch (axis) {
            case X:
                flatSize = new Point2i(size.getY(), size.getZ());
                break;
            case Y:
                flatSize = new Point2i(size.getX(), size.getZ());
                break;
            case Z:
                flatSize = new Point2i(size.getX(), size.getY());
                break;
            default:
                throw new UnsupportedOperationException();
        }
        boolean[][] flatFillingPlan = new boolean[flatSize.x][flatSize.y];
        function.accept(flatSize, flatFillingPlan);
        switch (axis) {
            case X:
                return generateFillingPlanByFunction(size, pos -> flatFillingPlan[pos.getY()][pos.getZ()]);
            case Y:
                return generateFillingPlanByFunction(size, pos -> flatFillingPlan[pos.getX()][pos.getZ()]);
            case Z:
                return generateFillingPlanByFunction(size, pos -> flatFillingPlan[pos.getX()][pos.getY()]);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static boolean[][][] getFillingPlan(BlockPos size, List<IParameter> parameters) {
        EnumParameterPattern parameterPattern = (EnumParameterPattern) parameters.get(0);
        if (parameterPattern == EnumParameterPattern.TRIANGLE) {
            EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
            EnumParameterAxis parameterAxis = (EnumParameterAxis) parameters.get(2);
            return FillingPolygon.get(size, parameterType, parameterAxis, TRIANGLE_POINTS);
        }
        if (parameterPattern == EnumParameterPattern.SQUARE) {
            EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
            EnumParameterAxis parameterAxis = parameterType == EnumParameterType.EMPTY
                ? (EnumParameterAxis) parameters.get(2)
                : null;
            return FillingSquare.get(size, parameterType, parameterAxis);
        }
        if (parameterPattern == EnumParameterPattern.PENTAGON) {
            EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
            EnumParameterAxis parameterAxis = (EnumParameterAxis) parameters.get(2);
            return FillingPolygon.get(size, parameterType, parameterAxis, PENTAGON_POINTS);
        }
        if (parameterPattern == EnumParameterPattern.HEXAGON) {
            EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
            EnumParameterAxis parameterAxis = (EnumParameterAxis) parameters.get(2);
            return FillingPolygon.get(size, parameterType, parameterAxis, HEXAGON_POINTS);
        }
        if (parameterPattern == EnumParameterPattern.OCTAGON) {
            EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
            EnumParameterAxis parameterAxis = (EnumParameterAxis) parameters.get(2);
            return FillingPolygon.get(size, parameterType, parameterAxis, OCTAGON_POINTS);
        }
        if (parameterPattern == EnumParameterPattern.CIRCLE) {
            EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
            EnumParameterAxis parameterAxis = (EnumParameterAxis) parameters.get(2);
            return FillingCircle.getCircle(size, parameterType, parameterAxis);
        }
        if (parameterPattern == EnumParameterPattern.FRAME) {
            EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
            return FillingFrame.get(size, parameterType);
        }
        if (parameterPattern == EnumParameterPattern.SPHERE) {
            EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
            return FillingCircle.getSphere(size, parameterType);
        }
        return generateFillingPlanByFunction(size, pos -> false);
    }

    public static boolean[][][] invertFillingPlan(BlockPos size, boolean[][][] fillingPlan) {
        return generateFillingPlanByFunction(size, pos -> !fillingPlan[pos.getX()][pos.getY()][pos.getZ()]);
    }
}
