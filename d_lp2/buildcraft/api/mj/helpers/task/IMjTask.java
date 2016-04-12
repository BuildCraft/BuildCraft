package buildcraft.api.mj.helpers.task;

import buildcraft.api.IUniqueWriter;
import buildcraft.api.mj.helpers.MjSimpleConsumer;

/** Represents a task that requires power. Added to {@link MjSimpleConsumer} to auto-request power. */
public interface IMjTask extends IUniqueWriter {
    /** Checks if this task is ready to be started. */
    boolean isReadyYet();

    int requiredMilliWatts();

    void tick(boolean isGettingPower);

    boolean isDone();
}
