/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;

public class AxisOrder {
    private static final Table<EnumAxisOrder, Inversion, AxisOrder> allOrders;

    static {
        ImmutableTable.Builder<EnumAxisOrder, Inversion, AxisOrder> builder = ImmutableTable.builder();
        for (EnumAxisOrder order : EnumAxisOrder.VALUES) {
            for (Inversion inv : Inversion.VALUES) {
                builder.put(order, inv, new AxisOrder(order, inv));
            }
        }
        allOrders = builder.build();
    }

    public final EnumAxisOrder order;
    public final Inversion inversion;
    public final Direction first, second, third;

    /** Creates an axis order that will scan axis in the order given, going in the directions specified by
     * positiveFirst, positiveSecond and positiveThird. If all are true then it will start at the smallest one and end
     * up at the biggest one. */
    private AxisOrder(EnumAxisOrder order, Inversion inv) {
        this.order = order;
        this.inversion = inv;
        first = Direction.get(inv.first, order.first);
        second = Direction.get(inv.second, order.second);
        third = Direction.get(inv.third, order.third);
    }

    public static AxisOrder readNbt(CompoundTag nbt) {
        return getFor(//
                EnumAxisOrder.getOrder(nbt.getString("order")),//
                Inversion.getFor(nbt.getString("inversion"))//
        );
    }

    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("order", order.name());
        nbt.putString("inversion", inversion.name());
        return nbt;
    }

    public static AxisOrder getFor(EnumAxisOrder order, Inversion inv) {
        if (order == null) throw new NullPointerException("order");
        if (inv == null) throw new NullPointerException("inv");
        AxisOrder axisOrder = allOrders.get(order, inv);
        if (axisOrder == null) {
            throw new IllegalStateException("Tried to lookup " + order + ", " + inv + " but failed!");
        }
        return axisOrder;
    }

    @Override
    public String toString() {
        return first + ", " + second + ", " + third;
    }

    public AxisOrder invertFirst() {
        return allOrders.get(order, Inversion.getFor(first.getOpposite(), second, third));
    }

    public AxisOrder invertSecond() {
        return allOrders.get(order, Inversion.getFor(first, second.getOpposite(), third));
    }

    public AxisOrder invertThird() {
        return allOrders.get(order, Inversion.getFor(first, second, third.getOpposite()));
    }

    public AxisOrder invert(Axis axis) {
        if (axis == first.getAxis()) return invertFirst();
        if (axis == second.getAxis()) return invertSecond();
        else return invertThird();
    }

    public enum Inversion {
        PPP,
        PPN,
        PNP,
        PNN,
        NPP,
        NPN,
        NNP,
        NNN;

        public static final Inversion[] VALUES = values();

        public final AxisDirection first, second, third;

        Inversion() {
            first = getFor(name().charAt(0));
            second = getFor(name().charAt(1));
            third = getFor(name().charAt(2));
        }

        private static AxisDirection getFor(char charAt) {
            if (charAt == 'P') {
                return AxisDirection.POSITIVE;
            } else if (charAt == 'N') {
                return AxisDirection.NEGATIVE;
            }
            throw new Error("Unknown char " + charAt);
        }

        public static Inversion getFor(Direction first, Direction second, Direction third) {
            return getFor(first.getAxisDirection(), second.getAxisDirection(), third.getAxisDirection());
        }

        public static Inversion getFor(AxisDirection first, AxisDirection second, AxisDirection third) {
            return getFor(first == AxisDirection.POSITIVE, second == AxisDirection.POSITIVE, third == AxisDirection.POSITIVE);
        }

        public static Inversion getFor(boolean first, boolean second, boolean third) {
            if (first) {
                if (second) {
                    return third ? PPP : PPN;
                } else {
                    return third ? PNP : PNN;
                }
            } else {
                if (second) {
                    return third ? NPP : NPN;
                } else {
                    return third ? NNP : NNN;
                }
            }
        }

        public static Inversion getFor(String name) {
            if (name == null || name.length() != 3) return PPP;
            boolean first = name.charAt(0) == 'P';
            boolean second = name.charAt(1) == 'P';
            boolean third = name.charAt(2) == 'P';
            return getFor(first, second, third);
        }
    }
}
