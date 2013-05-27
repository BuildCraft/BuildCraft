package buildcraft.silicon.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.slots.SlotPhantom;
import buildcraft.core.gui.slots.SlotUntouchable;
import buildcraft.silicon.TileAdvancedCraftingTable;
import net.minecraft.inventory.ICrafting;

public class ContainerAdvancedCraftingTable extends BuildCraftContainer {
	private TileAdvancedCraftingTable workbench;

	public ContainerAdvancedCraftingTable(InventoryPlayer playerInventory, TileAdvancedCraftingTable table) {
		super(table.getSizeInventory());
		this.workbench = table;

		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				addSlotToContainer(new SlotPhantom(table.getCraftingSlots(), x + y * 3, 31 + x * 18, 16 + y * 18));
			}
		}
		
		addSlotToContainer(new SlotUntouchable(table.getOutputSlot(), 0, 125, 34));

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

	@SuppressWarnings("rawtypes")
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (int i = 0; i < crafters.size(); i++) {
			workbench.sendGUINetworkData(this, (ICrafting) crafters.get(i));
		}
	}

	@Override
	public void updateProgressBar(int par1, int par2) {
		workbench.getGUINetworkData(par1, par2);
	}

}
