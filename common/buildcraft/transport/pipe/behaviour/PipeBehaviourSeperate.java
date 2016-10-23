package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.PipeBehaviour;

public abstract class PipeBehaviourSeperate extends PipeBehaviour {
    public PipeBehaviourSeperate(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourSeperate(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeBehaviour other) {
        if (other instanceof PipeBehaviourSeperate) {
            return other.getClass() == getClass();
        } else {
            return true;
        }
    }
}
