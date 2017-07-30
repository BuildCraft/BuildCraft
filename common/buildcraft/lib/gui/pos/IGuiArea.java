/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.pos;

import java.util.function.IntSupplier;

import buildcraft.lib.expression.api.IConstantNode;

/** Defines an area somewhere on the screen. */
public interface IGuiArea extends IGuiPosition {
    int getWidth();

    int getHeight();

    default int getCenterX() {
        return getX() + getWidth() / 2;
    }

    default int getCenterY() {
        return getY() + getHeight() / 2;
    }

    default int getEndX() {
        return getX() + getWidth();
    }

    default int getEndY() {
        return getY() + getHeight();
    }

    default boolean contains(int x, int y) {
        int tx = getX();
        int ty = getY();
        int w = getWidth();
        int h = getHeight();
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
        return new PositionCallable(this::getCenterX, this::getCenterY);
    }

    default IGuiPosition getEnd() {
        return new PositionCallable(this::getEndX, this::getEndY);
    }

    /** @param partX -1, 0 or 1. -1 equals the start, 0 equals the centre and 1 equals the end
     * @param partY -1, 0 or 1. -1 equals the start, 0 equals the centre and 1 equals the end
     * @return */
    default IGuiPosition getPosition(int partX, int partY) {
        IntSupplier x = partX < 0 ? this::getX : partX > 0 ? this::getEndX : this::getCenterX;
        IntSupplier y = partY < 0 ? this::getY : partY > 0 ? this::getEndY : this::getCenterY;
        return new PositionCallable(x, y);
    }

    @Override
    default IGuiArea offset(IGuiPosition by) {
        return offset(by::getX, by::getY);
    }

    @Override
    default IGuiArea offset(int x, IntSupplier y) {
        return offset(() -> x, y);
    }

    @Override
    default IGuiArea offset(IntSupplier x, int y) {
        return offset(x, () -> y);
    }

    @Override
    default IGuiArea offset(IntSupplier x, IntSupplier y) {
        return create(() -> getX() + x.getAsInt(), () -> getY() + y.getAsInt(), this::getWidth, this::getHeight);
    }

    @Override
    default IGuiArea offset(int x, int y) {
        return create(() -> getX() + x, () -> getY() + y, this::getWidth, this::getHeight);
    }

    default IGuiArea resize(int newWidth, int newHeight) {
        return create(this::getX, this::getY, () -> newWidth, () -> newHeight);
    }

    default IGuiArea expand(int by) {
        return expand(by, by);
    }

    default IGuiArea expand(int dX, int dY) {
        return create(() -> getX() - dX, () -> getY() - dY, () -> getWidth() + dX * 2, () -> getHeight() + dY * 2);
    }

    default IGuiArea expand(IntSupplier by) {
        if (by instanceof IConstantNode) {
            return expand(by.getAsInt());
        }
        return expand(by, by);
    }

    default IGuiArea expand(IntSupplier dX, IntSupplier dY) {
        if (dX instanceof IConstantNode && dY instanceof IConstantNode) {
            return expand(dX.getAsInt(), dY.getAsInt());
        }
        return create(//
            () -> getX() - dX.getAsInt(),//
            () -> getY() - dY.getAsInt(),//
            () -> getWidth() + dX.getAsInt() * 2,//
            () -> getHeight() + dY.getAsInt() * 2//
        );
    }

    static IGuiArea create(IntSupplier x, IntSupplier y, IntSupplier width, IntSupplier height) {
        if (x instanceof IConstantNode && y instanceof IConstantNode && width instanceof IConstantNode
            && height instanceof IConstantNode) {
            return new GuiRectangle(x.getAsInt(), y.getAsInt(), width.getAsInt(), height.getAsInt());
        }
        return new AreaCallable(x, y, width, height);
    }
}
