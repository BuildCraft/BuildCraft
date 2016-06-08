package buildcraft.api.bpt;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public abstract class Schematic {
    /** Attempts to mirror this schematic in the given axis. (So given Axis.Y you should invert top-to-bottom)
     * 
     * @param axis The axis to mirror in.
     * @throws SchematicException if your current state cannot be rotated in the given axis, but other states could be.
     *             (So don't throw if you are a fence and you were asked to mirror in the Y axis) */
    public abstract void mirror(Axis axis);

    /** Attempts to rotate this schematic in the given axis, by the given rotation. (So for Axis.Y and
     * Rotation.CLOCKWISE_90 you should do whatever {@link IBlockState#withRotation(Rotation)} would do.)
     * 
     * @param axis The axis to rotate in.
     * @param rotation The rotation to apply.
     * @throws SchematicException if your current state cannot be rotated in the given axis, but other states could be.
     *             (So don't throw if you are rail block and you were asked to rotate in the X axis, but DO throw if you
     *             are a torch and you have been asked to rotate to be placed upside down. */
    public abstract void rotate(Axis axis, Rotation rotation);

    /** Attempts to build this schematic from the builder. This should not set the blocks or extract items from the
     * builder, but should provide tasks for the builder to complete.
     * 
     * @param builder The builder that will execute the tasks
     * @param pos The position to build this schematic at
     * @return A collection of all the tasks you need doing to complete the schematic. */
    public abstract Iterable<IBptTask> createTasks(IBuilderAccessor builder, BlockPos pos);

    /** Clears the way for this schematic to build properly.
     * 
     * @param builder
     * @return A blueprint clearer that will dispatch the tasks necessary for clearing. You are recommended to use
     *         {@link DefaultBptActions#REQUIRE_AIR} if you just want air, or {@link DefaultBptActions#LEAVE} if you
     *         don't need to make any changes to the existing block. */
    public abstract PreBuildAction createClearingTask(IBuilderAccessor builder, BlockPos pos);

    public enum EnumPreBuildAction {
        LEAVE,
        REQUIRE_AIR,
        CUSTOM_REMOVAL;
    }

    public interface PreBuildAction {
        EnumPreBuildAction getType();

        Iterable<IBptTask> getTasks(IBuilderAccessor builder, BlockPos pos);
    }

    public enum DefaultBptActions implements PreBuildAction {
        LEAVE(EnumPreBuildAction.LEAVE),
        REQUIRE_AIR(EnumPreBuildAction.REQUIRE_AIR);
        private final EnumPreBuildAction type;

        private DefaultBptActions(EnumPreBuildAction type) {
            this.type = type;
        }

        @Override
        public EnumPreBuildAction getType() {
            return type;
        }

        @Override
        public Iterable<IBptTask> getTasks(IBuilderAccessor builder, BlockPos pos) {
            throw new IllegalStateException("You are responsible for creating tasks for " + type);
        }
    }
}
