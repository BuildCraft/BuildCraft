package buildcraft.api.bpt;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;

public class TransformRotation implements IBptTransform {
    public final Axis axis;
    public final Rotation rotation;

    public TransformRotation(Axis axis, Rotation rotation) {
        this.axis = axis;
        this.rotation = rotation;
    }

    public boolean isSimpleHorizontalRotation() {
        return axis == Axis.Y;
    }
}
