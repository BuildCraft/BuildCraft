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

    @Override
    public String getParameterName() {
        return "axis";
    }
}
