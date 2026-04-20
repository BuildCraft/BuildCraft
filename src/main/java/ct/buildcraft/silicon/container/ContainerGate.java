/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.statements.StatementManager;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.lib.gui.ContainerPipe;
import ct.buildcraft.lib.gui.MenuBC_Neptune;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.statement.ActionWrapper;
import ct.buildcraft.lib.statement.StatementWrapper;
import ct.buildcraft.lib.statement.TriggerWrapper;
import ct.buildcraft.silicon.BCSiliconGuis;
import ct.buildcraft.silicon.gate.GateContext;
import ct.buildcraft.silicon.gate.GateContext.GateGroup;
import ct.buildcraft.silicon.gate.GateLogic;
import ct.buildcraft.silicon.plug.PluggableGate;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class ContainerGate extends ContainerPipe {
    protected static final IdAllocator IDS = MenuBC_Neptune.IDS.makeChild("gate");

    public static final int ID_CONNECTION = IDS.allocId("CONNECTION");
    public static final int ID_VALID_STATEMENTS = IDS.allocId("VALID_STATEMENTS");

    public final GateLogic gate;

    public final int slotHeight;

    public final SortedSet<TriggerWrapper> possibleTriggers;
    public final SortedSet<ActionWrapper> possibleActions;

    public final GateContext<TriggerWrapper> possibleTriggersContext;
    public final GateContext<ActionWrapper> possibleActionsContext;

	public static ContainerGate creatClientMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
		ContainerLevelAccess access = CreateClientLevelAccess(buf);
		Direction gateSide = buf.readEnum(Direction.class);
		return access.evaluate((level, pos) -> {
			BlockEntity tile = level.getBlockEntity(pos);
            if (tile != null &&tile instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) tile;
                PipePluggable plug = holder.getPluggable(gateSide);
                if (plug instanceof PluggableGate gate) {
                    return new ContainerGate(containerId, playerInventory, gate.logic);
                }
            }
            BCLog.logger.error("ContainerGate.CreatClientMenu:Failed to creat menu");
            return null;
		}, null);
	}
    
    public ContainerGate(int containerId, Inventory playerInventory, GateLogic logic) {
        super(playerInventory, BCSiliconGuis.MENU_GATE.get(), containerId, logic.getPipeHolder());
        this.gate = logic;
        gate.getPipeHolder().onPlayerOpen(playerInventory.player);

        boolean split = gate.isSplitInTwo();
        int s = gate.variant.numSlots;
        if (split) {
            s = (int) Math.ceil(s / 2.0);
        }
        slotHeight = s;

        if (gate.getPipeHolder().getPipeWorld().isClientSide()) {
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
	public void removed(Player player) {
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
    public void readMessage(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        if (side == LogicalSide.SERVER) {
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
        } else if (side == LogicalSide.CLIENT) {
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
    public void writeMessage(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writeMessage(id, buffer, side);
        if (side == LogicalSide.SERVER) {
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
        sendMessage(ID_CONNECTION, (buffer) -> {
            buffer.writeByte(index);
            buffer.writeBoolean(to);
        });
    }
}
