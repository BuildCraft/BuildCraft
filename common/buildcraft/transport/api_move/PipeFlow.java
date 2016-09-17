package buildcraft.transport.api_move;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public abstract class PipeFlow implements ICapabilityProvider {
    public final IPipe pipe;

    public PipeFlow(IPipe pipe) {
        this.pipe = pipe;
    }

    public PipeFlow(IPipe pipe, NBTTagCompound nbt) {
        this.pipe = pipe;
    }

    public NBTTagCompound writeToNbt() {
        return new NBTTagCompound();
    }

    public abstract boolean canConnect(EnumFacing face, PipeFlow other);

    public abstract boolean canConnect(EnumFacing face, TileEntity oTile);

    public void onTick() {

    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return null;
    }
}
