package buildcraft.api.bpt;

import net.minecraft.block.Block;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public abstract class Schematic {
    /** Rotates this schematic in-place according to the same methods in
     * {@link Block#withRotation(net.minecraft.block.state.IBlockState, Rotation)}. */
    public abstract void rotate(Rotation rotation);

    /** Attempts to build this schematic from the builder. This should not set the blocks or extract items from the
     * builder, but should provide tasks for the builder to complete.
     * 
     * @param builder The builder that will execute the tasks
     * @param pos The position to build this schematic at
     * @return A collection of all the tasks you need doing to complete the schematic. */
    public abstract Iterable<IBptTask> createTasks(IBuilder builder, BlockPos pos);

    /** Clears the way for this schematic to build properly.
     * 
     * @param builder
     * @return A blueprint clearer that will dispatch the tasks necessary for clearing. You are recommended to use
     *         {@link DefaultBptActions#REQUIRE_AIR} if you just want air, or {@link DefaultBptActions#LEAVE} if you don't
     *         need to make any changes to the existing block. */
    public abstract PreBuildAction createClearingTask(IBuilder builder, BlockPos pos);

    public enum EnumPreBuildAction {
        LEAVE,
        REQUIRE_AIR,
        CUSTOM_REMOVAL;
    }

    public interface PreBuildAction {
        EnumPreBuildAction getType();

        Iterable<IBptTask> getTasks(IBuilder builder, BlockPos pos);
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
        public Iterable<IBptTask> getTasks(IBuilder builder, BlockPos pos) {
            throw new IllegalStateException("You are responsible for creating tasks for " + type);
        }
    }
}
