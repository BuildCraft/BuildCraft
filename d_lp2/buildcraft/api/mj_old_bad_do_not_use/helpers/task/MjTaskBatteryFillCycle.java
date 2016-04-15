package buildcraft.api._mj.helpers.task;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.mj.MjBattery;

/** A task that attempts to cycle-fill a battery, so waiting until it drains to a certain level before charging, and
 * stopping when it reaches a certain level. */
public class MjTaskBatteryFillCycle implements IMjTaskReadable {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftapi:battery_fill_cycle");
    private final int startFillLevel, milliWatts;
    private final MjBattery battery;
    private final String battryIdentifier;
    private boolean needsFilling = false;

    public MjTaskBatteryFillCycle(int startFillLevel, int milliWatts, MjBattery battery, String battryIdentifier) {
        this.startFillLevel = startFillLevel;
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
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("needsFilling", needsFilling);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        needsFilling = nbt.getBoolean("needsFilling");
    }

    @Override
    public int requiredMilliWatts() {
        return needsFilling ? milliWatts : 0;
    }

    @Override
    public void tick(boolean isGettingPower) {
        if (isGettingPower) {
            battery.addPower(milliWatts);
            if (battery.isFull()) {
                needsFilling = false;
            }
        } else {
            needsFilling = battery.getContained() <= startFillLevel;
        }
    }
    
    @Override
    public boolean isDone() {
        return false;
    }
}
