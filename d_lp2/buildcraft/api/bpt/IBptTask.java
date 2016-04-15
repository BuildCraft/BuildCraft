package buildcraft.api.bpt;

/** Represents a task that a builder can do to build part of its schematic. This should cache its {@link IBuilder} as
 * {@link IMjTask} is not aware of building tasks.
 * 
 * @date Created on 10 Apr 2016 by AlexIIL */
public interface IBptTask {
    /** Checks to see if this blueprint part has already been done, or has just completed. */
    boolean isDone();

    /** @return */
    int getRequiredMilliJoules();
}
