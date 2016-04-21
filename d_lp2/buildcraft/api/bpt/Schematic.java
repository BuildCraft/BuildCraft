package buildcraft.api.bpt;

import net.minecraft.block.Block;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.Vec3i;

public abstract class Schematic {
    /** Rotates this schematic in-place according to the same methods in
     * {@link Block#withRotation(net.minecraft.block.state.IBlockState, Rotation)}. You do NOT need to translate
     * yourself at all if your own "offset" is not the origin- the rotation method will call {@link #translate(Vec3i)}
     * along with this. */
    public abstract void rotate(Rotation rotation);

    /** Mirrors this schematic in-place according to the same methods in
     * {@link Block#withMirror(net.minecraft.block.state.IBlockState, Mirror)}. You do NOT need to translate yourself at
     * all if your own "offset" is not the origin- the mirroring method will call {@link #translate(Vec3i)} along with
     * this. */
    public abstract void mirror(Mirror mirror);

    /** Translates this schematic by the given vector. Generally only tile entities and normal entities need to
     * translate like this. */
    public abstract void translate(Vec3i by);

    /** Attempts to build this schematic from the builder. This should not set the blocks or extract items from the
     * builder, but should provide tasks for the builder to complete.
     * 
     * @param stage The current stage the builder is in.
     * @return A collection of all the tasks you need doing to complete the schematic. */
    public abstract Iterable<IBptTask> createTasks(IBuilder builder);
}
