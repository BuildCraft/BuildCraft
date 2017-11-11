/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.plug;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.info.ContextInfo;
import buildcraft.lib.expression.info.VariableInfo.CacheType;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoBoolean;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoBoolean.BooleanPossibilities;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoObject;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.BCTransportGuis;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.key.KeyPlugGate;
import buildcraft.transport.gate.EnumGateLogic;
import buildcraft.transport.gate.EnumGateMaterial;
import buildcraft.transport.gate.EnumGateModifier;
import buildcraft.transport.gate.GateLogic;
import buildcraft.transport.gate.GateVariant;

public class PluggableGate extends PipePluggable {
    public static final FunctionContext MODEL_FUNC_CTX_STATIC, MODEL_FUNC_CTX_DYNAMIC;
    private static final NodeVariableObject<String> MODEL_MATERIAL;
    private static final NodeVariableObject<String> MODEL_MODIFIER;
    private static final NodeVariableObject<String> MODEL_LOGIC;
    private static final NodeVariableObject<EnumFacing> MODEL_SIDE;
    private static final NodeVariableBoolean MODEL_IS_ON;
    public static final ContextInfo MODEL_VAR_INFO;

    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];

    public final GateLogic logic;

    public final ModelVariableData clientModelData = new ModelVariableData();

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 5 / 16.0;
        double max = 11 / 16.0;

        BOXES[EnumFacing.DOWN.getIndex()] = new AxisAlignedBB(min, ll, min, max, lu, max);
        BOXES[EnumFacing.UP.getIndex()] = new AxisAlignedBB(min, ul, min, max, uu, max);
        BOXES[EnumFacing.NORTH.getIndex()] = new AxisAlignedBB(min, min, ll, max, max, lu);
        BOXES[EnumFacing.SOUTH.getIndex()] = new AxisAlignedBB(min, min, ul, max, max, uu);
        BOXES[EnumFacing.WEST.getIndex()] = new AxisAlignedBB(ll, min, min, lu, max, max);
        BOXES[EnumFacing.EAST.getIndex()] = new AxisAlignedBB(ul, min, min, uu, max, max);

        MODEL_FUNC_CTX_STATIC = DefaultContexts.createWithAll();
        MODEL_MATERIAL = MODEL_FUNC_CTX_STATIC.putVariableString("material");
        MODEL_MODIFIER = MODEL_FUNC_CTX_STATIC.putVariableString("modifier");
        MODEL_LOGIC = MODEL_FUNC_CTX_STATIC.putVariableString("logic");
        MODEL_SIDE = MODEL_FUNC_CTX_STATIC.putVariableObject("side", EnumFacing.class);

        MODEL_FUNC_CTX_DYNAMIC = new FunctionContext(MODEL_FUNC_CTX_STATIC);
        MODEL_IS_ON = MODEL_FUNC_CTX_DYNAMIC.putVariableBoolean("on");

        MODEL_VAR_INFO = new ContextInfo(MODEL_FUNC_CTX_DYNAMIC);
        VariableInfoObject<String> infoMaterial = MODEL_VAR_INFO.createInfoObject("material", MODEL_MATERIAL);
        infoMaterial.cacheType = CacheType.ALWAYS;
        infoMaterial.setIsComplete = true;
        infoMaterial.possibleValues.addAll(Arrays.stream(EnumGateMaterial.VALUES).map(m -> m.tag).collect(Collectors.toList()));

        VariableInfoObject<String> infoModifier = MODEL_VAR_INFO.createInfoObject("modifier", MODEL_MODIFIER);
        infoModifier.cacheType = CacheType.ALWAYS;
        infoModifier.setIsComplete = true;
        infoModifier.possibleValues.addAll(Arrays.stream(EnumGateModifier.VALUES).map(m -> m.tag).collect(Collectors.toList()));

        VariableInfoObject<String> infoLogic = MODEL_VAR_INFO.createInfoObject("logic", MODEL_LOGIC);
        infoLogic.cacheType = CacheType.ALWAYS;
        infoLogic.setIsComplete = true;
        infoLogic.possibleValues.addAll(Arrays.stream(EnumGateLogic.VALUES).map(m -> m.tag).collect(Collectors.toList()));

        VariableInfoObject<EnumFacing> infoSide = MODEL_VAR_INFO.createInfoObject("side", MODEL_SIDE);
        infoSide.cacheType = CacheType.ALWAYS;
        infoSide.setIsComplete = true;
        Collections.addAll(infoSide.possibleValues, EnumFacing.VALUES);

        VariableInfoBoolean infoIsOn = MODEL_VAR_INFO.createInfoBoolean("on", MODEL_IS_ON);
        infoIsOn.cacheType = CacheType.ALWAYS;
        infoIsOn.setIsComplete = true;
        infoIsOn.possibleValues = BooleanPossibilities.FALSE_TRUE;
    }

    // Manual constructor (called by the specific item pluggable gate code)

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, EnumFacing side, GateVariant variant) {
        super(def, holder, side);
        logic = new GateLogic(this, variant);
    }

    // Saving + Loading

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, EnumFacing side, NBTTagCompound nbt) {
        super(def, holder, side);
        logic = new GateLogic(this, nbt.getCompoundTag("data"));
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("data", logic.writeToNbt());
        return nbt;
    }

    // Networking

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, EnumFacing side, PacketBuffer buffer) {
        super(def, holder, side);
        logic = new GateLogic(this, PacketBufferBC.asPacketBufferBc(buffer));
    }

    @Override
    public void writeCreationPayload(PacketBuffer buffer) {
        logic.writeCreationToBuf(PacketBufferBC.asPacketBufferBc(buffer));
    }

    public void sendMessage(int id, IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendMessage(to, (buffer) -> {
            /* The pluggable holder receives this message and requires the ID '1' (UPDATE) to forward the message onto
             * ourselves */
            buffer.writeByte(1);
            buffer.writeByte(id);
            writer.write(PacketBufferBC.asPacketBufferBc(buffer));
        });
    }

    public void sendGuiMessage(int id, IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendGuiMessage(to, (buffer) -> {
            /* The pluggable holder receives this message and requires the ID '1' (UPDATE) to forward the message onto
             * ourselves */
            buffer.writeByte(1);
            buffer.writeByte(id);
            writer.write(PacketBufferBC.asPacketBufferBc(buffer));
        });
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        // What???
        buffer.writeByte(0);
    }

    @Override
    public void readPayload(PacketBuffer b, Side side, MessageContext ctx) throws IOException {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(b);
        int id = buffer.readUnsignedByte();
        logic.readPayload(id, buffer, side, ctx);
    }

    // PipePluggable

    @Override
    public AxisAlignedBB getBoundingBox() {
        return BOXES[side.getIndex()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return BCTransportItems.plugGate.getStack(logic.variant);
    }

    @Override
    public PluggableModelKey getModelRenderKey(BlockRenderLayer layer) {
        if (layer == BlockRenderLayer.CUTOUT) {
            return new KeyPlugGate(side, logic.variant);
        }
        return null;
    }

    @Override
    public boolean onPluggableActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ) {
        if (!player.world.isRemote) {
            BlockPos pos = holder.getPipePos();
            int x = pos.getX();
            int y = pos.getY() << 3 | side.ordinal();
            int z = pos.getZ();
            BCTransportGuis.GATE.openGui(player, x, y, z);
        }
        return true;
    }

    // Gate methods

    @Override
    public void onTick() {
        logic.onTick();
        if (holder.getPipeWorld().isRemote) {
            clientModelData.tick();
        }
    }

    @Override
    public boolean canConnectToRedstone(@Nullable EnumFacing to) {
        return true;
    }

    // Model

    public static void setClientModelVariables(EnumFacing side, GateVariant variant) {
        MODEL_SIDE.value = side;
        MODEL_MATERIAL.value = variant.material.tag;
        MODEL_MODIFIER.value = variant.modifier.tag;
        MODEL_LOGIC.value = variant.logic.tag;
        MODEL_IS_ON.value = false;// Used by the item
    }

    public void setClientModelVariables() {
        setClientModelVariables(side, logic.variant);
        MODEL_IS_ON.value = logic.isOn;
    }
}
