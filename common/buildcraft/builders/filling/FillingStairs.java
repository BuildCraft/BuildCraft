/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filling;

import buildcraft.lib.misc.DrawingUtil;
import buildcraft.lib.misc.MathUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public class FillingStairs {
    public static boolean[][][] get(BlockPos size,
                                    EnumParameterType parameterType,
                                    EnumParameterFacing parameterFacing) {
        EnumFacing side;
        switch (parameterFacing.facing) {
            case WEST:
                side = EnumFacing.SOUTH;
                break;
            case EAST:
                side = EnumFacing.NORTH;
                break;
            case NORTH:
                side = EnumFacing.EAST;
                break;
            case SOUTH:
                side = EnumFacing.WEST;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return Filling.generateFillingPlanByFunctionInFacing(size, side, (flatSize, flatFillingPlan) -> {
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
                flatSize.x - 1,
                0,
                flatSize.x - 1,
                flatSize.y - 1,
                drawPixel
            );
            DrawingUtil.drawLine(
                0,
                0,
                flatSize.x - 1,
                flatSize.y - 1,
                drawPixel
            );
            if (parameterType == EnumParameterType.FILLED) {
                DrawingUtil.fill(
                    flatFillingPlan,
                    flatSize.x - 2,
                    1,
                    flatSize.x,
                    flatSize.y
                );
            }
        });
    }
}
