/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filling;

import net.minecraft.util.math.BlockPos;

@SuppressWarnings("WeakerAccess")
public class FillingFrame {
    public static boolean[][][] get(BlockPos size,
                                    EnumParameterType parameterType) {
        BlockPos sizeS = size.add(-1, -1, -1);
        return Filling.generateFillingPlanByFunction(size, pos ->
            parameterType == EnumParameterType.FILLED ?
                pos.getX() == 0 || pos.getX() == sizeS.getX() ||
                    pos.getY() == 0 || pos.getY() == sizeS.getY() ||
                    pos.getZ() == 0 || pos.getZ() == sizeS.getZ() :
                ((pos.getX() == 0 || pos.getX() == sizeS.getX()) && (pos.getY() == 0 || pos.getY() == sizeS.getY())) ||
                    ((pos.getY() == 0 || pos.getY() == sizeS.getY()) && (pos.getZ() == 0 || pos.getZ() == sizeS.getZ())) ||
                    ((pos.getZ() == 0 || pos.getZ() == sizeS.getZ()) && (pos.getX() == 0 || pos.getX() == sizeS.getX()))
        );
    }
}
