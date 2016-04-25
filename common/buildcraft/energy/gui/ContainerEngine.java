/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

import buildcraft.core.lib.engines.TileEngineWithInventory;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.widgets.FluidTankWidget;
import buildcraft.energy.TileEngineIron;
import buildcraft.energy.TileEngineStone;
import buildcraft.lib.gui.ContainerBC8;

public class ContainerEngine extends ContainerBC8 {

    protected TileEngineWithInventory engine;

    public ContainerEngine(EntityPlayer player, TileEngineWithInventory tileEngine) {
        super(player, tileEngine.getSizeInventory());

        engine = tileEngine;

        int yOffset = 0;

        if (tileEngine instanceof TileEngineStone) {
            addSlotToContainer(new Slot(tileEngine, 0, 80, 41));
        } else {// Assume TileEngineIron
            TileEngineIron combustionEngine = (TileEngineIron) tileEngine;

            FluidTankWidget fuelWidget = new FluidTankWidget(combustionEngine.tankFuel, 26, 19, 16, 58).withOverlay(176, 0);
            addWidget(fuelWidget);
            addWidget(fuelWidget.copyMoved(combustionEngine.tankCoolant, 80, 19));
            addWidget(fuelWidget.copyMoved(combustionEngine.tankResidue, 134, 19));
            yOffset = 11;
        }

        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 9; k++) {
                addSlotToContainer(new Slot(player.inventory, k + i * 9 + 9, 8 + k * 18, 84 + i * 18 + yOffset));
            }
        }

        for (int j = 0; j < 9; j++) {
            addSlotToContainer(new Slot(player.inventory, j, 8 + j * 18, 142 + yOffset));
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Object crafter : crafters) {
            engine.sendGUINetworkData(this, (ICrafting) crafter);
        }
    }

    @Override
    public void updateProgressBar(int i, int j) {
        engine.getGUINetworkData(i, j);
    }

    public boolean isUsableByPlayer(EntityPlayer entityplayer) {
        return engine.isUseableByPlayer(entityplayer);
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return engine.isUseableByPlayer(entityplayer);
    }
}
