/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filling;

import buildcraft.lib.misc.DrawingUtil;
import buildcraft.lib.misc.MathUtil;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings("WeakerAccess")
public class FillingSphere {
    public static boolean[][][] getSphere(BlockPos size,
                                          EnumParameterType parameterType) {
        boolean[][][] fillingPlan = new boolean[size.getX()][size.getY()][size.getZ()];
        DrawingUtil.drawSphere(
            new BlockPos(
                Math.ceil(size.getX() / 2D + 0.5),
                Math.ceil(size.getY() / 2D + 0.5),
                Math.ceil(size.getZ() / 2D + 0.5)
            ),
            new BlockPos(
                Math.round(size.getX() / 2D - 1),
                Math.round(size.getY() / 2D - 1),
                Math.round(size.getZ() / 2D - 1)
            ),
            parameterType == EnumParameterType.FILLED,
            pos ->
                fillingPlan
                    [MathUtil.clamp(pos.getX(), 0, size.getX() - 1)]
                    [MathUtil.clamp(pos.getY(), 0, size.getY() - 1)]
                    [MathUtil.clamp(pos.getZ(), 0, size.getZ() - 1)]
                    = true
        );
        return fillingPlan;
    }
}
