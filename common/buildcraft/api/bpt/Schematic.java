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

    /** Translates this schematic by the given vector. Generally only tile entities and normal entities need to
     * translate like this. */
    public abstract void translate(Vec3i by);

    /** Attempts to build this schematic from the builder. This should not set the blocks or extract items from the
     * builder, but should provide tasks for the builder to complete.
     * 
     * @param builder The builder that will execute the tasks
     * @return A collection of all the tasks you need doing to complete the schematic. */
    public abstract Iterable<IBptTask> createTasks(IBuilder builder);

    /** Clears the way for this schematic to build properly.
     * 
     * @param builder
     * @return A blueprint clearer that will dispatch the tasks necessary for clearing. You are recommended to use
     *         {@link DefaultBptClearers#REMOVE} if you just want air, or {@link DefaultBptClearers#NONE} if you don't
     *         need to make any changes to the existing block. */
    public abstract BptClearer createClearingTask(IBuilder builder);

    public enum EnumClearType {
        NONE,
        REMOVE,
        CUSTOM;
    }

    public interface BptClearer {
        EnumClearType getType();

        Iterable<IBptTask> getTasks();
    }

    public enum DefaultBptClearers implements BptClearer {
        NONE(EnumClearType.NONE),
        REMOVE(EnumClearType.REMOVE);
        private final EnumClearType type;

        private DefaultBptClearers(EnumClearType type) {
            this.type = type;
        }

        @Override
        public EnumClearType getType() {
            return type;
        }

        @Override
        public Iterable<IBptTask> getTasks() {
            throw new IllegalStateException("You are responsible for creating tasks for " + type);
        }
    }
}
