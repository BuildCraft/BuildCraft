/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.container;

import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.lib.gui.ContainerPipe;
import ct.buildcraft.lib.gui.slot.SlotPhantom;
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.transport.BCTransportGuis;
import ct.buildcraft.transport.pipe.Pipe;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ContainerDiamondPipe extends ContainerPipe {
    private final PipeBehaviourDiamond behaviour;
    public final IItemHandlerAdv filters;
    
    public static ContainerDiamondPipe create(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
    	ContainerLevelAccess access = CreateClientLevelAccess(buf);
    	return access.evaluate((level, pos) -> {
    		BlockEntity tile = level.getBlockEntity(pos);
    		if(tile instanceof IPipeHolder pipeHolder && pipeHolder.getPipe() != Pipe.EMPTY) {
    			if(pipeHolder.getPipe().getBehaviour() instanceof PipeBehaviourDiamond diamond)
    			return new ContainerDiamondPipe(containerId, playerInventory, new ItemHandlerSimple(PipeBehaviourDiamond.FILTERS_PER_SIDE * 6), diamond);
    		}
    		return null;
    	}, null);
    }

    public ContainerDiamondPipe(int containerId, Inventory playerInventory, IItemHandlerAdv filterInv, PipeBehaviourDiamond pipe) {
        super(playerInventory, BCTransportGuis.MENU_PIPE_DIAMOND.get(), containerId, pipe.pipe.getHolder());
        this.behaviour = pipe;
        this.filters = filterInv;
        behaviour.pipe.getHolder().onPlayerOpen(playerInventory.player);

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new SlotPhantom(filterInv, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
        }

        for (int l = 0; l < 3; l++) {
            for (int k1 = 0; k1 < 9; k1++) {
                addSlot(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 140 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; i1++) {
            addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 198));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        behaviour.pipe.getHolder().onPlayerClose(player);
    }
}
