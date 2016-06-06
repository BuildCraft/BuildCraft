package buildcraft.api.bpt;

import java.util.Set;

import net.minecraft.util.EnumFacing;

import buildcraft.api.IUniqueWriter;

/** Represents a task that a builder can do to build part of its schematic. This should cache its {@link IBuilderAccessor}.
 * 
 * @date Created on 10 Apr 2016 by AlexIIL */
public interface IBptTask extends IUniqueWriter {
    Set<EnumFacing> getRequiredSolidFaces(IBuilderAccessor builder);

    /** Checks to see if this task part has already been done, or has just completed. */
    boolean isDone(IBuilderAccessor builder);

    /** @param milliJoules The number of milli joules to receive
     * @return The number of milliJoules left over */
    int receivePower(IBuilderAccessor builder, int milliJoules);

    /** @return */
    int getRequiredMilliJoules(IBuilderAccessor builder);
}
