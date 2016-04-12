package buildcraft.api.bpt;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public abstract class SchematicBlock extends Schematic {
    /** Stores the offset of this block. */
    protected BlockPos offset;

    @Override
    public void translate(Vec3i by) {
        offset = offset.add(by);
    }
}
