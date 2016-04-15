package buildcraft.api.bpt;

import buildcraft.api.IUniqueWriter;

/** Represents a task that a builder can do to build part of its schematic. This should cache its {@link IBuilder} as
 * {@link IMjTask} is not aware of building tasks.
 * 
 * @date Created on 10 Apr 2016 by AlexIIL */
public interface IBptTask extends IUniqueWriter {
    /** Checks if this task can be started (so it can start to receive power). */
    boolean isReady();

    /** Checks to see if this blueprint part has already been done, or has just completed. */
    boolean isDone();

    /** @param milliJoules The number of milli joules to receive
     * @return The number of milliJoules left over */
    int receivePower(int milliJoules);

    /** @return */
    int getRequiredMilliJoules();
}
