/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filling;

import net.minecraft.util.EnumFacing;

public enum EnumParameterAxis implements IParameter {
    X(EnumFacing.Axis.X),
    Y(EnumFacing.Axis.Y),
    Z(EnumFacing.Axis.Z);

    public final EnumFacing.Axis axis;

    EnumParameterAxis(EnumFacing.Axis axis) {
        this.axis = axis;
    }
}
