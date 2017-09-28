/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.plug;

import java.io.IOException;
import java.util.Arrays;

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

import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IFlowFluid;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.info.ContextInfo;
import buildcraft.lib.expression.info.VariableInfo.CacheType;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoDouble;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoObject;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.BCTransportConfig;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.key.KeyPlugPulsar;

public class PluggablePulsar extends PipePluggable {

    public static final FunctionContext MODEL_FUNC_CTX;
    private static final NodeVariableObject<EnumFacing> MODEL_SIDE;
    private static final NodeVariableDouble MODEL_STAGE;
    private static final NodeVariableBoolean MODEL_ON;
    private static final NodeVariableBoolean MODEL_AUTO;
    private static final NodeVariableBoolean MODEL_MANUAL;
    public static final ContextInfo MODEL_VAR_INFO;

    private static final int PULSE_STAGE = 20;

    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];

    public final ModelVariableData clientModelData = new ModelVariableData();

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

        MODEL_FUNC_CTX = DefaultContexts.createWithAll();
        MODEL_SIDE = MODEL_FUNC_CTX.putVariableObject("side", EnumFacing.class);
        MODEL_STAGE = MODEL_FUNC_CTX.putVariableDouble("stage");
        MODEL_ON = MODEL_FUNC_CTX.putVariableBoolean("on");
        MODEL_AUTO = MODEL_FUNC_CTX.putVariableBoolean("auto");
        MODEL_MANUAL = MODEL_FUNC_CTX.putVariableBoolean("manual");

        MODEL_VAR_INFO = new ContextInfo(MODEL_FUNC_CTX);
        VariableInfoObject<EnumFacing> infoSide = MODEL_VAR_INFO.createInfoObject("side", MODEL_SIDE);
        infoSide.cacheType = CacheType.ALWAYS;
        infoSide.setIsComplete = true;
        infoSide.possibleValues.addAll(Arrays.asList(EnumFacing.VALUES));

        VariableInfoDouble infoStage = MODEL_VAR_INFO.createInfoDouble("stage", MODEL_STAGE);
        infoStage.cacheType = CacheType.IN_SET;
        infoStage.setIsComplete = false;
        infoStage.possibleValues.add(0);
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
        pulseStage = MathUtil.clamp(nbt.getInteger("pulseStage"), 0, PULSE_STAGE);
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setBoolean("manuallyEnabled", manuallyEnabled);
        nbt.setInteger("gateEnabledTicks", gateEnabledTicks);
        nbt.setInteger("gateSinglePulses", gateSinglePulses);
        nbt.setInteger("pulseStage", pulseStage);
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
        buffer.writeByte(pulseStage);
    }

    private void readData(PacketBuffer b) {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(b);
        isPulsing = buffer.readBoolean();
        autoEnabled = buffer.readBoolean();
        manuallyEnabled = buffer.readBoolean();
        pulseStage = buffer.readByte();
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
            setModelVariables(1);
            clientModelData.tick();
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
            if (gateSinglePulses > 0) {
                long power = MjAPI.MJ;
                if (holder.getPipe().getFlow() instanceof IFlowFluid) {
                    // Special extration logic for fluids:
                    // Always extract either 1 bucket, or nothing.
                    power = BCTransportConfig.mjPerMillibucket * 1000;
                } else if (holder.getPipe().getFlow() instanceof IFlowItems) {
                    power = BCTransportConfig.mjPerItem;
                } else {
                    power = MjAPI.MJ;
                }
                long excess = rsRec.receivePower(power, true);
                if (excess == 0) {
                    rsRec.receivePower(power, false);
                } else {
                    // Nothing was extracted, so lets extract in the future
                    gateSinglePulses++;
                    // ParticleUtil.spawnFailureParticles
                }
            } else {
                rsRec.receivePower(MjAPI.MJ, false);
            }
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
            SoundUtil.playLeverSwitch(holder.getPipeWorld(), holder.getPipePos(), manuallyEnabled);
            scheduleNetworkUpdate();
        }
        return true;
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

    // Model

    public static void setModelVariablesForItem() {
        MODEL_STAGE.value = 0;
        MODEL_AUTO.value = false;
        MODEL_MANUAL.value = false;
        MODEL_ON.value = false;
        MODEL_SIDE.value = EnumFacing.WEST;
    }

    public void setModelVariables(float partialTicks) {
        if (isPulsing) {
            MODEL_STAGE.value = (pulseStage + partialTicks) / 20 % 1;
        } else {
            MODEL_STAGE.value = 0;
        }
        MODEL_ON.value = isPulsing;
        MODEL_MANUAL.value = manuallyEnabled;
        MODEL_AUTO.value = autoEnabled;
        MODEL_SIDE.value = side;
    }
}
