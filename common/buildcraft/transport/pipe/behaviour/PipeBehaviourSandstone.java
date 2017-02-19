package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventPower;

public class PipeBehaviourSandstone extends PipeBehaviour {

    public PipeBehaviourSandstone(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourSandstone(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeBehaviour other) {
        return true;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return false;
    }

    @PipeEventHandler
    public static void configurePower(PipeEventPower.Configure event) {
        event.setMaxPower(MjAPI.MJ * 8);
        event.setPowerResistance(MjAPI.MJ / 40);// 1/40th lost, or 0.025%
    }
}
