/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filling;

import buildcraft.lib.misc.DrawingUtil;
import buildcraft.lib.misc.MathUtil;
import net.minecraft.util.math.BlockPos;

import javax.vecmath.Point2d;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class FillingPolygon {
    public static boolean[][][] get(BlockPos size,
                                    EnumParameterType parameterType,
                                    EnumParameterAxis parameterAxis,
                                    List<Point2d> points) {
        return Filling.generateFillingPlanByFunctionInAxis(size, parameterAxis.axis, (flatSize, flatFillingPlan) -> {
            for (int i = 0; i < points.size(); i++) {
                Point2d point1 = points.get(i % points.size());
                Point2d point2 = points.get((i + 1) % points.size());
                DrawingUtil.drawLine(
                    (int) Math.round(point1.x * (flatSize.x - 1)),
                    (int) Math.round(point1.y * (flatSize.y - 1)),
                    (int) Math.round(point2.x * (flatSize.x - 1)),
                    (int) Math.round(point2.y * (flatSize.y - 1)),
                    (x, y) ->
                        flatFillingPlan
                            [MathUtil.clamp(x, 0, flatSize.x - 1)]
                            [MathUtil.clamp(y, 0, flatSize.y - 1)]
                            = true
                );
            }
            if (parameterType == EnumParameterType.FILLED) {
                DrawingUtil.fill(
                    flatFillingPlan,
                    flatSize.x / 2,
                    flatSize.y / 2,
                    flatSize.x,
                    flatSize.y
                );
            }
        });
    }
}
