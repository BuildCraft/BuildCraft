/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filling;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2i;

import net.minecraft.util.math.BlockPos;

import buildcraft.lib.misc.DrawingUtil;
import buildcraft.lib.misc.MathUtil;

@SuppressWarnings("WeakerAccess")
public class FillingCircle {
    private static void draw(EnumParameterType parameterType, Point2i flatSize, boolean[][] flatFillingPlan) {
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
    }

    public static boolean[][][] getCircle(BlockPos size,
                                          EnumParameterType parameterType,
                                          EnumParameterAxis parameterAxis) {
        return Filling.generateFillingPlanByFunctionInAxis(size, parameterAxis.axis, (flatSize, flatFillingPlan) -> {
            draw(parameterType, flatSize, flatFillingPlan);
        });
    }

    public static boolean[][][] getSphere(BlockPos size,
                                          EnumParameterType parameterType) {
        boolean[][][] flatFillingPlans = new boolean[size.getY()][size.getX()][size.getZ()];
        Point2i flatSize = new Point2i(size.getX(), size.getZ());
        int levelsCount = size.getY();
        for (int level = 0; level < levelsCount; level++) {
            double position = (level + 0.5) / levelsCount;
            if (position > 0.5) {
                position = 1 - position;
            }
            Point2i resizedSize = new Point2i(
                MathUtil.clamp(
                    (int) Math.round(Math.sqrt(position * (1 - position)) * 2 * flatSize.x),
                    1,
                    flatSize.x - 1
                ),
                MathUtil.clamp(
                    (int) Math.round(Math.sqrt(position * (1 - position)) * 2 * flatSize.y),
                    1,
                    flatSize.y - 1
                )
            );
            boolean[][] resizedFillingPlan = new boolean[resizedSize.x][resizedSize.y];
            draw(parameterType, resizedSize, resizedFillingPlan);
            for (int y = 0; y < resizedSize.y; y++) {
                for (int x = 0; x < resizedSize.x; x++) {
                    flatFillingPlans
                        [level]
                        [x + (flatSize.x - resizedSize.x) / 2]
                        [y + (flatSize.y - resizedSize.y) / 2]
                        = resizedFillingPlan[x][y];
                }
            }
        }
        return Filling.generateFillingPlanByFunction(size, pos -> flatFillingPlans[pos.getY()][pos.getX()][pos.getZ()]);
    }
}
