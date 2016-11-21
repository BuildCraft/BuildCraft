package buildcraft.transport.plug;

import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.neptune.IPipeHolder;
import buildcraft.api.transport.neptune.IPluggableDynamicRenderer;
import buildcraft.api.transport.neptune.PipePluggable;
import buildcraft.api.transport.neptune.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.key.KeyPlugPulsar;
import buildcraft.transport.client.render.PlugPulsarRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class PluggablePulsar extends PipePluggable {

    private static final int PULSE_STAGE = 20;

    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];

    private boolean isPulsing = false;
    /** Increments from 0 to 20 to decide when it should pulse some power into the pipe behaviour */
    private int pulseStage = 0;
    private int gateEnabledTicks;
    private int gateSinglePulses;

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
        this.isPulsing = nbt.getBoolean("isPulsing");
        gateEnabledTicks = nbt.getInteger("gateEnabledTicks");
        gateSinglePulses = nbt.getInteger("gateSinglePulses");
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setBoolean("isPulsing", isPulsing);
        nbt.setInteger("gateEnabledTicks", (byte) gateEnabledTicks);
        nbt.setInteger("gateSinglePulses", gateSinglePulses);
        return nbt;
    }

    // Networking

    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, EnumFacing side, PacketBuffer buffer) {
        super(definition, holder, side);
        this.isPulsing = buffer.readBoolean();
        gateEnabledTicks = buffer.readInt();
        gateSinglePulses = buffer.readInt();
    }

    @Override
    public void writeCreationPayload(PacketBuffer buffer) {
        super.writeCreationPayload(buffer);
        buffer.writeBoolean(isPulsing);
        buffer.writeInt(gateEnabledTicks);
        buffer.writeInt(gateSinglePulses);
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == Side.CLIENT) {
            this.isPulsing = buffer.readBoolean();
            gateEnabledTicks = buffer.readInt();
            gateSinglePulses = buffer.readInt();
        }
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        if (side == Side.SERVER) {
            buffer.writeBoolean(isPulsing);
            buffer.writeInt(gateEnabledTicks);
            buffer.writeInt(gateSinglePulses);
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
        if (isPulsing()) {
            pulseStage++;
        } else {
            pulseStage--;
            if (pulseStage < 0) {
                pulseStage = 0;
            }
        }
        if (gateEnabledTicks > 0) {
            gateEnabledTicks--;
        }
        if (holder.getPipeWorld().isRemote) {
            if (pulseStage == PULSE_STAGE) {
                pulseStage = 0;
                if (gateSinglePulses > 0) {
                    gateSinglePulses--;
                }
            }
            return;
        }
        if (pulseStage == PULSE_STAGE) {
            pulseStage = 0;
            IMjRedstoneReceiver rsRec = (IMjRedstoneReceiver) holder.getPipe().getBehaviour();
            rsRec.receivePower(MjAPI.MJ, false);
            if (gateSinglePulses > 0) {
                gateSinglePulses--;
            }
        }
    }

    @Override
    public boolean onPluggableActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ) {
        if (!holder.getPipeWorld().isRemote) {
            this.isPulsing = !isPulsing;
            scheduleNetworkUpdate();
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    public double getStage(float partialTicks) {
        if (isPulsing()) {
            double v = (pulseStage + partialTicks) / 20.0;
            Function<Double, Double> easing = x -> x < 0.5 ?
                    4 * x * x * x :
                    1 - (2 - 2 * x) * (2 - 2 * x) * (2 - 2 * x) * (2 - 2 * x) / 2;
            if(v < 0.5) {
                v = easing.apply(v * 2);
            } else {
                v = 1 - easing.apply(v * 2 - 1);
            }
            v += 0.001;
            return v;
        } else {
            return 0;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public PluggableModelKey<?> getModelRenderKey(BlockRenderLayer layer) {
        if (layer == BlockRenderLayer.CUTOUT) return new KeyPlugPulsar(side);
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IPluggableDynamicRenderer getDynamicRenderer() {
        return new PlugPulsarRenderer(this);
    }

    public void enablePulsar() {
        gateEnabledTicks = 10;
        scheduleNetworkUpdate();
    }

    public void addSinglePulse() {
        gateSinglePulses++;
        scheduleNetworkUpdate();
    }

    public boolean isPulsing() {
        return isPulsing || gateEnabledTicks > 0 || gateSinglePulses > 0;
    }
}
