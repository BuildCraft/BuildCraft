package buildcraft.lib.bpt.task;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBuilderAccessor;

public class ConditionUsable {
    public final ICondition condition;
    public boolean hasChecked = false;

    public ConditionUsable(ICondition condition) {
        this.condition = condition;
    }

    public void readFromNbt(NBTTagCompound nbt) {
        hasChecked = nbt.getBoolean("hasChecked");
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("hasChecked", hasChecked);
        return nbt;
    }

    /** @return Boolean.TRUE for true, Boolean.FALSE for false, and null for "this has already been resolved" */
    public Boolean resolve(IBuilderAccessor builder, BlockPos buildAt) {
        if (hasChecked) {
            return null;
        } else {
            hasChecked = true;
            return condition.resolve(builder, buildAt) ? Boolean.TRUE : Boolean.FALSE;
        }
    }
}
