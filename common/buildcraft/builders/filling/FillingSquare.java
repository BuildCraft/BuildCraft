/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filling;

import net.minecraft.util.math.BlockPos;

@SuppressWarnings("WeakerAccess")
public class FillingSquare {
    public static boolean[][][] get(BlockPos size,
                                    EnumParameterType parameterType,
                                    EnumParameterAxis parameterAxis) {
        if (parameterType == EnumParameterType.FILLED) {
            return Filling.generateFillingPlanByFunction(size, pos -> true);
        } else {
            return Filling.generateFillingPlanByFunctionInAxis(size, parameterAxis.axis, (pos, flatSize) ->
                pos.getX() == 0 || pos.getX() == flatSize.getX() - 1 ||
                    pos.getY() == 0 || pos.getY() == flatSize.getY() - 1
            );
        }
    }
}
