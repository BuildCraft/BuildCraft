package buildcraft.transport.plug;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.key.KeyPlugPulsar;

public class PluggablePulsar extends PipePluggable {

    private static final int PULSE_STAGE = 20;

    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];

    private boolean manuallyEnabled = false;
    /** Increments from 0 to {@link #PULSE_STAGE} to decide when it should pulse some power into the pipe behaviour */
    private int pulseStage = 0;
    private int gateEnabledTicks;
    private int gateSinglePulses;
    private boolean lastPulsing = false;

    /** Used on the client to determine if this should render pulsing */
    private boolean isPulsing = false;
    /** Used on the client to determine if this is being activated by a gate */
    private boolean autoEnabled = false;

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 5 / 16.0;
        double max = 11 / 16.0;

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
        this.manuallyEnabled = nbt.getBoolean("manuallyEnabled");
        gateEnabledTicks = nbt.getInteger("gateEnabledTicks");
        gateSinglePulses = nbt.getInteger("gateSinglePulses");
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setBoolean("manuallyEnabled", manuallyEnabled);
        nbt.setInteger("gateEnabledTicks", gateEnabledTicks);
        nbt.setInteger("gateSinglePulses", gateSinglePulses);
        return nbt;
    }

    // Networking

    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, EnumFacing side, PacketBuffer buffer) {
        super(definition, holder, side);
        readData(buffer);
    }

    @Override
    public void writeCreationPayload(PacketBuffer buffer) {
        super.writeCreationPayload(buffer);
        writeData(buffer);
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == Side.CLIENT) {
            readData(buffer);
        }
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        if (side == Side.SERVER) {
            writeData(buffer);
        }
    }

    private void writeData(PacketBuffer b) {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(b);
        buffer.writeBoolean(isPulsing());
        buffer.writeBoolean(gateEnabledTicks > 0 || gateSinglePulses > 0);
        buffer.writeBoolean(manuallyEnabled);
    }

    private void readData(PacketBuffer b) {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(b);
        isPulsing = buffer.readBoolean();
        autoEnabled = buffer.readBoolean();
        manuallyEnabled = buffer.readBoolean();
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
    public void onRemove(NonNullList<ItemStack> toDrop) {
        toDrop.add(new ItemStack(BCTransportItems.plugPulsar));
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCTransportItems.plugPulsar);
    }

    @Override
    public void onTick() {
        if (holder.getPipeWorld().isRemote) {
            if (isPulsing) {
                pulseStage++;
                if (pulseStage == PULSE_STAGE) {
                    pulseStage = 0;
                }
            } else {
                // pulseStage--;
                // if (pulseStage < 0) {
                pulseStage = 0;
                // }
            }
            return;
        }
        boolean isOn = isPulsing();

        if (isOn) {
            pulseStage++;
        } else {
            // pulseStage--;
            // if (pulseStage < 0) {
            pulseStage = 0;
            // }
        }
        if (gateEnabledTicks > 0) {
            gateEnabledTicks--;
        }
        if (pulseStage == PULSE_STAGE) {
            pulseStage = 0;
            IMjRedstoneReceiver rsRec = (IMjRedstoneReceiver) holder.getPipe().getBehaviour();
            rsRec.receivePower(MjAPI.MJ, false);
            if (gateSinglePulses > 0) {
                gateSinglePulses--;
            }
        }
        if (isOn != lastPulsing) {
            lastPulsing = isOn;
            scheduleNetworkUpdate();
        }
    }

    @Override
    public boolean onPluggableActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ) {
        if (!holder.getPipeWorld().isRemote) {
            manuallyEnabled = !manuallyEnabled;
            scheduleNetworkUpdate();
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    public double getStage(float partialTicks) {
        if (isPulsingClient()) {
            return (pulseStage + partialTicks) / 20 % 1;
        } else {
            return 0;
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isPulsingClient() {
        return isPulsing;
    }

    @SideOnly(Side.CLIENT)
    public boolean isAutoEnabledClient() {
        return autoEnabled;
    }

    @SideOnly(Side.CLIENT)
    public boolean isManuallyEnabledClient() {
        return manuallyEnabled;
    }

    @Override
    public PluggableModelKey getModelRenderKey(BlockRenderLayer layer) {
        if (layer == BlockRenderLayer.CUTOUT) return new KeyPlugPulsar(side);
        return null;
    }

    public void enablePulsar() {
        gateEnabledTicks = 10;
    }

    public void addSinglePulse() {
        gateSinglePulses++;
    }

    private boolean isPulsing() {
        return manuallyEnabled || gateEnabledTicks > 0 || gateSinglePulses > 0;
    }
}
