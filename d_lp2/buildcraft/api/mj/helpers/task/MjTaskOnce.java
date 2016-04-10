package buildcraft.api.mj.helpers.task;

/** Represents a task that only requires a set amount of power, and
 * 
 * @date Created on 10 Apr 2016 by AlexIIL */
public abstract class MjTaskOnce implements IMjTask {
    private final int milliJoules, ticks, watts;
    private final boolean allAtOnce;
    private int recievedJoules = 0;

    public MjTaskOnce(int milliJoules, int ticks, boolean allAtOnce) {
        this.milliJoules = milliJoules;
        this.ticks = ticks;
        this.allAtOnce = allAtOnce;
        watts = milliJoules / Math.max(1, ticks);
    }

    @Override
    public int requiredMilliWatts() {
        return recievedJoules >= milliJoules ? 0 : watts;
    }

    @Override
    public final void tick(boolean isGettingPower) {
        if (isGettingPower) {
            recievedJoules += watts / 20;
            if (!allAtOnce) {
                onRecievePower(recievedJoules);
            } else if (recievedJoules >= milliJoules) {
                onRecievePower(recievedJoules);
            }
        }
    }

    protected abstract void onRecievePower(int mJSoFar);
}
