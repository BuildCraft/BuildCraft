package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

public class PipeBehaviourWoodPower extends PipeBehaviour {

    public PipeBehaviourWoodPower(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWoodPower(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWoodPower);
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        if (face == null) {
            return 0;
        }
        if (pipe.getConnectedPipe(face) != null) {
            return 0;
        }
        TileEntity tile = pipe.getConnectedTile(face);
        if (tile == null) {
            return 0;
        }
        IMjReceiver recv = tile.getCapability(MjAPI.CAP_RECEIVER, face.getOpposite());
        return recv == null ? 1 : recv.canReceive() ? 0 : 1;
    }
}
