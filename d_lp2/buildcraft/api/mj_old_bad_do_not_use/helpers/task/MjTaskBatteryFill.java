package buildcraft.api._mj.helpers.task;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.mj.MjBattery;

/** A task that attempts to keep a battery full, at all times, forever.
 * 
 * @date Created on 10 Apr 2016 by AlexIIL */
public class MjTaskBatteryFill implements IMjTaskReadable {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftapi:battery_fill");
    private final int milliWatts;
    private final MjBattery battery;
    private final String battryIdentifier;

    public MjTaskBatteryFill(int milliWatts, MjBattery battery, String battryIdentifier) {
        this.milliWatts = milliWatts;
        this.battery = battery;
        this.battryIdentifier = battryIdentifier;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public String getUID() {
        return battryIdentifier;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {}

    @Override
    public int requiredMilliWatts() {
        return battery.isFull() ? 0 : milliWatts;
    }

    @Override
    public void tick(boolean isGettingPower) {
        if (isGettingPower) {
            battery.addPower(milliWatts);
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }
}
