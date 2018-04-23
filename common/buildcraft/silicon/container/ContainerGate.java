/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.StatementManager;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.ContainerPipe;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.ActionWrapper;
import buildcraft.lib.statement.StatementWrapper;
import buildcraft.lib.statement.TriggerWrapper;

import buildcraft.silicon.gate.GateContext;
import buildcraft.silicon.gate.GateContext.GateGroup;
import buildcraft.silicon.gate.GateLogic;

public class ContainerGate extends ContainerPipe {
    protected static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("gate");

    public static final int ID_CONNECTION = IDS.allocId("CONNECTION");
    public static final int ID_VALID_STATEMENTS = IDS.allocId("VALID_STATEMENTS");

    public final GateLogic gate;

    public final int slotHeight;

    public final SortedSet<TriggerWrapper> possibleTriggers;
    public final SortedSet<ActionWrapper> possibleActions;

    public final GateContext<TriggerWrapper> possibleTriggersContext;
    public final GateContext<ActionWrapper> possibleActionsContext;

    public ContainerGate(EntityPlayer player, GateLogic logic) {
        super(player, logic.getPipeHolder());
        this.gate = logic;
        gate.getPipeHolder().onPlayerOpen(player);

        boolean split = gate.isSplitInTwo();
        int s = gate.variant.numSlots;
        if (split) {
            s = (int) Math.ceil(s / 2.0);
        }
        slotHeight = s;

        if (gate.getPipeHolder().getPipeWorld().isRemote) {
            possibleTriggers = new TreeSet<>();
            possibleActions = new TreeSet<>();
        } else {
            possibleTriggers = gate.getAllValidTriggers();
            possibleActions = gate.getAllValidActions();
        }

        possibleTriggersContext = new GateContext<>(new ArrayList<>());
        possibleActionsContext = new GateContext<>(new ArrayList<>());

        refreshPossibleGroups();

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

    private void refreshPossibleGroups() {
        refresh(possibleActions, possibleActionsContext);
        refresh(possibleTriggers, possibleTriggersContext);
    }

    private static <T extends StatementWrapper> void refresh(SortedSet<T> from, GateContext<T> to) {
        to.groups.clear();
        Map<EnumPipePart, List<T>> parts = new EnumMap<>(EnumPipePart.class);
        for (T val : from) {
            parts.computeIfAbsent(val.sourcePart, p -> new ArrayList<>()).add(val);
        }
        List<T> list = parts.get(EnumPipePart.CENTER);
        if (list == null) {
            list = new ArrayList<>(1);
            list.add(null);
        } else {
            list.add(0, null);
        }
        to.groups.add(new GateGroup<>(EnumPipePart.CENTER, list));
        for (EnumPipePart part : EnumPipePart.FACES) {
            list = parts.get(part);
            if (list != null) {
                to.groups.add(new GateGroup<>(part, list));
            }
        }
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
            } else if (id == ID_VALID_STATEMENTS) {
                sendMessage(ID_VALID_STATEMENTS);
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
                refreshPossibleGroups();
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
            }
        }
    }

    public void setConnected(int index, boolean to) {
        sendMessage(ID_CONNECTION, (buffer) -> {
            buffer.writeByte(index);
            buffer.writeBoolean(to);
        });
    }
}
