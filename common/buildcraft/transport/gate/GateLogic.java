/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gate;

import java.util.*;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.*;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEventActionActivate;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.IPayloadWriter;
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

    public final PluggableGate pluggable;
    public final GateVariant variant;

    public final TriggerWrapper[] triggers;
    public final IStatementParameter[][] triggerParameters;

    public final ActionWrapper[] actions;
    public final IStatementParameter[][] actionParameters;

    public final List<StatementSlot> activeActions = new ArrayList<>();

    /** Used to determine if gate logic should go across several trigger/action pairs. */
    public final boolean[] connections;

    public final boolean[] triggerOn, actionOn;

    public int redstoneOutput, redstoneOutputSide;

    private final EnumSet<EnumDyeColor> wireBroadcasts;

    /** Used on the client to determine if this gate should glow or not. */
    public boolean isOn;

    public GateLogic(PluggableGate pluggable, GateVariant variant) {
        this.pluggable = pluggable;
        this.variant = variant;
        triggers = new TriggerWrapper[variant.numSlots];
        triggerParameters = new IStatementParameter[variant.numSlots][variant.numTriggerArgs];

        actions = new ActionWrapper[variant.numSlots];
        actionParameters = new IStatementParameter[variant.numSlots][variant.numActionArgs];

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

        for (int i = 0; i < triggers.length; i++) {
            String tag = nbt.getString("trigger[" + i + "]");
            EnumPipePart part = EnumPipePart.fromMeta(nbt.getByte("trigger[" + i + "].side"));
            TriggerWrapper wrapper = TriggerWrapper.wrap(StatementManager.statements.get(tag), part.face);
            triggers[i] = wrapper;

            if (wrapper != null) {
                for (int j = 0; j < triggerParameters[i].length; j++) {
                    NBTTagCompound cpt = nbt.getCompoundTag("triggerParameters[" + i + "][" + j + "]");
                    if (cpt.hasNoTags()) {
                        triggerParameters[i][j] = wrapper.createParameter(j);
                        continue;
                    }
                    tag = cpt.getString("kind");
                    IStatementParameter param = StatementManager.createParameter(tag);
                    if (param != null) {
                        param.readFromNBT(cpt);
                        triggerParameters[i][j] = param;
                    } else {
                        BCLog.logger.warn("Didn't find an IStatementParamater for " + tag);
                    }
                }
            }
        }

        for (int i = 0; i < actions.length; i++) {
            String tag = nbt.getString("action[" + i + "]");
            EnumPipePart part = EnumPipePart.fromMeta(nbt.getByte("action[" + i + "].side"));
            ActionWrapper wrapper = ActionWrapper.wrap(StatementManager.statements.get(tag), part.face);
            actions[i] = wrapper;

            if (wrapper != null) {
                for (int j = 0; j < actionParameters[i].length; j++) {
                    NBTTagCompound cpt = nbt.getCompoundTag("actionParameters[" + i + "][" + j + "]");
                    if (cpt.hasNoTags()) {
                        actionParameters[i][j] = wrapper.createParameter(j);
                        continue;
                    }
                    tag = cpt.getString("kind");
                    IStatementParameter param = StatementManager.createParameter(tag);
                    if (param != null) {
                        param.readFromNBT(cpt);
                        actionParameters[i][j] = param;
                    } else {
                        BCLog.logger.warn("Didn't find an IStatementParamater for " + tag);
                    }
                }
            }
        }
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

        for (int i = 0; i < triggers.length; i++) {
            TriggerWrapper trigger = triggers[i];
            if (trigger != null) {
                nbt.setString("trigger[" + i + "]", trigger.getUniqueTag());
                nbt.setByte("trigger[" + i + "].side", (byte) trigger.sourcePart.getIndex());

                for (int j = 0; j < triggerParameters[i].length; j++) {
                    IStatementParameter param = triggerParameters[i][j];
                    if (param != null) {
                        NBTTagCompound cpt = new NBTTagCompound();
                        param.writeToNBT(cpt);
                        cpt.setString("kind", param.getUniqueTag());
                        nbt.setTag("triggerParameters[" + i + "][" + j + "]", cpt);
                    }
                }
            }
        }

        for (int i = 0; i < actions.length; i++) {
            ActionWrapper action = actions[i];
            if (action != null) {
                nbt.setString("action[" + i + "]", action.getUniqueTag());
                nbt.setByte("action[" + i + "].side", (byte) action.sourcePart.getIndex());
            }

            for (int j = 0; j < actionParameters[i].length; j++) {
                IStatementParameter param = actionParameters[i][j];
                if (param != null) {
                    NBTTagCompound cpt = new NBTTagCompound();
                    param.writeToNBT(cpt);
                    cpt.setString("kind", param.getUniqueTag());
                    nbt.setTag("actionParameters[" + i + "][" + j + "]", cpt);
                }
            }
        }

        return nbt;
    }

    // Networking

    public GateLogic(PluggableGate pluggable, PacketBuffer buffer) {
        this(pluggable, new GateVariant(buffer));

        MessageUtil.readBooleanArray(buffer, triggerOn);
        MessageUtil.readBooleanArray(buffer, actionOn);
        MessageUtil.readBooleanArray(buffer, connections);
        boolean on = false;
        for (boolean b : actionOn) {
            on |= b;
        }
        isOn = on;
    }

    public void writeCreationToBuf(PacketBuffer buffer) {
        variant.writeToBuffer(buffer);

        MessageUtil.writeBooleanArray(buffer, triggerOn);
        MessageUtil.writeBooleanArray(buffer, actionOn);
        MessageUtil.writeBooleanArray(buffer, connections);
    }

    /** Helper method to send a custom payload to the other side via the pluggable. */
    public final void sendPayload(int id, IPayloadWriter writer) {
        pluggable.sendMessage(id, writer);
    }

    public void readPayload(int id, PacketBuffer buffer, Side side, MessageContext ctx) {
        if (side == Side.CLIENT) {
            if (id == NET_ID_RESOLVE) {
                MessageUtil.readBooleanArray(buffer, triggerOn);
                MessageUtil.readBooleanArray(buffer, actionOn);
                MessageUtil.readBooleanArray(buffer, connections);
                boolean on = false;
                for (boolean b : actionOn) {
                    on |= b;
                }
                isOn = on;
            } else {
                BCLog.logger.warn("Unknown ID " + id);
            }
        } else {
            BCLog.logger.warn("Unknown side + ID" + id);
        }
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
        return pluggable.holder.getPipeTile();
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
        return Arrays.asList(triggers);
    }

    @Override
    public List<IStatement> getActions() {
        return Arrays.asList(actions);
    }

    @Override
    public List<StatementSlot> getActiveActions() {
        return activeActions;
    }

    @Override
    public List<IStatementParameter> getTriggerParameters(int slot) {
        return Arrays.asList(triggerParameters[slot]);
    }

    @Override
    public List<IStatementParameter> getActionParameters(int slot) {
        return Arrays.asList(actionParameters[slot]);
    }

    @Override
    public int getRedstoneInput(EnumFacing side) {
        return pluggable.holder.getRedstoneInput(side);
    }

    @Override
    public boolean setRedstoneOutput(EnumFacing side, int value) {
        return pluggable.holder.setRedstoneOutput(side, value);
    }

    // Gate helpers

    public void setTrigger(int index, TriggerWrapper trigger) {
        setStatementInternal(index, triggers, triggerParameters, trigger);
    }

    public StatementWrapper getTrigger(int index) {
        return triggers[index];
    }

    public void setTriggerParam(int index, int pIndex, IStatementParameter param) {
        triggerParameters[index][pIndex] = param;
    }

    public IStatementParameter getTriggerParam(int index, int pIndex) {
        return triggerParameters[index][pIndex];
    }

    public void setAction(int index, ActionWrapper action) {
        setStatementInternal(index, actions, actionParameters, action);
    }

    public StatementWrapper getAction(int index) {
        return actions[index];
    }

    public void setActionParam(int index, int pIndex, IStatementParameter param) {
        actionParameters[index][pIndex] = param;
    }

    public IStatementParameter getActionParam(int index, int pIndex) {
        return actionParameters[index][pIndex];
    }

    /** Sets up the given trigger or action statements to the given ones. */
    private static void setStatementInternal(int index, StatementWrapper[] array, IStatementParameter[][] parameters, StatementWrapper statement) {
        StatementWrapper old = array[index];
        array[index] = statement;
        if (statement == null) {
            Arrays.fill(parameters[index], null);
        } else {
            if (old != null && old.delegate == statement.delegate) {
                // Don't clear out parameters if its the same statement with a different side.
                return;
            }
            int max = parameters[index].length;
            int maxTrigger = statement.maxParameters();
            for (int i = 0; i < maxTrigger && i < max; i++) {
                parameters[index][i] = statement.createParameter(i);
            }
            for (int i = maxTrigger; i < max; i++) {
                parameters[index][i] = null;
            }
        }
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

        for (int triggerIndex = 0; triggerIndex < triggers.length; triggerIndex++) {
            TriggerWrapper trigger = triggers[triggerIndex];
            groupCount++;
            if (trigger != null) {
                if (trigger.isTriggerActive(this, triggerParameters[triggerIndex])) {
                    groupActive++;
                    triggerOn[triggerIndex] = true;
                }
            }
            if (connections.length == triggerIndex || !connections[triggerIndex]) {
                boolean allActionsActive = variant.logic == EnumGateLogic.AND ? groupActive == groupCount : groupActive > 0;
                for (int i = groupCount - 1; i >= 0; i--) {
                    int actionIndex = triggerIndex - i;
                    ActionWrapper action = actions[actionIndex];
                    actionOn[actionIndex] = allActionsActive;
                    if (action != null) {
                        if (allActionsActive) {
                            StatementSlot slot = new StatementSlot();
                            slot.statement = action.delegate;
                            slot.parameters = actionParameters[actionIndex];
                            slot.part = action.sourcePart;
                            activeActions.add(slot);
                            action.actionActivate(this, actionParameters[actionIndex]);
                            getPipeHolder().fireEvent(new PipeEventActionActivate(getPipeHolder(), action.getDelegate(), actionParameters[actionIndex], action.sourcePart));
                        } else {
                            action.actionDeactivated(this, actionParameters[actionIndex]);
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
}
