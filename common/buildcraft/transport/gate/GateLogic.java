/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerInternalSided;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeEventActionActivate;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;
import buildcraft.lib.statement.FullStatement.IStatementChangeListener;

import buildcraft.transport.gate.ActionWrapper.ActionWrapperExternal;
import buildcraft.transport.gate.ActionWrapper.ActionWrapperInternal;
import buildcraft.transport.gate.ActionWrapper.ActionWrapperInternalSided;
import buildcraft.transport.gate.TriggerWrapper.TriggerWrapperExternal;
import buildcraft.transport.gate.TriggerWrapper.TriggerWrapperInternal;
import buildcraft.transport.gate.TriggerWrapper.TriggerWrapperInternalSided;
import buildcraft.transport.plug.PluggableGate;
import buildcraft.transport.wire.IWireEmitter;
import buildcraft.transport.wire.WorldSavedDataWireSystems;

public class GateLogic implements IGate, IWireEmitter, IRedstoneStatementContainer {
    public static final int NET_ID_RESOLVE = 3;
    public static final int NET_ID_CHANGE = 4;

    /*
     * Ideally we wouldn't use a pluggable, but we would use a more generic way of looking at a gate -- perhaps one
     * that's embedded in a robot, or in a minecart.
     */
    @Deprecated
    public final PluggableGate pluggable;
    public final GateVariant variant;
    public final StatementPair[] statements;

    public final List<StatementSlot> activeActions = new ArrayList<>();

    /** Used to determine if gate logic should go across several trigger/action pairs. */
    public final boolean[] connections;

    /** Used at the client to display if an action is activated (or would be activated if its not null), or a trigger is
     * currently triggering. */
    public final boolean[] triggerOn, actionOn;

    public int redstoneOutput, redstoneOutputSide;

    private final EnumSet<EnumDyeColor> wireBroadcasts;

    /** Used on the client to determine if this gate should glow or not. */
    public boolean isOn;

    public GateLogic(PluggableGate pluggable, GateVariant variant) {
        this.pluggable = pluggable;
        this.variant = variant;
        statements = new StatementPair[variant.numSlots];
        for (int s = 0; s < variant.numSlots; s++) {
            statements[s] = new StatementPair(s);
        }

        connections = new boolean[variant.numSlots - 1];
        triggerOn = new boolean[variant.numSlots];
        actionOn = new boolean[variant.numSlots];

        wireBroadcasts = EnumSet.noneOf(EnumDyeColor.class);
    }

    // Saving + Loading

    public GateLogic(PluggableGate pluggable, NBTTagCompound nbt) {
        this(pluggable, new GateVariant(nbt.getCompoundTag("variant")));

        short c = nbt.getShort("connections");
        for (int i = 0; i < connections.length; i++) {
            connections[i] = ((c >>> i) & 1) == 1;
        }

        for (int i = 0; i < statements.length; i++) {
            String tName = "trigger[" + i + "]";
            String aName = "action[" + i + "]";
            // Legacy
            if (nbt.hasKey(tName, Constants.NBT.TAG_STRING)) {
                NBTTagCompound nbt2 = new NBTTagCompound();
                nbt2.setString("kind", nbt.getString(tName));
                nbt2.setByte("side", nbt.getByte(tName + ".side"));
                nbt.setTag(tName, nbt2);
            }
            // Legacy
            if (nbt.hasKey(aName, Constants.NBT.TAG_STRING)) {
                NBTTagCompound nbt2 = new NBTTagCompound();
                nbt2.setString("kind", nbt.getString(aName));
                nbt2.setByte("side", nbt.getByte(aName + ".side"));
                nbt.setTag(aName, nbt2);
            }

            statements[i].trigger.readFromNbt(nbt.getCompoundTag(tName));
            statements[i].action.readFromNbt(nbt.getCompoundTag(aName));
        }

        wireBroadcasts.addAll(NBTUtilBC.readEnumSet(nbt.getTag("wireBroadcasts"), EnumDyeColor.class));
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("variant", variant.writeToNBT());

        short c = 0;
        for (int i = 0; i < connections.length; i++) {
            if (connections[i]) {
                c |= 1 << i;
            }
        }
        nbt.setShort("connections", c);

        for (int s = 0; s < statements.length; s++) {
            nbt.setTag("trigger[" + s + "]", statements[s].trigger.writeToNbt());
            nbt.setTag("action[" + s + "]", statements[s].action.writeToNbt());
        }
        nbt.setTag("wireBroadcasts", NBTUtilBC.writeEnumSet(wireBroadcasts, EnumDyeColor.class));
        return nbt;
    }

    // Networking

    public GateLogic(PluggableGate pluggable, PacketBufferBC buffer) {
        this(pluggable, new GateVariant(buffer));

        MessageUtil.readBooleanArray(buffer, triggerOn);
        MessageUtil.readBooleanArray(buffer, actionOn);
        MessageUtil.readBooleanArray(buffer, connections);
        try {
            for (StatementPair pair : statements) {
                pair.trigger.readFromBuffer(buffer);
                pair.action.readFromBuffer(buffer);
            }
        } catch (IOException io) {
            throw new Error(io);
        }
        boolean on = false;
        for (int i = 0; i < statements.length; i++) {
            boolean b = actionOn[i];
            on |= b && (statements[i].action.get() != null);
        }
        isOn = on;

    }

    public void writeCreationToBuf(PacketBufferBC buffer) {
        variant.writeToBuffer(buffer);

        MessageUtil.writeBooleanArray(buffer, triggerOn);
        MessageUtil.writeBooleanArray(buffer, actionOn);
        MessageUtil.writeBooleanArray(buffer, connections);

        for (StatementPair pair : statements) {
            pair.trigger.writeToBuffer(buffer);
            pair.action.writeToBuffer(buffer);
        }
    }

    /** Helper method to send a custom payload to the other side via the pluggable. */
    public final void sendPayload(int id, IPayloadWriter writer) {
        pluggable.sendMessage(id, writer);
    }

    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        if (id == NET_ID_CHANGE) {
            boolean isAction = buffer.readBoolean();
            int slot = buffer.readUnsignedByte();
            if (slot < 0 || slot >= statements.length) {
                throw new InvalidInputDataException(
                    "Slot index out of range! (" + slot + ", must be within " + statements.length + ")");
            }
            StatementPair s = statements[slot];
            (isAction ? s.action : s.trigger).readFromBuffer(buffer);
            return;
        }
        if (side == Side.CLIENT) {
            if (id == NET_ID_RESOLVE) {
                MessageUtil.readBooleanArray(buffer, triggerOn);
                MessageUtil.readBooleanArray(buffer, actionOn);
                MessageUtil.readBooleanArray(buffer, connections);
                boolean on = false;
                for (int i = 0; i < statements.length; i++) {
                    boolean b = actionOn[i];
                    on |= b && (statements[i].action.get() != null);
                }
                isOn = on;
            } else {
                BCLog.logger.warn("Unknown ID " + id);
            }
        } else {
            BCLog.logger.warn("Unknown side + ID" + id);
        }
    }

    public void sendStatementUpdate(boolean isAction, int slot) {
        pluggable.sendMessage(NET_ID_CHANGE, (buffer) -> {
            buffer.writeBoolean(isAction);
            buffer.writeByte(slot);
            StatementPair s = statements[slot];
            (isAction ? s.action : s.trigger).writeToBuffer(buffer);
        });
    }

    public void sendResolveData() {
        pluggable.sendMessage(NET_ID_RESOLVE, (buffer) -> {
            MessageUtil.writeBooleanArray(buffer, triggerOn);
            MessageUtil.writeBooleanArray(buffer, actionOn);
            MessageUtil.writeBooleanArray(buffer, connections);
        });
    }

    // IGate

    @Override
    public EnumFacing getSide() {
        return pluggable.side;
    }

    @Override
    public TileEntity getTile() {
        return getPipeHolder().getPipeTile();
    }

    @Override
    public TileEntity getNeighbourTile(EnumFacing side) {
        return getPipeHolder().getNeighbourTile(side);
    }

    @Override
    public IPipeHolder getPipeHolder() {
        return pluggable.holder;
    }

    @Override
    public List<IStatement> getTriggers() {
        List<IStatement> list = new ArrayList<>(statements.length);
        for (StatementPair pair : statements) {
            TriggerWrapper e = pair.trigger.get();
            list.add(e == null ? e : e.delegate);
        }
        return list;
    }

    @Override
    public List<IStatement> getActions() {
        List<IStatement> list = new ArrayList<>(statements.length);
        for (StatementPair pair : statements) {
            ActionWrapper e = pair.action.get();
            list.add(e == null ? e : e.delegate);
        }
        return list;
    }

    @Override
    public List<StatementSlot> getActiveActions() {
        return activeActions;
    }

    @Override
    public List<IStatementParameter> getTriggerParameters(int slot) {
        return Arrays.asList(statements[slot].trigger.getParameters());
    }

    @Override
    public List<IStatementParameter> getActionParameters(int slot) {
        return Arrays.asList(statements[slot].action.getParameters());
    }

    @Override
    public int getRedstoneInput(EnumFacing side) {
        return getPipeHolder().getRedstoneInput(side);
    }

    @Override
    public boolean setRedstoneOutput(EnumFacing side, int value) {
        return getPipeHolder().setRedstoneOutput(side, value);
    }

    // Wire related

    @Override
    public boolean isEmitting(EnumDyeColor colour) {
        return wireBroadcasts.contains(colour);
    }

    @Override
    public void emitWire(EnumDyeColor colour) {
        wireBroadcasts.add(colour);
    }

    // Internal Logic

    /** @return True if the gate GUI should be split into 2 separate columns. Needed on the server for the values of
     *         {@link #connections} */
    public boolean isSplitInTwo() {
        return variant.numSlots > 4;
    }

    public void resolveActions() {
        int groupCount = 0;
        int groupActive = 0;

        boolean[] prevTriggers = Arrays.copyOf(triggerOn, triggerOn.length);
        boolean[] prevActions = Arrays.copyOf(actionOn, actionOn.length);

        Arrays.fill(triggerOn, false);
        Arrays.fill(actionOn, false);

        activeActions.clear();

        EnumSet<EnumDyeColor> previousBroadcasts = EnumSet.copyOf(wireBroadcasts);
        wireBroadcasts.clear();

        for (int triggerIndex = 0; triggerIndex < statements.length; triggerIndex++) {
            StatementPair pair = statements[triggerIndex];
            TriggerWrapper trigger = pair.trigger.get();
            groupCount++;
            if (trigger != null) {
                IStatementParameter[] params = new IStatementParameter[pair.trigger.getParamCount()];
                for (int p = 0; p < pair.trigger.getParamCount(); p++) {
                    params[p] = pair.trigger.getParamRef(p).get();
                }
                if (trigger.isTriggerActive(this, params)) {
                    groupActive++;
                    triggerOn[triggerIndex] = true;
                }
            }
            if (connections.length == triggerIndex || !connections[triggerIndex]) {
                boolean allActionsActive;
                if (variant.logic == EnumGateLogic.AND) {
                    allActionsActive = groupActive == groupCount;
                } else {
                    allActionsActive = groupActive > 0;
                }
                for (int i = groupCount - 1; i >= 0; i--) {
                    int actionIndex = triggerIndex - i;
                    StatementPair fullAction = statements[actionIndex];
                    ActionWrapper action = fullAction.action.get();
                    actionOn[actionIndex] = allActionsActive;
                    if (action != null) {
                        if (allActionsActive) {
                            StatementSlot slot = new StatementSlot();
                            slot.statement = action.delegate;
                            slot.parameters = fullAction.action.getParameters().clone();
                            slot.part = action.sourcePart;
                            activeActions.add(slot);
                            action.actionActivate(this, slot.parameters);
                            PipeEvent evt = new PipeEventActionActivate(getPipeHolder(), action.getDelegate(),
                                slot.parameters, action.sourcePart);
                            getPipeHolder().fireEvent(evt);
                        } else {
                            action.actionDeactivated(this, fullAction.action.getParameters());
                        }
                    }
                }
                groupActive = 0;
                groupCount = 0;
            }
        }

        if (!previousBroadcasts.equals(wireBroadcasts)) {
            IWireManager wires = getPipeHolder().getWireManager();
            EnumSet<EnumDyeColor> turnedOff = EnumSet.copyOf(previousBroadcasts);
            turnedOff.removeAll(wireBroadcasts);
            // FIXME: add call to "wires.stopEmittingColour(turnedOff)"

            EnumSet<EnumDyeColor> turnedOn = EnumSet.copyOf(wireBroadcasts);
            turnedOn.removeAll(previousBroadcasts);
            // FIXME: add call to "wires.emittingColour(turnedOff)"

            if (!getPipeHolder().getPipeWorld().isRemote) {
                WorldSavedDataWireSystems.get(getPipeHolder().getPipeWorld()).gatesChanged = true;
            }
        }

        if (!Arrays.equals(prevTriggers, triggerOn) || !Arrays.equals(prevActions, actionOn)) {
            sendResolveData();
        }
    }

    public void onTick() {
        if (getPipeHolder().getPipeWorld().isRemote) {
            return;
        }
        resolveActions();
    }

    public SortedSet<TriggerWrapper> getAllValidTriggers() {
        SortedSet<TriggerWrapper> set = new TreeSet<>();
        for (ITriggerInternal trigger : StatementManager.getInternalTriggers(this)) {
            if (isValidTrigger(trigger)) {
                set.add(new TriggerWrapperInternal(trigger));
            }
        }
        for (EnumFacing face : EnumFacing.VALUES) {
            for (ITriggerInternalSided trigger : StatementManager.getInternalSidedTriggers(this, face)) {
                if (isValidTrigger(trigger)) {
                    set.add(new TriggerWrapperInternalSided(trigger, face));
                }
            }
            TileEntity neighbour = getNeighbourTile(face);
            if (neighbour != null) {
                for (ITriggerExternal trigger : StatementManager.getExternalTriggers(face, neighbour)) {
                    if (isValidTrigger(trigger)) {
                        set.add(new TriggerWrapperExternal(trigger, face));
                    }
                }
            }
        }
        return set;
    }

    public SortedSet<ActionWrapper> getAllValidActions() {
        SortedSet<ActionWrapper> set = new TreeSet<>();
        for (IActionInternal trigger : StatementManager.getInternalActions(this)) {
            if (isValidAction(trigger)) {
                set.add(new ActionWrapperInternal(trigger));
            }
        }
        for (EnumFacing face : EnumFacing.VALUES) {
            for (IActionInternalSided trigger : StatementManager.getInternalSidedActions(this, face)) {
                if (isValidAction(trigger)) {
                    set.add(new ActionWrapperInternalSided(trigger, face));
                }
            }
            TileEntity neighbour = getNeighbourTile(face);
            if (neighbour != null) {
                for (IActionExternal trigger : StatementManager.getExternalActions(face, neighbour)) {
                    if (isValidAction(trigger)) {
                        set.add(new ActionWrapperExternal(trigger, face));
                    }
                }
            }
        }
        return set;
    }

    public boolean isValidTrigger(IStatement statement) {
        return statement != null && statement.minParameters() <= variant.numTriggerArgs;
    }

    public boolean isValidAction(IStatement statement) {
        return statement != null && statement.minParameters() <= variant.numActionArgs;
    }

    public class StatementPair {
        public final FullStatement<TriggerWrapper> trigger;
        public final FullStatement<ActionWrapper> action;

        public StatementPair(int index) {
            IStatementChangeListener tChange = (s, i) -> {
                sendStatementUpdate(false, index);
            };
            IStatementChangeListener aChange = (s, i) -> {
                sendStatementUpdate(true, index);
            };
            trigger = new FullStatement<>(TriggerType.INSTANCE, variant.numTriggerArgs, tChange);
            action = new FullStatement<>(ActionType.INSTANCE, variant.numActionArgs, aChange);
        }
    }
}
