/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import net.minecraftforge.items.IItemHandler;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiaWood;

public class ContainerDiaWoodPipe extends ContainerBC_Neptune {
    private final PipeBehaviourDiaWood behaviour;
    private final IItemHandler filterInv;

    public ContainerDiaWoodPipe(EntityPlayer player, PipeBehaviourDiaWood behaviour) {
        super(player);
        this.behaviour = behaviour;
        this.filterInv = behaviour.filters;

        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotPhantom(filterInv, i, 8 + i * 18, 18));
        }

        for (int l = 0; l < 3; l++) {
            for (int k1 = 0; k1 < 9; k1++) {
                addSlotToContainer(new Slot(player.inventory, k1 + l * 9 + 9, 8 + k1 * 18, 79 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; i1++) {
            addSlotToContainer(new Slot(player.inventory, i1, 8 + i1 * 18, 137));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return true;// FIXME!
    }
}
