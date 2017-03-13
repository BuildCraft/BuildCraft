package buildcraft.builders.snapshot;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SchematicBlockContext {
    public final World world;
    public final BlockPos pos;
    public final BlockPos basePos;

    public SchematicBlockContext(World world, BlockPos pos, BlockPos basePos) {
        this.world = world;
        this.pos = pos;
        this.basePos = basePos;
    }
}
