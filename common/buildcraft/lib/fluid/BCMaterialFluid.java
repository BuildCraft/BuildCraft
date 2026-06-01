/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.MaterialLiquid;

public class BCMaterialFluid extends MaterialLiquid {
    public BCMaterialFluid(MapColor color, boolean canBurn) {
        super(color);
        if(canBurn) {
            setBurning();
        }
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean blocksMovement() {
        return true;
    }
}
