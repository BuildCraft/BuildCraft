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

@SuppressWarnings("WeakerAccess")
public class FillingCircle {
    public static boolean[][][] get(BlockPos size,
                                    EnumParameterType parameterType,
                                    EnumParameterAxis parameterAxis) {
        return Filling.generateFillingPlanByFunctionInAxis(size, parameterAxis.axis, (flatSize, flatFillingPlan) -> {
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
}
