package buildcraft.transport.api_move;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public abstract class PipeBehaviour implements ICapabilityProvider {
    public final IPipe pipe;

    public PipeBehaviour(IPipe pipe) {
        this.pipe = pipe;
    }

    public PipeBehaviour(IPipe pipe, NBTTagCompound nbt) {
        this.pipe = pipe;
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();

        return nbt;
    }

    public void writePayload(PacketBuffer buffer, Side side) {}

    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) {}

    public abstract int getTextureIndex(EnumFacing face);

    // Event handling

    public void configureFlow(PipeFlow flow) {}

    public abstract boolean canConnect(EnumFacing face, PipeBehaviour other);

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
