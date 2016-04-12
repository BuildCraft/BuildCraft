package buildcraft.api.mj.helpers.task;

import net.minecraft.nbt.NBTTagCompound;

/** Represents a task that only requires a set amount of power, and
 * 
 * @date Created on 10 Apr 2016 by AlexIIL */
public abstract class MjTaskOnce implements IMjTask {
    private final int milliJoules, ticks, watts;
    private final boolean allAtOnce;
    private int receivedJoules = 0;

    public MjTaskOnce(int milliJoules, int ticks, boolean allAtOnce) {
        this.milliJoules = milliJoules;
        this.ticks = ticks;
        this.allAtOnce = allAtOnce;
        watts = milliJoules / Math.max(1, ticks);
    }

    public MjTaskOnce(NBTTagCompound nbt) {
        this.milliJoules = nbt.getInteger("milliJoules");
        this.ticks = nbt.getInteger("ticks");
        this.watts = nbt.getInteger("watts");
        this.allAtOnce = nbt.getBoolean("allAtOnce");
        this.receivedJoules = nbt.getInteger("receivedJoules");
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("milliJoules", milliJoules);
        nbt.setInteger("ticks", ticks);
        nbt.setInteger("watts", watts);
        nbt.setBoolean("allAtOnce", allAtOnce);
        nbt.setInteger("receivedJoules", receivedJoules);
        return nbt;
    }

    @Override
    public int requiredMilliWatts() {
        return receivedJoules >= milliJoules ? 0 : watts;
    }

    @Override
    public final void tick(boolean isGettingPower) {
        if (isGettingPower) {
            receivedJoules += watts / 20;
            if (!allAtOnce) {
                onRecievePower(receivedJoules);
            } else if (receivedJoules >= milliJoules) {
                onRecievePower(receivedJoules);
            }
        }
    }

    protected abstract void onRecievePower(int mJSoFar);
}
