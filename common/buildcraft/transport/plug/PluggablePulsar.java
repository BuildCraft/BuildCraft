package buildcraft.transport.plug;

import java.io.IOException;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.transport.BCTransportItems;
import buildcraft.transport.api_move.IPipeHolder;
import buildcraft.transport.api_move.PipePluggable;
import buildcraft.transport.api_move.PluggableDefinition;
import buildcraft.transport.client.model.key.KeyPlugBlocker;

public class PluggablePulsar extends PipePluggable {

    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];

    public boolean isPulsing = false;
    /** Increments from 0 to 20 to decide when it should pulse some power into the pipe behaviour */
    private int pulseStage = 0;

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 4 / 16.0;
        double max = 12 / 16.0;

        BOXES[EnumFacing.DOWN.ordinal()] = new AxisAlignedBB(min, ll, min, max, lu, max);
        BOXES[EnumFacing.UP.ordinal()] = new AxisAlignedBB(min, ul, min, max, uu, max);
        BOXES[EnumFacing.NORTH.ordinal()] = new AxisAlignedBB(min, min, ll, max, max, lu);
        BOXES[EnumFacing.SOUTH.ordinal()] = new AxisAlignedBB(min, min, ul, max, max, uu);
        BOXES[EnumFacing.WEST.ordinal()] = new AxisAlignedBB(ll, min, min, lu, max, max);
        BOXES[EnumFacing.EAST.ordinal()] = new AxisAlignedBB(ul, min, min, uu, max, max);
    }

    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, EnumFacing side) {
        super(definition, holder, side);
    }

    // Saving + Loading

    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, EnumFacing side, NBTTagCompound nbt) {
        super(definition, holder, side);
        isPulsing = nbt.getBoolean("isPulsing");
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setBoolean("isPulsing", isPulsing);
        return nbt;
    }

    // Networking

    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, EnumFacing side, PacketBuffer buffer) {
        super(definition, holder, side);
        isPulsing = buffer.readBoolean();
    }

    @Override
    public void writeCreationPayload(PacketBuffer buffer) {
        super.writeCreationPayload(buffer);
        buffer.writeBoolean(isPulsing);
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == Side.CLIENT) {
            isPulsing = buffer.readBoolean();
        }
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        if (side == Side.SERVER) {
            buffer.writeBoolean(isPulsing);
        }
    }

    // PipePluggable

    @Override
    public AxisAlignedBB getBoundingBox() {
        return BOXES[side.ordinal()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public void onRemove(List<ItemStack> toDrop) {
        toDrop.add(new ItemStack(BCTransportItems.plugPulsar));
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCTransportItems.plugPulsar);
    }

    @Override
    public void onTick() {
        if (isPulsing | true) {
            pulseStage++;
        } else {
            pulseStage--;
            if (pulseStage < 0) {
                pulseStage = 0;
            }
        }
        if (holder.getPipeWorld().isRemote) {
            return;
        }
        if (pulseStage == 20) {
            pulseStage = 0;
            IMjRedstoneReceiver rsRec = (IMjRedstoneReceiver) holder.getPipe().getBehaviour();
            rsRec.receivePower(MjAPI.MJ, false);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public PluggableModelKey<?> getModelRenderKey(BlockRenderLayer layer) {
        if (layer == BlockRenderLayer.CUTOUT) return new KeyPlugBlocker(side);
        return null;
    }
}
