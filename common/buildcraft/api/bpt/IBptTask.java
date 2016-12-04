package buildcraft.api.bpt;

import java.util.Set;

import net.minecraft.util.EnumFacing;

/** Represents a task that a builder can do to build part of its schematic. This should cache its
 * {@link IBuilderAccessor}.
 * 
 * since 10 April 2016 */
@Deprecated
public interface IBptTask extends IBptWriter {
    Set<EnumFacing> getRequiredSolidFaces(IBuilderAccessor builder);

    /** Checks to see if this task part has already been done, or has just completed. */
    boolean isDone(IBuilderAccessor builder);

    /** @param microJoules The number of micro joules to receive
     * @return The number of milliJoules left over */
    long receivePower(IBuilderAccessor builder, long microJoules);

    /** @return */
    long getRequiredMicroJoules(IBuilderAccessor builder);
}
