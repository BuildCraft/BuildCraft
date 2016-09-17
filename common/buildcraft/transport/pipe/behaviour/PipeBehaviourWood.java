package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.IPipe.ConnectedType;
import buildcraft.transport.api_move.PipeBehaviour;

public class PipeBehaviourWood extends PipeBehaviour {
    public PipeBehaviourWood(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWood(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return pipe.getConnectedType(face) == ConnectedType.TILE ? 1 : 0;
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWood);
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return true;
    }
}
