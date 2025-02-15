/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.pos;

import buildcraft.lib.expression.api.IConstantNode;

import java.util.function.DoubleSupplier;

/** Defines a single point somewhere on the screen. */
public interface IGuiPosition {
    double getX();

    double getY();

    default IGuiPosition offset(DoubleSupplier x, DoubleSupplier y) {
        if (x instanceof IConstantNode && y instanceof IConstantNode) {
            return offset(x.getAsDouble(), y.getAsDouble());
        }
        return offset(new PositionCallable(x, y));
    }

    default IGuiPosition offset(double x, DoubleSupplier y) {
        if (y instanceof IConstantNode) {
            return offset(x, y.getAsDouble());
        }
        return offset(new PositionCallable(x, y));
    }

    default IGuiPosition offset(DoubleSupplier x, double y) {
        if (x instanceof IConstantNode) {
            return offset(x.getAsDouble(), y);
        }
        return offset(new PositionCallable(x, y));
    }

    default IGuiPosition offset(double x, double y) {
        return PositionOffset.createOffset(this, x, y);
    }

    default IGuiPosition offset(IGuiPosition by) {
        return new PositionAdded(this, by);
    }

    static IGuiPosition create(DoubleSupplier x, DoubleSupplier y) {
        if (x instanceof IConstantNode && y instanceof IConstantNode) {
            return new PositionAbsolute(x.getAsDouble(), y.getAsDouble());
        }
        return new PositionCallable(x, y);
    }
}
