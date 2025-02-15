/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.pos;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

import java.util.function.DoubleSupplier;

/** Defines an area somewhere on the screen. */
public interface IGuiArea extends IGuiPosition {
    double getWidth();

    double getHeight();

    default double getCenterX() {
        return getX() + getWidth() / 2;
    }

    default double getCenterY() {
        return getY() + getHeight() / 2;
    }

    default double getEndX() {
        return getX() + getWidth();
    }

    default double getEndY() {
        return getY() + getHeight();
    }

    default boolean contains(double x, double y) {
        double tx = getX();
        double ty = getY();
        double w = getWidth();
        double h = getHeight();
        if (x < tx || x >= tx + w) return false;
        if (y < ty || y >= ty + h) return false;
        return true;
    }

    default boolean contains(IGuiPosition position) {
        return contains(position.getX(), position.getY());
    }

    default boolean contains(IGuiArea element) {
        if (element.getX() < getX() || element.getEndX() >= getEndX()) return false;
        if (element.getY() < getY() || element.getEndY() >= getEndY()) return false;
        return true;
    }

    default String rectangleToString() {
        return "[x = " + getX() + ", y = " + getY() + ", w = " + getWidth() + ", h = " + getHeight() + "]";
    }

    default GuiRectangle asImmutable() {
        return new GuiRectangle(getX(), getY(), getWidth(), getHeight());
    }

    default IGuiPosition getCenter() {
        return getPosition(0, 0);
    }

    default IGuiPosition getEnd() {
        return getPosition(1, 1);
    }

    default IGuiPosition getCenterTop() {
        return getPosition(0, 0);
    }

    /** @param partX -1, 0 or 1. -1 equals the start, 0 equals the centre and 1 equals the end
     * @param partY -1, 0 or 1. -1 equals the start, 0 equals the centre and 1 equals the end
     * @return */
    default IGuiPosition getPosition(int partX, int partY) {
        DoubleSupplier x = partX < 0 ? this::getX : partX > 0 ? this::getEndX : this::getCenterX;
        DoubleSupplier y = partY < 0 ? this::getY : partY > 0 ? this::getEndY : this::getCenterY;
        return new PositionCallable(x, y);
    }

    @Override
    default IGuiArea offset(IGuiPosition by) {
        if (by instanceof PositionAbsolute) {
            if (by.getX() == 0 && by.getY() == 0) {
                return this;
            }
        }
        return offset(by::getX, by::getY);
    }

    @Override
    default IGuiArea offset(double x, DoubleSupplier y) {
        if (y instanceof IConstantNode) {
            return offset(x, y.getAsDouble());
        }
        return offset(NodeConstantDouble.of(x), y);
    }

    @Override
    default IGuiArea offset(DoubleSupplier x, double y) {
        if (x instanceof IConstantNode) {
            return offset(x.getAsDouble(), y);
        }
        return offset(x, NodeConstantDouble.of(y));
    }

    @Override
    default IGuiArea offset(DoubleSupplier x, DoubleSupplier y) {
        return create(() -> getX() + x.getAsDouble(), () -> getY() + y.getAsDouble(), this::getWidth, this::getHeight);
    }

    @Override
    default IGuiArea offset(double x, double y) {
        if (x == 0 && y == 0) return this;
        return create(() -> getX() + x, () -> getY() + y, this::getWidth, this::getHeight);
    }

    default IGuiArea resize(double newWidth, double newHeight) {
        return create(this::getX, this::getY, () -> newWidth, () -> newHeight);
    }

    default IGuiArea resize(DoubleSupplier newWidth, DoubleSupplier newHeight) {
        return create(this::getX, this::getY, newWidth, newHeight);
    }

    default IGuiArea expand(double by) {
        return expand(by, by);
    }

    default IGuiArea expand(double dX, double dY) {
        return create(() -> getX() - dX, () -> getY() - dY, () -> getWidth() + dX * 2, () -> getHeight() + dY * 2);
    }

    default IGuiArea expand(DoubleSupplier by) {
        if (by instanceof IConstantNode) {
            return expand(by.getAsDouble());
        }
        return expand(by, by);
    }

    default IGuiArea expand(DoubleSupplier dX, DoubleSupplier dY) {
        if (dX instanceof IConstantNode && dY instanceof IConstantNode) {
            return expand(dX.getAsDouble(), dY.getAsDouble());
        }
        return create(//
                () -> getX() - dX.getAsDouble(), //
                () -> getY() - dY.getAsDouble(), //
                () -> getWidth() + dX.getAsDouble() * 2, //
                () -> getHeight() + dY.getAsDouble() * 2//
        );
    }

    default IGuiArea offsetToOrigin() {
        return create(() -> 0, () -> 0, this::getWidth, this::getHeight);
    }

    static IGuiArea create(DoubleSupplier width, DoubleSupplier height) {
        if (width instanceof IConstantNode && height instanceof IConstantNode) {
            return new GuiRectangle(width.getAsDouble(), height.getAsDouble());
        }
        return new AreaCallable(width, height);
    }

    static IGuiArea create(DoubleSupplier x, DoubleSupplier y, DoubleSupplier width, DoubleSupplier height) {
        if (x instanceof IConstantNode && y instanceof IConstantNode && width instanceof IConstantNode
                && height instanceof IConstantNode)
        {
            return new GuiRectangle(x.getAsDouble(), y.getAsDouble(), width.getAsDouble(), height.getAsDouble());
        }
        return new AreaCallable(x, y, width, height);
    }

    static IGuiArea create(IGuiPosition pos, double width, double height) {
        if (pos instanceof PositionAbsolute) {
            return new GuiRectangle(pos.getX(), pos.getY(), width, height);
        }
        return new AreaCallable(pos::getX, pos::getY, () -> width, () -> height);
    }
}
