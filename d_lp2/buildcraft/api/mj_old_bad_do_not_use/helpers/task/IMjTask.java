package buildcraft.api._mj.helpers.task;

import buildcraft.api.IUniqueWriter;
import buildcraft.api._mj.helpers.MjSimpleConsumer;

/** Represents a task that requires power. Added to {@link MjSimpleConsumer} to auto-request power. */
public interface IMjTask extends IUniqueWriter {
    /** You should check if you actually need the power here too. */
    int requiredMilliWatts();

    void tick(boolean isGettingPower);

    /** @return True if this task has completed its work and can be discarded by whatever holds it. */
    boolean isDone();
}
