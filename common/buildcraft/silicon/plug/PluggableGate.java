/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.plug;

import java.io.IOException;

import net.fabricmc.api.EnvType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Box;

import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;

import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.silicon.gate.GateLogic;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.transport.pipe.PluggableHolder;

/**
 * Pipe pluggable for logic gates (migrated stub).
 *
 * STUB(R.Chen): Expression model system (FunctionContext, ContextInfo, ModelVariableData,
 * NodeVariable*, BooleanPossibilities) is not in libLeaf — all model code removed.
 * STUB(R.Chen): BCSiliconItems, BCSiliconGuis, ItemGateCopier, AdvancementUtil deferred.
 * STUB(R.Chen): Box → Box (net.minecraft.util.math.Box); direction-specific BOXES array
 * deferred until BlockEntity/rendering phase.
 */
public class PluggableGate extends PipePluggable implements IWireEmitter {

    public final GateLogic logic;

    // Manual constructor (called by the specific item pluggable gate code)
    public PluggableGate(PluggableDefinition def, IPipeHolder holder, net.minecraft.util.math.Direction side,
                         GateVariant variant) {
        super(def, holder, side);
        logic = new GateLogic(this, variant);
    }

    // Saving + Loading
    public PluggableGate(PluggableDefinition def, IPipeHolder holder, net.minecraft.util.math.Direction side,
                         NbtCompound nbt) {
        super(def, holder, side);
        logic = new GateLogic(this, nbt.getCompound("data"));
    }

    @Override
    public NbtCompound writeToNbt() {
        NbtCompound nbt = super.writeToNbt();
        nbt.put("data", logic.writeToNbt());
        return nbt;
    }

    // Networking
    public PluggableGate(PluggableDefinition def, IPipeHolder holder, net.minecraft.util.math.Direction side,
                         PacketByteBuf buffer) {
        super(def, holder, side);
        logic = new GateLogic(this, PacketBufferBC.asPacketBufferBc(buffer));
    }

    @Override
    public void writeCreationPayload(PacketByteBuf buffer) {
        logic.writeCreationToBuf(PacketBufferBC.asPacketBufferBc(buffer));
    }

    public void sendMessage(IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendMessage(to, (buffer) -> {
            /* The pluggable holder receives this message and requires the ID '1' (UPDATE) to forward the message onto
             * ourselves */
            buffer.writeByte(PluggableHolder.ID_UPDATE_PLUG);
            writer.write(PacketBufferBC.asPacketBufferBc(buffer));
        });
    }

    public void sendGuiMessage(IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendGuiMessage(to, (buffer) -> {
            /* The pluggable holder receives this message and requires the ID '1' (UPDATE) to forward the message onto
             * ourselves */
            buffer.writeByte(PluggableHolder.ID_UPDATE_PLUG);
            writer.write(PacketBufferBC.asPacketBufferBc(buffer));
        });
    }

    @Override
    public void writePayload(PacketByteBuf buffer, EnvType side) {
        throw new Error("All messages must have an ID, and we can't just write a payload directly!");
    }

    @Override
    public void readPayload(PacketByteBuf b, EnvType side, Object ctx) throws IOException {
        logic.readPayload(PacketBufferBC.asPacketBufferBc(b), side, ctx);
    }

    // PipePluggable

    @Override
    public Box getBoundingBox() {
        // STUB(R.Chen): Direction-specific bounding boxes deferred. Generic centred gate box.
        double min = 5 / 16.0;
        double max = 11 / 16.0;
        double ll  = 2 / 16.0;
        double uu  = 14 / 16.0;
        return new Box(min, ll, min, max, uu, max);
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    // IWireEmitter

    @Override
    public boolean isEmitting(DyeColor colour) {
        return logic.isEmitting(colour);
    }

    @Override
    public void emitWire(DyeColor colour) {
        logic.emitWire(colour);
    }

    // Gate tick

    @Override
    public void onTick() {
        logic.onTick();
        // STUB(R.Chen): clientModelData.tick() deferred until model system is migrated.
    }
}
