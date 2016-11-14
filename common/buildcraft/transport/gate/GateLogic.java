package buildcraft.transport.gate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCLog;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.neptune.IPipeHolder;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.command.IPayloadWriter;
import buildcraft.transport.plug.PluggableGate;
import buildcraft.transport.wire.IWireEmitter;

public class GateLogic implements IGate, IWireEmitter {
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
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("variant", variant.writeToNbt());
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
        return getPipeHolder().getNeighbouringTile(side);
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

    // Gate helpers

    public void setTrigger(int index, IStatement trigger) {
        setStatementInternal(index, triggers, triggerParameters, trigger);
    }

    public IStatement getTrigger(int index) {
        return triggers[index];
    }

    public void setTriggerParam(int index, int pIndex, IStatementParameter param) {
        triggerParameters[index][pIndex] = param;
    }

    public IStatementParameter getTriggerParam(int index, int pIndex) {
        return triggerParameters[index][pIndex];
    }

    public void setAction(int index, IStatement trigger) {
        setStatementInternal(index, actions, actionParameters, trigger);
    }

    public void setActionParam(int index, int pIndex, IStatementParameter param) {
        actionParameters[index][pIndex] = param;
    }

    /** Sets up the given trigger or action statements to the given ones. */
    private static void setStatementInternal(int index, IStatement[] array, IStatementParameter[][] paramters, IStatement statement) {
        array[index] = statement;
        if (statement == null) {
            Arrays.fill(paramters[index], null);
        } else {
            int max = paramters[index].length;
            int maxTrigger = statement.maxParameters();
            for (int i = 0; i < maxTrigger && i < max; i++) {
                paramters[index][i] = statement.createParameter(i);
            }
            for (int i = maxTrigger; i < max; i++) {
                paramters[index][i] = null;
            }
        }
    }

    // Wire related

    @Override
    public boolean isEmitting(EnumDyeColor colour) {
        return wireBroadcasts.contains(colour);
    }

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
                    if (action != null) {
                        if (allActionsActive) {
                            StatementSlot slot = new StatementSlot();
                            slot.statement = action.delegate;
                            slot.parameters = actionParameters[actionIndex];
                            slot.part = action.sourcePart;
                            activeActions.add(slot);
                            action.actionActivate(this, actionParameters[actionIndex]);
                            actionOn[actionIndex] = true;
                        } else {
                            action.actionDeactivated(this, actionParameters[actionIndex]);
                        }
                    }
                }
                groupActive = 0;
                groupCount = 0;
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
        // TEMP -- this is waaay to often, but is just for testing purposes.
        resolveActions();
    }
}
