/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import java.util.Iterator;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.core.IBox;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;

public class BoxIterator implements Iterator<BlockPos> {
    @Nonnull
    private final BlockPos min, max;
    private final boolean invert, repeat;// TODO: remove repeat if its not used in the future
    private AxisOrder order;
    private BlockPos current;

    public BoxIterator(IBox box, AxisOrder order, boolean invert) {
        this(box.min(), box.max(), order, invert);
    }

    public BoxIterator(BlockPos min, BlockPos max, AxisOrder order, boolean invert) {
        this(min, max, invert, false, order, null);
    }

    private BoxIterator(BlockPos min, BlockPos max, boolean invert, boolean repeat, AxisOrder order, BlockPos current) {
        if (min == null) throw new NullPointerException("min");
        if (max == null) throw new NullPointerException("max");
        if (order == null) throw new NullPointerException("order");
        this.min = min;
        this.max = max;
        this.invert = invert;
        this.repeat = repeat;
        this.order = order;
        this.current = current == null ? getStart() : current;
    }

    public static BoxIterator readFromNbt(NBTTagCompound nbt) {
        BlockPos min = nbt.hasKey("min") ? NBTUtilBC.readBlockPos(nbt.getTag("min")) : null;
        BlockPos max = nbt.hasKey("max") ? NBTUtilBC.readBlockPos(nbt.getTag("max")) : null;
        boolean invert = nbt.getBoolean("invert");
        boolean repeat = false;
        AxisOrder order = AxisOrder.readNbt(nbt.getCompoundTag("order"));
        BlockPos current = nbt.hasKey("current") ? NBTUtilBC.readBlockPos(nbt.getTag("current")) : null;
        if (min == null || max == null || order == null) {
            return null;
        }
        return new BoxIterator(min, max, invert, repeat, order, current);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("min", NBTUtilBC.writeBlockPos(min));
        nbt.setTag("max", NBTUtilBC.writeBlockPos(max));
        nbt.setBoolean("invert", invert);
        // repeat
        nbt.setTag("order", order.writeNBT());
        if (current != null) {
            nbt.setTag("current", NBTUtilBC.writeBlockPos(current));
        }
        return nbt;
    }

    private BlockPos getStart() {
        BlockPos pos = BlockPos.ORIGIN;
        pos = replace(pos, order.first);
        pos = replace(pos, order.second);
        return replace(pos, order.third);
    }

    private BlockPos replace(BlockPos toReplace, EnumFacing facing) {
        BlockPos with = facing.getAxisDirection() == AxisDirection.POSITIVE ? min : max;
        return VecUtil.replaceValue(toReplace, facing.getAxis(), VecUtil.getValue(with, facing.getAxis()));
    }

    public BlockPos getCurrent() {
        return current;
    }

    @Nonnull
    public BlockPos getMin() {
        return min;
    }

    @Nonnull
    public BlockPos getMax() {
        return max;
    }

    public boolean isInvert() {
        return invert;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public AxisOrder getOrder() {
        return order;
    }

    /** Moves on to the next block. Unlike {@link #next()} this returns the one AFTER that one, so you cannot use
     * {@link #hasNext()}! */
    public BlockPos advance() {
        if (current == null) {
            current = getStart();
            return getCurrent();
        }
        current = increment(current, order.first);
        if (shouldReset(current, order.first)) {
            if (invert) {
                order = order.invertFirst();
            }
            current = replace(current, order.first);
            current = increment(current, order.second);
            if (shouldReset(current, order.second)) {
                if (invert) {
                    order = order.invertSecond();
                }
                current = replace(current, order.second);
                current = increment(current, order.third);
                if (shouldReset(current, order.third)) {
                    if (repeat) {
                        if (invert) {
                            order = order.invertThird();
                        }
                        current = replace(current, order.third);
                    } else {
                        current = null;
                    }
                }
            }
        }
        return getCurrent();
    }

    private static BlockPos increment(BlockPos pos, EnumFacing facing) {
        int diff = facing.getAxisDirection().getOffset();
        int value = VecUtil.getValue(pos, facing.getAxis()) + diff;
        return VecUtil.replaceValue(pos, facing.getAxis(), value);
    }

    private boolean shouldReset(BlockPos current, EnumFacing facing) {
        int lstReturned = VecUtil.getValue(current, facing.getAxis());
        BlockPos goingTo = facing.getAxisDirection() == AxisDirection.POSITIVE ? max : min;
        int to = VecUtil.getValue(goingTo, facing.getAxis());
        if (facing.getAxisDirection() == AxisDirection.POSITIVE) return lstReturned > to;
        return lstReturned < to;
    }

    public boolean hasFinished() {
        return current == null;
    }

    // Iterator

    @Override
    public boolean hasNext() {
        return !hasFinished();
    }

    @Override
    public BlockPos next() {
        BlockPos c = current;
        advance();
        return c;
    }
}
