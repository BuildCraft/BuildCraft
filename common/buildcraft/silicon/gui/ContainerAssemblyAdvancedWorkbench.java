package buildcraft.silicon.gui;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Slot;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.factory.gui.ContainerAutoWorkbench.SlotAutoCrafting;
import buildcraft.silicon.TileAssemblyAdvancedWorkbench;
import buildcraft.silicon.TileAssemblyTable;

public class ContainerAssemblyAdvancedWorkbench extends BuildCraftContainer {
	private InventoryPlayer player;
	private TileAssemblyAdvancedWorkbench workbench;

	public ContainerAssemblyAdvancedWorkbench(InventoryPlayer playerInventory, TileAssemblyAdvancedWorkbench table) {
		super(table.getSizeInventory());
		this.player = playerInventory;
		this.workbench = table;
//		addSlotToContainer(new SlotAutoCrafting(inventoryplayer.player, tile, craftResult, 0, 124, 35));
//		for (int k = 0; k < 3; k++) {
//			for (int j1 = 0; j1 < 3; j1++) {
//				addSlotToContainer(new Slot(workbench, j1 + k * 3, 31 + j1 * 18, 16 + k * 18));
//			}
//		}

		for (int k = 0; k < 3; k++) {
			for (int j1 = 0; j1 < 9; j1++) {
				addSlotToContainer(new Slot(workbench, j1 + k * 9, 8 + j1 * 18, 85 + k * 18));
			}
		}

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 153 + l * 18));
			}

		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 211));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return workbench.isUseableByPlayer(var1);
	}

}
