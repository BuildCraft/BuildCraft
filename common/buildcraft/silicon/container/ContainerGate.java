/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.util.*;

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

    public ContainerGate(MenuType menuType, int id, Player player, GateLogic logic) {
        super(menuType, id, player, logic.getPipeHolder());
        this.gate = logic;
//        gate.getPipeHolder().onPlayerOpen(player); // Calen: moved to BCSiliconMenuTypes to ensure message handled before gui opened

        boolean split = gate.isSplitInTwo();
        int s = gate.variant.numSlots;
        if (split) {
            s = (int) Math.ceil(s / 2.0);
        }
        slotHeight = s;

        if (gate.getPipeHolder().getPipeWorld().isClientSide) {
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
//    public void onContainerClosed(Player player)
    public void removed(Player player) {
//        super.onContainerClosed(player);
        super.removed(player);
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
    public void readMessage(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        if (side == NetworkDirection.PLAY_TO_SERVER) {
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
        } else if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == ID_VALID_STATEMENTS) {
                possibleTriggers.clear();
                possibleActions.clear();
                int numTriggers = buffer.readInt();
                int numActions = buffer.readInt();
                for (int i = 0; i < numTriggers; i++) {
                    String tag = buffer.readUtf(256);
                    EnumPipePart part = buffer.readEnum(EnumPipePart.class);
                    TriggerWrapper wrapper = TriggerWrapper.wrap(StatementManager.statements.get(tag), part.face);
                    if (gate.isValidTrigger(wrapper)) {
                        possibleTriggers.add(wrapper);
                    }
                }
                for (int i = 0; i < numActions; i++) {
                    String tag = buffer.readUtf(256);
                    EnumPipePart part = buffer.readEnum(EnumPipePart.class);
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
    public void writeMessage(int id, PacketBufferBC buffer, Dist side) {
        super.writeMessage(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == ID_VALID_STATEMENTS) {
                buffer.writeInt(possibleTriggers.size());
                buffer.writeInt(possibleActions.size());
                for (TriggerWrapper wrapper : possibleTriggers) {
                    buffer.writeUtf(wrapper.getUniqueTag());
                    buffer.writeEnum(wrapper.sourcePart);
                }

                for (ActionWrapper wrapper : possibleActions) {
                    buffer.writeUtf(wrapper.getUniqueTag());
                    buffer.writeEnum(wrapper.sourcePart);
                }
            }
        }
    }

    public void setConnected(int index, boolean to) {
        sendMessage(ID_CONNECTION, (buffer) ->
        {
            buffer.writeByte(index);
            buffer.writeBoolean(to);
        });
    }
}
