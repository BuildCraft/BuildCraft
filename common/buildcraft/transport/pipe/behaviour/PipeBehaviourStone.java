package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.neptune.IPipe;

public class PipeBehaviourStone extends PipeBehaviourSeperate {
    public PipeBehaviourStone(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourStone(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return 0;
    }
}
