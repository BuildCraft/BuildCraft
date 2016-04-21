package buildcraft.lib.bpt.helper;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.bpt.IBptTask;

public abstract class BptTaskSimple implements IBptTask {
    private final int required;
    private int stored;

    public BptTaskSimple(int required) {
        this.required = required;
    }

    public BptTaskSimple(NBTTagCompound nbt) {
        this.required = nbt.getInteger("required");
        this.stored = nbt.getInteger("stored");
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("required", required);
        nbt.setInteger("stored", stored);
        return nbt;
    }

    @Override
    public int receivePower(int milliJoules) {
        int accepted = Math.min(getRequiredMilliJoules(), milliJoules);
        this.stored += accepted;
        if (stored >= required) {
            onReceiveFullPower();
        }
        return milliJoules - accepted;
    }

    @Override
    public int getRequiredMilliJoules() {
        return required - stored;
    }

    protected abstract void onReceiveFullPower();
}
