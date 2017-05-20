package buildcraft.lib.misc.data;

import static net.minecraft.util.EnumFacing.Axis.*;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.util.EnumFacing.Axis;

import buildcraft.lib.misc.data.AxisOrder.Inversion;

public enum EnumAxisOrder {
    XYZ(X, Y, Z),
    XZY(X, Z, Y),
    YXZ(Y, X, Z),
    YZX(Y, Z, X),
    ZXY(Z, X, Y),
    ZYX(Z, Y, X);

    public static final EnumAxisOrder[] VALUES = values();

    private static final Map<String, EnumAxisOrder> orderMap;

    static {
        ImmutableMap.Builder<String, EnumAxisOrder> builder = ImmutableMap.builder();
        for (EnumAxisOrder order : values()) {
            builder.put(order.name(), order);
        }
        orderMap = builder.build();
    }

    public final Axis first, second, third;

    EnumAxisOrder(Axis a, Axis b, Axis c) {
        this.first = a;
        this.second = b;
        this.third = c;
    }

    public static EnumAxisOrder getOrder(String name) {
        EnumAxisOrder order = orderMap.get(name);
        if (order == null) {
            order = XZY;
        }
        return order;
    }

    public static EnumAxisOrder getOrder(Axis first, Axis b) {
        if (first == X) {
            return b == Y ? XYZ : XZY;
        } else if (first == Y) {
            return b == X ? YXZ : YZX;
        } else {
            return b == X ? ZXY : ZYX;
        }
    }

    public AxisOrder getMinToMaxOrder() {
        return AxisOrder.getFor(this, Inversion.PPP);
    }

    public AxisOrder getMaxToMinOrder() {
        return AxisOrder.getFor(this, Inversion.NNN);
    }
}
