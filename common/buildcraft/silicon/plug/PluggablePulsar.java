/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import buildcraft.api.BCModules;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.*;
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
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.BCSiliconStatements;
import buildcraft.silicon.client.model.key.KeyPlugPulsar;
import buildcraft.transport.BCTransportConfig;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.util.Arrays;

public class PluggablePulsar extends PipePluggable {

    public static final FunctionContext MODEL_FUNC_CTX;
    private static final NodeVariableObject<Direction> MODEL_SIDE;
    private static final NodeVariableDouble MODEL_STAGE;
    private static final NodeVariableBoolean MODEL_ON;
    private static final NodeVariableBoolean MODEL_AUTO;
    private static final NodeVariableBoolean MODEL_MANUAL;
    public static final ContextInfo MODEL_VAR_INFO;

    private static final int PULSE_STAGE = 20;

    private static final VoxelShape[] BOXES = new VoxelShape[6];

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

        BOXES[Direction.DOWN.ordinal()] = Shapes.box(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()] = Shapes.box(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = Shapes.box(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = Shapes.box(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()] = Shapes.box(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()] = Shapes.box(ul, min, min, uu, max, max);

        MODEL_FUNC_CTX = DefaultContexts.createWithAll();
        MODEL_SIDE = MODEL_FUNC_CTX.putVariableObject("side", Direction.class);
        MODEL_STAGE = MODEL_FUNC_CTX.putVariableDouble("stage");
        MODEL_ON = MODEL_FUNC_CTX.putVariableBoolean("on");
        MODEL_AUTO = MODEL_FUNC_CTX.putVariableBoolean("auto");
        MODEL_MANUAL = MODEL_FUNC_CTX.putVariableBoolean("manual");

        MODEL_VAR_INFO = new ContextInfo(MODEL_FUNC_CTX);
        VariableInfoObject<Direction> infoSide = MODEL_VAR_INFO.createInfoObject(MODEL_SIDE);
        infoSide.cacheType = CacheType.ALWAYS;
        infoSide.setIsComplete = true;
//        infoSide.possibleValues.addAll(Arrays.asList(Direction.VALUES));
        infoSide.possibleValues.addAll(Arrays.asList(Direction.VALUES.clone()));

        VariableInfoDouble infoStage = MODEL_VAR_INFO.createInfoDouble(MODEL_STAGE);
        infoStage.cacheType = CacheType.IN_SET;
        infoStage.setIsComplete = false;
        infoStage.possibleValues.add(0.0);
    }

    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    // Saving + Loading

    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, Direction side, CompoundTag nbt) {
        super(definition, holder, side);
        this.manuallyEnabled = nbt.getBoolean("manuallyEnabled");
        gateEnabledTicks = nbt.getInt("gateEnabledTicks");
        gateSinglePulses = nbt.getInt("gateSinglePulses");
        pulseStage = MathUtil.clamp(nbt.getInt("pulseStage"), 0, PULSE_STAGE);
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.putBoolean("manuallyEnabled", manuallyEnabled);
        nbt.putInt("gateEnabledTicks", gateEnabledTicks);
        nbt.putInt("gateSinglePulses", gateSinglePulses);
        nbt.putInt("pulseStage", pulseStage);
        return nbt;
    }

    // Networking

    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, Direction side, FriendlyByteBuf buffer) {
        super(definition, holder, side);
        readData(buffer);
    }

    @Override
    public void writeCreationPayload(FriendlyByteBuf buffer) {
        super.writeCreationPayload(buffer);
        writeData(buffer);
    }

    @Override
//    public void readPayload(PacketBuffer buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(FriendlyByteBuf buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            readData(buffer);
        }
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, Dist side) {
        super.writePayload(buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            writeData(buffer);
        }
    }

    private void writeData(FriendlyByteBuf b) {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(b);
        buffer.writeBoolean(isPulsing());
        buffer.writeBoolean(gateEnabledTicks > 0 || gateSinglePulses > 0);
        buffer.writeBoolean(manuallyEnabled);
        buffer.writeByte(pulseStage);
    }

    private void readData(FriendlyByteBuf b) {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(b);
        isPulsing = buffer.readBoolean();
        autoEnabled = buffer.readBoolean();
        manuallyEnabled = buffer.readBoolean();
        pulseStage = buffer.readByte();
    }

    // PipePluggable

    @Override
    public VoxelShape getBoundingBox() {
        return BOXES[side.ordinal()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCSiliconItems.plugPulsar.get());
    }

    @Override
    public void onTick() {
        if (holder.getPipeWorld().isClientSide) {
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
                if (BCModules.TRANSPORT.isLoaded()) {
                    if (holder.getPipe().getFlow() instanceof IFlowFluid) {
                        power = BCTransportConfig.mjPerMillibucket * 1000;
                    } else if (holder.getPipe().getFlow() instanceof IFlowItems) {
                        power = BCTransportConfig.mjPerItem;
                    }
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

    @PipeEventHandler
    public void onAddActions(PipeEventStatement.AddActionInternalSided event) {
        if (event.side == this.side) {
            event.actions.add(BCSiliconStatements.ACTION_PULSAR_CONSTANT);
            event.actions.add(BCSiliconStatements.ACTION_PULSAR_SINGLE);
        }
    }

    @Override
    public boolean onPluggableActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ) {
        if (!holder.getPipeWorld().isClientSide) {
            manuallyEnabled = !manuallyEnabled;
            SoundUtil.playLeverSwitch(holder.getPipeWorld(), holder.getPipePos(), manuallyEnabled);
            scheduleNetworkUpdate();
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public PluggableModelKey getModelRenderKey(RenderType layer) {
        if (layer == RenderType.cutout()) return new KeyPlugPulsar(side);
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
        MODEL_SIDE.value = Direction.WEST;
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
