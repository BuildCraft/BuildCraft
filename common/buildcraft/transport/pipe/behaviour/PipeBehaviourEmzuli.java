package buildcraft.transport.pipe.behaviour;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.transport.neptune.IPipe;

public class PipeBehaviourEmzuli extends PipeBehaviourWood {

    private final EnumDyeColor[] slotColours = new EnumDyeColor[4];

    public PipeBehaviourEmzuli(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourEmzuli(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        return nbt;
    }
}
