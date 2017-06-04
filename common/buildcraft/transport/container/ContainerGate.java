/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.container;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.data.ForwardingReference;
import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.gate.ActionWrapper;
import buildcraft.transport.gate.GateLogic;
import buildcraft.transport.gate.GateVariant;
import buildcraft.transport.gate.StatementWrapper;
import buildcraft.transport.gate.TriggerWrapper;

public class ContainerGate extends ContainerBC_Neptune {
    protected static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("gate");

    public static final int ID_CONNECTION = IDS.allocId("CONNECTION");
    public static final int ID_TRIGGER = IDS.allocId("TRIGGER");
    public static final int ID_TRIGGER_PARAM = IDS.allocId("TRIGGER_PARAM");
    public static final int ID_ACTION = IDS.allocId("ACTION");
    public static final int ID_ACTION_PARAM = IDS.allocId("ACTION_PARAM");
    public static final int ID_VALID_STATEMENTS = IDS.allocId("VALID_STATEMENTS");
    public static final int ID_CURRENT_SET = IDS.allocId("CURRENT_SET");

    public final GateLogic gate;

    public final int slotHeight;
    public final SlotPair[] pairs;

    public final SortedSet<TriggerWrapper> possibleTriggers;
    public final SortedSet<ActionWrapper> possibleActions;

    public ContainerGate(EntityPlayer player, GateLogic logic) {
        super(player);
        this.gate = logic;
        gate.getPipeHolder().onPlayerOpen(player);

        boolean split = gate.isSplitInTwo();
        int s = gate.variant.numSlots;
        if (split) {
            s = (int) Math.ceil(s / 2.0);
        }
        slotHeight = s;

        pairs = new SlotPair[gate.variant.numSlots];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = new SlotPair(i, gate.variant);
        }

        if (gate.getPipeHolder().getPipeWorld().isRemote) {
            possibleTriggers = new TreeSet<>();
            possibleActions = new TreeSet<>();
        } else {
            possibleTriggers = gate.getAllValidTriggers();
            possibleActions = gate.getAllValidActions();
        }

        addFullPlayerInventory(33 + slotHeight * 18);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        gate.getPipeHolder().onPlayerClose(player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        if (side == Side.SERVER) {
            if (id == ID_CONNECTION) {
                int index = buffer.readUnsignedByte();
                boolean to = buffer.readBoolean();
                if (index < gate.connections.length) {
                    gate.connections[index] = to;
                    gate.sendResolveData();
                }
            } else if (id == ID_TRIGGER || id == ID_ACTION) {
                int index = buffer.readUnsignedByte();
                String tag = buffer.readString(64);
                EnumFacing face = EnumPipePart.fromMeta(buffer.readUnsignedByte()).face;
                IStatement statement = StatementManager.statements.get(tag);
                if (id == ID_TRIGGER) {
                    gate.setTrigger(index, TriggerWrapper.wrap(statement, face));
                } else {
                    gate.setAction(index, ActionWrapper.wrap(statement, face));
                }
            } else if (id == ID_TRIGGER_PARAM || id == ID_ACTION_PARAM) {
                int index = buffer.readUnsignedByte();
                int paramIndex = buffer.readUnsignedByte();

                if (buffer.readBoolean()) {
                    NBTTagCompound nbt = buffer.readCompoundTag();

                    IStatementParameter param;
                    if (id == ID_TRIGGER_PARAM) {
                        param = gate.getTriggerParam(index, paramIndex);
                    } else {
                        param = gate.getActionParam(index, paramIndex);
                    }

                    if (param != null) {
                        param.readFromNBT(nbt);
                    }

                    if (id == ID_TRIGGER_PARAM) {
                        gate.setTriggerParam(index, paramIndex, param);
                    } else {
                        gate.setActionParam(index, paramIndex, param);
                    }
                }
            } else if (id == ID_VALID_STATEMENTS) {
                sendMessage(ID_VALID_STATEMENTS);
            } else if (id == ID_CURRENT_SET) {
                sendMessage(ID_CURRENT_SET);
            }
        } else if (side == Side.CLIENT) {
            if (id == ID_VALID_STATEMENTS) {
                possibleTriggers.clear();
                possibleActions.clear();
                int numTriggers = buffer.readInt();
                int numActions = buffer.readInt();
                for (int i = 0; i < numTriggers; i++) {
                    String tag = buffer.readString(256);
                    EnumPipePart part = buffer.readEnumValue(EnumPipePart.class);
                    TriggerWrapper wrapper = TriggerWrapper.wrap(StatementManager.statements.get(tag), part.face);
                    if (gate.isValidTrigger(wrapper)) {
                        possibleTriggers.add(wrapper);
                    }
                }
                for (int i = 0; i < numActions; i++) {
                    String tag = buffer.readString(256);
                    EnumPipePart part = buffer.readEnumValue(EnumPipePart.class);
                    ActionWrapper wrapper = ActionWrapper.wrap(StatementManager.statements.get(tag), part.face);
                    if (gate.isValidAction(wrapper)) {
                        possibleActions.add(wrapper);
                    }
                }
            } else if (id == ID_CURRENT_SET) {
                for (SlotPair pair : pairs) {
                    pair.readFromBuffer(buffer);
                }
            }
        }
    }

    @Override
    public void writeMessage(int id, PacketBufferBC buffer, Side side) {
        super.writeMessage(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == ID_VALID_STATEMENTS) {
                buffer.writeInt(possibleTriggers.size());
                buffer.writeInt(possibleActions.size());
                for (TriggerWrapper wrapper : possibleTriggers) {
                    buffer.writeString(wrapper.getUniqueTag());
                    buffer.writeEnumValue(wrapper.sourcePart);
                }

                for (ActionWrapper wrapper : possibleActions) {
                    buffer.writeString(wrapper.getUniqueTag());
                    buffer.writeEnumValue(wrapper.sourcePart);
                }
            } else if (id == ID_CURRENT_SET) {
                for (SlotPair pair : pairs) {
                    pair.writeToBuffer(buffer);
                }
            }
        }
    }

    public void setConnected(int index, boolean to) {
        sendMessage(ID_CONNECTION, (buffer) -> {
            buffer.writeByte(index);
            buffer.writeBoolean(to);
        });
    }

    private void setFirst(int id, int index, StatementWrapper to) {
        sendMessage(id, (buffer) -> {
            buffer.writeByte(index);
            if (to == null) {
                buffer.writeString("");
                buffer.writeByte(0);
            } else {
                buffer.writeString(to.getUniqueTag());
                buffer.writeByte(to.sourcePart.getIndex());
            }
        });
    }

    private void setParam(int id, int index, int paramIndex, IStatementParameter to) {
        sendMessage(id, (buffer) -> {
            buffer.writeByte(index);
            buffer.writeByte(paramIndex);
            if (to == null) {
                buffer.writeBoolean(false);
            } else {
                buffer.writeBoolean(true);
                NBTTagCompound nbt = new NBTTagCompound();
                to.writeToNBT(nbt);
                buffer.writeCompoundTag(nbt);
            }
        });
    }

    private void setTrigger(int index, TriggerWrapper to) {
        setFirst(ID_TRIGGER, index, to);
        gate.setTrigger(index, to);
    }

    private void setAction(int index, ActionWrapper to) {
        setFirst(ID_ACTION, index, to);
        gate.setAction(index, to);
    }

    private void setTriggerParam(int index, int paramIndex, IStatementParameter to) {
        setParam(ID_TRIGGER_PARAM, index, paramIndex, to);
        gate.setTriggerParam(index, paramIndex, to);
    }

    private void setActionParam(int index, int paramIndex, IStatementParameter to) {
        setParam(ID_ACTION_PARAM, index, paramIndex, to);
        gate.setActionParam(index, paramIndex, to);
    }

    public class SlotPair {
        public final IReference<TriggerWrapper> trigger;
        public final IReference<ActionWrapper> action;
        public final ParamRef[] triggerParams;
        public final ParamRef[] actionParams;
        private final int index;

        public SlotPair(final int slotIndex, GateVariant variant) {
            this.index = slotIndex;
            trigger = new ForwardingReference<>(() -> logic().triggers[slotIndex], (val) -> setTrigger(slotIndex, val));
            action = new ForwardingReference<>(() -> logic().actions[slotIndex], (val) -> setAction(slotIndex, val));
            triggerParams = new ParamRef[variant.numTriggerArgs];
            actionParams = new ParamRef[variant.numActionArgs];
            for (int i = 0; i < triggerParams.length; i++) {
                final int idx = i;
                triggerParams[i] = new ParamRef(
                    () -> logic().triggerParameters[slotIndex][idx],
                    val -> setTriggerParam(slotIndex, idx, val)
                );
            }
            for (int i = 0; i < actionParams.length; i++) {
                final int idx = i;
                actionParams[i] = new ParamRef(
                    () -> logic().actionParameters[slotIndex][idx],
                    val -> setActionParam(slotIndex, idx, val)
                );
            }
        }

        public void writeToBuffer(PacketBuffer buffer) {
            StatementWrapper wrapper = trigger.get();
            if (wrapper == null) {
                buffer.writeString("");
                buffer.writeByte(0);
            } else {
                buffer.writeString(wrapper.getUniqueTag());
                buffer.writeByte(wrapper.sourcePart.getIndex());
            }
            wrapper = action.get();
            if (wrapper == null) {
                buffer.writeString("");
                buffer.writeByte(0);
            } else {
                buffer.writeString(wrapper.getUniqueTag());
                buffer.writeByte(wrapper.sourcePart.getIndex());
            }
            for (ParamRef triggerParam : triggerParams) {
                triggerParam.writeToBuffer(buffer);
            }
            for (ParamRef actionParam : actionParams) {
                actionParam.writeToBuffer(buffer);
            }
        }

        public void readFromBuffer(PacketBuffer buffer) throws IOException {
            {
                String tag = buffer.readString(256);
                EnumPipePart part = EnumPipePart.fromMeta(buffer.readUnsignedByte());
                TriggerWrapper wrapper = TriggerWrapper.wrap(StatementManager.statements.get(tag), part.face);
                if (gate.isValidTrigger(wrapper)) {
                    gate.setTrigger(index, wrapper);
                }
            }
            {
                String tag = buffer.readString(256);
                EnumPipePart part = EnumPipePart.fromMeta(buffer.readUnsignedByte());
                ActionWrapper wrapper = ActionWrapper.wrap(StatementManager.statements.get(tag), part.face);
                if (gate.isValidAction(wrapper)) {
                    gate.setAction(index, wrapper);
                }
            }
            for (ParamRef triggerParam : triggerParams) {
                triggerParam.readFromBuffer(buffer);
            }
            for (ParamRef actionParam : actionParams) {
                actionParam.readFromBuffer(buffer);
            }
        }

        private GateLogic logic() {
            return ContainerGate.this.gate;
        }
    }

    private static class ParamRef extends ForwardingReference<IStatementParameter> {
        public ParamRef(Supplier<IStatementParameter> getter, Consumer<IStatementParameter> setter) {
            super(getter, setter);
        }

        public void writeToBuffer(PacketBuffer buffer) {
            IStatementParameter current = get();
            if (current == null) {
                buffer.writeBoolean(false);
            } else {
                buffer.writeBoolean(true);
                NBTTagCompound nbt = new NBTTagCompound();
                current.writeToNBT(nbt);
                buffer.writeCompoundTag(nbt);
            }
        }

        public void readFromBuffer(PacketBuffer buffer) throws IOException {
            if (buffer.readBoolean()) {
                NBTTagCompound nbt = buffer.readCompoundTag();
                IStatementParameter param = get();
                if (param != null) {
                    param.readFromNBT(nbt);
                }
            }
        }
    }
}
