/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filling;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import buildcraft.lib.misc.DrawingUtil;
import buildcraft.lib.misc.MathUtil;

public class FillingStairs {
    public static boolean[][][] get(BlockPos size,
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
            DrawingUtil.drawLine(
                0,
                0,
                flatSize.x - 1,
                flatSize.y - 1,
                (x, y) ->
                    flatFillingPlan
                        [MathUtil.clamp(x, 0, flatSize.x - 1)]
                        [MathUtil.clamp(y, 0, flatSize.y - 1)]
                        = true
            );
            DrawingUtil.fill(
                flatFillingPlan,
                flatSize.x - 1,
                0,
                flatSize.x,
                flatSize.y
            );
        });
    }
}
