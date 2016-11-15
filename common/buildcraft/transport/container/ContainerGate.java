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

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.data.ForwardingReference;
import buildcraft.lib.misc.data.IReference;
import buildcraft.transport.gate.*;

public class ContainerGate extends ContainerBC_Neptune {
    public static final int ID_CONNECTION = 0;
    public static final int ID_TRIGGER = 1;
    public static final int ID_TRIGGER_PARAM = 2;
    public static final int ID_ACTION = 3;
    public static final int ID_ACTION_PARAM = 4;
    public static final int ID_VALID_STATEMENTS = 5;

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
    public void handleMessage(MessageContext ctx, PacketBuffer buffer, Side side) throws IOException {
        int id = buffer.readUnsignedByte();
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
                String tag = buffer.readStringFromBuffer(64);
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
                String tag = buffer.readStringFromBuffer(64);
                NBTTagCompound nbt = buffer.readNBTTagCompoundFromBuffer();

                IStatementParameter param = StatementManager.createParameter(tag);
                if (param != null) {
                    param.readFromNBT(nbt);
                }

                if (id == ID_TRIGGER_PARAM) {
                    gate.setTriggerParam(index, paramIndex, param);
                } else {
                    gate.setActionParam(index, paramIndex, param);
                }
            } else if (id == ID_VALID_STATEMENTS) {
                this.sendMessage((buf) -> {
                    BCLog.logger.info("Sending triggers and statements (" + possibleTriggers.size() + ", " + possibleActions.size() + ")");
                    buf.writeByte(ID_VALID_STATEMENTS);
                    buf.writeInt(possibleTriggers.size());
                    buf.writeInt(possibleActions.size());
                    for (TriggerWrapper wrapper : possibleTriggers) {
                        buf.writeString(wrapper.getUniqueTag());
                        buf.writeByte(wrapper.sourcePart.getIndex());
                    }

                    for (ActionWrapper wrapper : possibleActions) {
                        buf.writeString(wrapper.getUniqueTag());
                        buf.writeByte(wrapper.sourcePart.getIndex());
                    }
                });
            }
        } else if (side == Side.CLIENT) {
            if (id == ID_VALID_STATEMENTS) {
                BCLog.logger.info("Receiving triggers and statements ( " + buffer.getInt(buffer.readerIndex()) + ", " + buffer.getInt(buffer.readerIndex() + 4) + " )");
                possibleTriggers.clear();
                possibleActions.clear();
                int numTriggers = buffer.readInt();
                int numActions = buffer.readInt();
                for (int i = 0; i < numTriggers; i++) {
                    String tag = buffer.readStringFromBuffer(256);
                    EnumPipePart part = EnumPipePart.fromMeta(buffer.readUnsignedByte());
                    TriggerWrapper wrapper = TriggerWrapper.wrap(StatementManager.statements.get(tag), part.face);
                    if (gate.isValidTrigger(wrapper)) {
                        possibleTriggers.add(wrapper);
                    }
                }
                for (int i = 0; i < numActions; i++) {
                    String tag = buffer.readStringFromBuffer(256);
                    EnumPipePart part = EnumPipePart.fromMeta(buffer.readUnsignedByte());
                    ActionWrapper wrapper = ActionWrapper.wrap(StatementManager.statements.get(tag), part.face);
                    if (gate.isValidAction(wrapper)) {
                        possibleActions.add(wrapper);
                    }
                }
            }
        }
    }

    public void setConnected(int index, boolean to) {
        sendMessage((buffer) -> {
            buffer.writeByte(ID_CONNECTION);
            buffer.writeByte(index);
            buffer.writeBoolean(to);
        });
    }

    private void setFirst(int id, int index, StatementWrapper to) {
        sendMessage((buffer) -> {
            buffer.writeByte(id);
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
        sendMessage((buffer) -> {
            buffer.writeByte(id);
            buffer.writeByte(index);
            buffer.writeByte(paramIndex);
            if (to == null) {
                buffer.writeString("");
                buffer.writeNBTTagCompoundToBuffer(null);
            } else {
                buffer.writeString(to.getUniqueTag());
                NBTTagCompound nbt = new NBTTagCompound();
                to.writeToNBT(nbt);
                buffer.writeNBTTagCompoundToBuffer(nbt);
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

        public SlotPair(final int slotIndex, GateVariant variant) {
            trigger = new ForwardingReference<>(() -> logic().triggers[slotIndex], (val) -> {
                setTrigger(slotIndex, val);
            });
            action = new ForwardingReference<>(() -> logic().actions[slotIndex], (val) -> {
                setAction(slotIndex, val);
            });
            triggerParams = new ParamRef[variant.numTriggerArgs];
            actionParams = new ParamRef[variant.numActionArgs];
            for (int i = 0; i < triggerParams.length; i++) {
                final int idx = i;
                triggerParams[i] = new ParamRef(() -> logic().triggerParameters[slotIndex][idx], (val) -> {
                    setTriggerParam(slotIndex, idx, val);
                });
            }
            for (int i = 0; i < actionParams.length; i++) {
                final int idx = i;
                actionParams[i] = new ParamRef(() -> logic().actionParameters[slotIndex][idx], (val) -> {
                    setActionParam(slotIndex, idx, val);
                });
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
    }
}
