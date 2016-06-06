package buildcraft.api.bpt;

import net.minecraft.util.EnumFacing.Axis;

public class TransformMirror implements IBptTransform {
    public final Axis axis;

    public TransformMirror(Axis axis) {
        this.axis = axis;
    }
}
