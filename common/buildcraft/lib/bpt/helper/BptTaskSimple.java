package buildcraft.lib.bpt.helper;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilder;

public abstract class BptTaskSimple implements IBptTask {
    public final int required;
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
    public int receivePower(IBuilder builder, int milliJoules) {
        int accepted = Math.min(getRequiredMilliJoules(builder), milliJoules);
        this.stored += accepted;
        if (stored >= required) {
            onReceiveFullPower(builder);
        }
        return milliJoules - accepted;
    }

    @Override
    public int getRequiredMilliJoules(IBuilder builder) {
        return required - stored;
    }

    protected abstract void onReceiveFullPower(IBuilder builder);
}
