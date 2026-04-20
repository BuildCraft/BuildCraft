/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc.data;

import java.util.Iterator;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.IBox;
import ct.buildcraft.lib.misc.StringUtilBC;
import ct.buildcraft.lib.misc.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;

public class BoxIterator implements Iterator<BlockPos> {
    @Nonnull
    private final BlockPos min, max;
    private final boolean invert, repeat;// TODO: remove repeat if its not used in the future
    private AxisOrder order;
    private BlockPos current;
    private boolean hasRepeated = false;

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

    public static BoxIterator readFromNbt(CompoundTag nbt) {
        BlockPos min = BlockPos.of(nbt.getLong("min"));
        BlockPos max = BlockPos.of(nbt.getLong("max"));
        boolean invert = nbt.getBoolean("invert");
        boolean repeat = false;
        AxisOrder order = AxisOrder.readNbt(nbt.getCompound("order"));
        BlockPos current = BlockPos.of(nbt.getLong("current"));
        if (min == null || max == null || order == null) {
            return null;
        }
        return new BoxIterator(min, max, invert, repeat, order, current);
    }

    public CompoundTag writeToNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("min", min.asLong());
        nbt.putLong("max", min.asLong());
        nbt.putBoolean("invert", invert);
        // repeat
        nbt.put("order", order.writeNBT());
        if (current != null) {
            nbt.putLong("current", current.asLong());
        }
        return nbt;
    }

    private BlockPos getStart() {
        BlockPos pos = BlockPos.ZERO;
        pos = replace(pos, order.first);
        pos = replace(pos, order.second);
        return replace(pos, order.third);
    }

    private BlockPos replace(BlockPos toReplace, Direction facing) {
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

    @Override
    public String toString() {
        return "{BoxIterator [" + StringUtilBC.blockPosToString(min) + "] -> [" + StringUtilBC
            .blockPosToString(max) + "] @ " + StringUtilBC.blockPosToString(current) + " order: [" + order + "]"
            + (invert ? " inverting" : "") + (repeat ? " repeating" : "") + " }";
    }

    /** Moves on to the next block. Unlike {@link #next()} this returns the one AFTER that one, so you cannot use
     * {@link #hasNext()}! */
    public BlockPos advance() {
        if (current == null) {
            current = getStart();
            return getCurrent();
        }
        current = increment(current, order.first);
        if (shouldReset(order.first)) {
            if (invert) {
                order = order.invertFirst();
            }
            current = replace(current, order.first);
            current = increment(current, order.second);
            if (shouldReset(order.second)) {
                if (invert) {
                    order = order.invertSecond();
                }
                current = replace(current, order.second);
                current = increment(current, order.third);
                if (shouldReset(order.third)) {
                    if (repeat) {
                        if (invert) {
                            order = order.invertThird();
                        }
                        current = replace(current, order.third);
                        hasRepeated = true;
                    } else {
                        current = null;
                    }
                }
            }
        }
        return getCurrent();
    }

    private static BlockPos increment(BlockPos pos, Direction facing) {
        int diff = facing.getAxisDirection().getStep();
        int value = VecUtil.getValue(pos, facing.getAxis()) + diff;
        return VecUtil.replaceValue(pos, facing.getAxis(), value);
    }

    private boolean shouldReset(Direction facing) {
        int lstReturned = VecUtil.getValue(current, facing.getAxis());
        BlockPos goingTo = facing.getAxisDirection() == AxisDirection.POSITIVE ? max : min;
        int to = VecUtil.getValue(goingTo, facing.getAxis());
        if (facing.getAxisDirection() == AxisDirection.POSITIVE) return lstReturned > to;
        return lstReturned < to;
    }

    /** Checks to see if {@link #advance()} has ever, or could ever, return the given block position. */
    public boolean contains(BlockPos pos) {
        if (pos.getX() < min.getX() || pos.getX() > max.getX()) {
            return false;
        }
        if (pos.getY() < min.getY() || pos.getY() > max.getY()) {
            return false;
        }
        if (pos.getZ() < min.getZ() || pos.getZ() > max.getZ()) {
            return false;
        }
        return true;
    }

    /** Checks to see if {@link #advance()} will return the given block position before this repeats. */
    public boolean willVisit(BlockPos pos) {
        if (!contains(pos)) {
            return false;
        }
        if (current == null) {
            return true;
        }
        return compare(pos) < 0;
    }

    /** Checks to see if {@link #advance()} has already returned the given block position before the last repeat. */
    public boolean hasVisited(BlockPos pos) {
        if (!contains(pos)) {
            return false;
        }
        if (current == null && !hasRepeated) {
            return false;
        }
        return compare(pos) >= 0;
    }

    private int compare(BlockPos pos) {
        int cmp = compare(pos, order.third);
        if (cmp != 0) {
            return cmp;
        }
        cmp = compare(pos, order.second);
        if (cmp != 0) {
            return cmp;
        }
        return compare(pos, order.first);
    }

    private int compare(BlockPos pos, Direction direction) {
        int argVal = VecUtil.getValue(pos, direction.getAxis());
        int currentVal = VecUtil.getValue(current, direction.getAxis());
        return (currentVal - argVal) * direction.getAxisDirection().getStep();
    }

    /** Moves this iterator so that {@link #advance()} will return the given block position next.
     * 
     * @throws IllegalArgumentException if {@link #contains(BlockPos)} doesn't return true. */
    public void moveTo(BlockPos pos) {
        if (!contains(pos)) {
            throw new IllegalArgumentException("This " + this + " doesn't contain " + pos + "!");
        }

        Direction a = order.first;
        Direction b = order.second;
        Direction c = order.third;

        int valueA = VecUtil.getValue(pos, a.getAxis());
        int valueB = VecUtil.getValue(pos, b.getAxis());
        int valueC = VecUtil.getValue(pos, c.getAxis());

        int boundA = VecUtil.getValue(max, min, a);
        int boundB = VecUtil.getValue(max, min, b);
        int boundC = VecUtil.getValue(max, min, c);

        if (!invert) {
            if (valueA != boundA) {
                current = pos.offset(a.getOpposite().getNormal());
                return;
            }

            if (valueB != boundB) {
                current = pos.offset(b.getOpposite().getNormal());
                current = VecUtil.replaceValue(current, a.getAxis(), VecUtil.getValue(min, max, a));
                return;
            }

            if (valueC != boundC) {
                current = pos.offset(c.getOpposite().getNormal());
                current = VecUtil.replaceValue(current, a.getAxis(), VecUtil.getValue(min, max, a));
                current = VecUtil.replaceValue(current, b.getAxis(), VecUtil.getValue(min, max, b));
                return;
            }
            current = null;
            return;
        }

        if (current == null) {
            current = getStart();
        }

        int db = compare(pos, b);
        int dc = compare(pos, c);

        BlockPos size = max.subtract(min);
        int sizeB = 1 + VecUtil.getValue(size, b.getAxis());

        if ((dc * sizeB + db) % 2 == 1) {
            order = order.invertFirst();
        }
        if (dc % 2 == 1) {
            order = order.invertSecond();
        }

        a = order.first;
        b = order.second;
        c = order.third;

        boundA = VecUtil.getValue(max, min, a);
        boundB = VecUtil.getValue(max, min, b);
        boundC = VecUtil.getValue(max, min, c);

        if (valueA != boundA) {
            current = pos.offset(order.first.getOpposite().getNormal());
        } else if (valueB != boundB) {
            current = pos.offset(order.second.getOpposite().getNormal());
            order = order.invertFirst();
        } else if (valueC != boundC) {
            current = pos.offset(order.third.getOpposite().getNormal());
            order = order.invertFirst();
            order = order.invertSecond();
        } else {
            current = null;
        }
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
