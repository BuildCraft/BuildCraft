/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filling;

import java.util.function.BiConsumer;

import net.minecraft.util.math.BlockPos;

import buildcraft.lib.misc.DrawingUtil;
import buildcraft.lib.misc.MathUtil;

public class FillingTriangle {
    public static boolean[][][] get(BlockPos size,
                                    EnumParameterType parameterType,
                                    EnumParameterFacing parameterFacing) {
        return Filling.generateFillingPlanByFunctionInFacing(size, parameterFacing.facing, (flatSize, flatFillingPlan) -> {
            BiConsumer<Integer, Integer> drawPixel = (x, y) ->
                flatFillingPlan
                    [MathUtil.clamp(x, 0, flatSize.x - 1)]
                    [MathUtil.clamp(y, 0, flatSize.y - 1)]
                    = true;
            DrawingUtil.drawLine(
                0,
                0,
                flatSize.x - 1,
                0,
                drawPixel
            );
            DrawingUtil.drawLine(
                0,
                0,
                flatSize.x / 2,
                flatSize.y - 1,
                drawPixel
            );
            DrawingUtil.drawLine(
                flatSize.x - 1,
                0,
                flatSize.x % 2 == 0 ? flatSize.x / 2 : flatSize.x / 2 + 1,
                flatSize.y - 1,
                drawPixel
            );
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
