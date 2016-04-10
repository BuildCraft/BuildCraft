package buildcraft.api.bpt;

import buildcraft.api.mj.helpers.task.IMjTask;

/** Represents a task that a builder can do to build part of its schematic. This should cache its {@link IBuilder} as
 * {@link IMjTask} is not aware of building tasks.
 * 
 * @date Created on 10 Apr 2016 by AlexIIL */
public interface IBptTask extends IMjTask {
    /** Checks to see if this blueprint part has already been done, or has just completed. */
    @Override
    boolean isDone();

    /** Checks to see if the builder has all the requirements needed for this part, or the {@link IBuilder} has the
     * permission to not use up items. */
    @Override
    boolean isReadyYet();
}
