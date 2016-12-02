package buildcraft.api.bpt;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.bpt.task.TaskUsable;

/**
 * This class really isn't needed any more - this has been mega-deprecated as its worse than the old schematic class
 */
@Deprecated
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
     * builder, but should provide the task for the builder to complete. <br>
     * Note that {@link IBuilderAccessor#hasPermissionToEdit(BlockPos)} has already been called, and this will only be
     * called if it returned true.
     * 
     * @param builder The builder that will execute the tasks
     * @param pos The position to build this schematic at
     * @return A collection of all the tasks you need doing to complete the schematic. */
    public abstract TaskUsable createTask(IBuilderAccessor builder, BlockPos pos);

    /** Clears the way for this schematic to build properly.
     * 
     * @param builder
     * @return A blueprint clearer that will dispatch the tasks necessary for clearing. You are recommended to use
     *         {@link DefaultBptActions#REQUIRE_AIR} if you just want air, or {@link DefaultBptActions#LEAVE} if you
     *         don't need to make any changes to the existing block. */
    public abstract PreBuildAction createClearingTask(IBuilderAccessor builder, BlockPos pos);

    /** Attempts to either build this schematic completely, or leave the world untouched.
     * 
     * @param provider The material provider
     * @param pos The position to build at
     * @return True if this built completely, false if nothing changed. */
    public abstract boolean buildImmediatly(World world, IMaterialProvider provider, BlockPos pos);

    /** @return An approximate time cost. Values range between 1 and 100. Should be smaller for simple blocks (say air
     *         or stone) but higher for complex blocks (like a chest or furnace) */
    public int getTimeCost() {
        return 20;
    }

    public enum EnumPreBuildAction {
        LEAVE,
        REQUIRE_AIR,
        CUSTOM_REMOVAL;
    }

    public interface PreBuildAction {
        EnumPreBuildAction getType();

        TaskUsable getTask(IBuilderAccessor builder, BlockPos pos);

        /** @return A non-negative cost value for the clearing. This is just to limit the number of clearing actions per
         *         tick, return a higher value if you need to do lots of things. (1 is the minimum, 100 is the max) */
        int getTimeCost();
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
        public TaskUsable getTask(IBuilderAccessor builder, BlockPos pos) {
            if (type == EnumPreBuildAction.LEAVE) {
                return TaskUsable.NOTHING;
            }
            throw new IllegalStateException("You are responsible for creating tasks for " + type);
        }

        @Override
        public int getTimeCost() {
            return type == EnumPreBuildAction.LEAVE ? 1 : 24;
        }
    }
}
