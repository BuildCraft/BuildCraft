package buildcraft.lib.bpt.helper;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilderAccessor;

public abstract class BptTaskSimple implements IBptTask {
    public final long required;
    private long stored;

    public BptTaskSimple(long required) {
        this.required = required;
    }

    public BptTaskSimple(NBTTagCompound nbt) {
        this.required = nbt.getLong("required");
        this.stored = nbt.getLong("stored");
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("required", required);
        nbt.setLong("stored", stored);
        return nbt;
    }

    @Override
    public long receivePower(IBuilderAccessor builder, long microJoules) {
        long accepted = Math.min(getRequiredMicroJoules(builder), microJoules);
        this.stored += accepted;
        if (stored >= required) {
            onReceiveFullPower(builder);
        }
        return microJoules - accepted;
    }

    @Override
    public long getRequiredMicroJoules(IBuilderAccessor builder) {
        return required - stored;
    }

    protected abstract void onReceiveFullPower(IBuilderAccessor builder);
}
