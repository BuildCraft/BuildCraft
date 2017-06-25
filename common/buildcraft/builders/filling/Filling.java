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

import javax.vecmath.Point2i;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import buildcraft.lib.misc.DrawingUtil;

public enum Filling {
    INSTANCE;

    private List<Item> itemBlocks = new ArrayList<>();

    Filling() {
        StreamSupport.stream(Item.REGISTRY.spliterator(), false)
            .filter(item -> item instanceof ItemBlock || item instanceof ItemBlockSpecial)
            .forEach(this::addItemBlock);
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

    public void addItemBlock(Item... items) {
        itemBlocks.addAll(Arrays.asList(items));
    }

    public List<Item> getItemBlocks() {
        return new ArrayList<>(itemBlocks);
    }

    public Class<? extends IParameter> getNextParameterClass(List<IParameter> parameters) {
        if (parameters.size() == 0) {
            return EnumParameterPattern.class;
        }
        EnumParameterPattern parameterPattern = (EnumParameterPattern) parameters.get(0);
        if (parameterPattern == EnumParameterPattern.FRAME) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
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
        if (parameterPattern == EnumParameterPattern.SPHERE) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
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
        return null;
    }

    public boolean[][][] generateFillingPlanByFunction(BlockPos size,
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

    public boolean[][][] generateFillingPlanByFunctionInAxis(BlockPos size,
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

    public boolean[][][] generateFillingPlanByFunctionInAxis(BlockPos size,
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

    public boolean[][][] getFillingPlan(BlockPos size, List<IParameter> parameters) {
        EnumParameterPattern parameterPattern = (EnumParameterPattern) parameters.get(0);
        BlockPos sizeS = size.add(-1, -1, -1);
        if (parameterPattern == EnumParameterPattern.FRAME) {
            EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
            return generateFillingPlanByFunction(size, pos ->
                parameterType == EnumParameterType.FILLED ?
                    pos.getX() == 0 || pos.getX() == sizeS.getX() ||
                        pos.getY() == 0 || pos.getY() == sizeS.getY() ||
                        pos.getZ() == 0 || pos.getZ() == sizeS.getZ() :
                    ((pos.getX() == 0 || pos.getX() == sizeS.getX()) && (pos.getY() == 0 || pos.getY() == sizeS.getY())) ||
                        ((pos.getY() == 0 || pos.getY() == sizeS.getY()) && (pos.getZ() == 0 || pos.getZ() == sizeS.getZ())) ||
                        ((pos.getZ() == 0 || pos.getZ() == sizeS.getZ()) && (pos.getX() == 0 || pos.getX() == sizeS.getX()))
            );
        }
        if (parameterPattern == EnumParameterPattern.SQUARE) {
            EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
            if (parameterType == EnumParameterType.FILLED) {
                return generateFillingPlanByFunction(size, pos -> true);
            } else {
                EnumParameterAxis parameterAxis = (EnumParameterAxis) parameters.get(2);
                return generateFillingPlanByFunctionInAxis(size, parameterAxis.axis, (pos, flatSize) ->
                    pos.getX() == 0 || pos.getX() == flatSize.getX() - 1 ||
                        pos.getY() == 0 || pos.getY() == flatSize.getY() - 1
                );
            }
        }
        if (parameterPattern == EnumParameterPattern.CIRCLE) {
            EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
            EnumParameterAxis parameterAxis = (EnumParameterAxis) parameters.get(2);
            return generateFillingPlanByFunctionInAxis(size, parameterAxis.axis, (flatSize, flatFillingPlan) -> {
                DrawingUtil.drawEllipse(
                    flatSize.x % 2 == 0 ? flatSize.x / 2 - 1 : flatSize.x / 2,
                    flatSize.y % 2 == 0 ? flatSize.y / 2 - 1 : flatSize.y / 2,
                    flatSize.x % 2 == 0 ? flatSize.x / 2 - 1 : flatSize.x / 2,
                    flatSize.y % 2 == 0 ? flatSize.y / 2 - 1 : flatSize.y / 2,
                    parameterType == EnumParameterType.FILLED,
                    (x, y) -> {
                        List<Point2i> positions = new ArrayList<>();
                        positions.add(
                            new Point2i(
                                flatSize.x % 2 == 0 && x > flatSize.x / 2 ? x + 1 : x,
                                flatSize.y % 2 == 0 && y > flatSize.y / 2 ? y + 1 : y
                            )
                        );
                        if (flatSize.x % 2 == 0 && x == flatSize.x / 2) {
                            positions.add(
                                new Point2i(
                                    x + 1,
                                    flatSize.y % 2 == 0 && y > flatSize.y / 2 ? y + 1 : y
                                )
                            );
                        }
                        if (flatSize.y % 2 == 0 && y == flatSize.y / 2) {
                            positions.add(
                                new Point2i(
                                    flatSize.x % 2 == 0 && x > flatSize.x / 2 ? x + 1 : x,
                                    y + 1
                                )
                            );
                        }
                        for (Point2i p : positions) {
                            if (p.x >= 0 && p.y >= 0 && p.x < flatSize.x && p.y < flatSize.y) {
                                flatFillingPlan[p.x][p.y] = true;
                            }
                        }
                    }
                );
            });
        }
        return generateFillingPlanByFunction(size, pos -> false);
    }

    public boolean[][][] invertFillingPlan(BlockPos size, boolean[][][] fillingPlan) {
        return generateFillingPlanByFunction(size, pos -> !fillingPlan[pos.getX()][pos.getY()][pos.getZ()]);
    }
}
