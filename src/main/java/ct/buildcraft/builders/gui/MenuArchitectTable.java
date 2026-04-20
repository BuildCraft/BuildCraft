package ct.buildcraft.builders.gui;

import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.builders.BCBuildersGuis;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MenuArchitectTable extends AbstractContainerMenu {

	protected final ContainerLevelAccess access;
	
	public MenuArchitectTable(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new ItemStackHandler(1), new ItemStackHandler(1), ContainerLevelAccess.NULL);
	}
	
	public MenuArchitectTable(int containerId, Inventory playerInventory, IItemHandler in, IItemHandler out, ContainerLevelAccess access) {
		super(BCBuildersGuis.MENU_ARCHITECT_TABLE.get(), containerId);
		this.access = ContainerLevelAccess.NULL;
		for(int i = 0; i < 3; ++i) 
			for(int j = 0; j < 9; ++j) 
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 48 + j * 18, 84 + i * 18));
		
		for(int k = 0; k < 9; ++k) 
			this.addSlot(new Slot(playerInventory, k, 48 + k * 18, 142));
		this.addSlot(new SlotItemHandler(in, 0 ,95, 35));
		this.addSlot(new SlotItemHandler(out, 0 ,154, 35));
/*		for(int j = 0; j<9;j++) {
			Slot typeSlot = new RecordSlot(filter, j, 8+18*j, 27).setBackground(InventoryMenu.BLOCK_ATLAS, BCTransportSprites.FILTERED_BUFFER_EMPTY_SLOT_GUI);
			this.addSlot(typeSlot);
			this.addSlot(new SlotItemHandler(main, j, 8+18*j, 61));
		}*/
	}

	@Override
	public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return super.stillValid(this.access, player, BCBuildersBlocks.ARCHITECT.get());
	}

	

}