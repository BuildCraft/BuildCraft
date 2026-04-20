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

public class MenuFiller extends AbstractContainerMenu {

	protected final ContainerLevelAccess access;
	
	public MenuFiller(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new ItemStackHandler(27), ContainerLevelAccess.NULL);
	}
	
	public MenuFiller(int containerId, Inventory playerInventory, IItemHandler res, ContainerLevelAccess access) {
		super(BCBuildersGuis.MENU_FILLER.get(), containerId);
		this.access = ContainerLevelAccess.NULL;
		for(int i = 0; i < 3; ++i) 
			for(int j = 0; j < 9; ++j) 
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 48 + j * 18, 84 + i * 18));
		
		for(int k = 0; k < 9; ++k) 
			this.addSlot(new Slot(playerInventory, k, 48 + k * 18, 142));
		for(int i = 0; i < 3; ++i) 
			for(int j = 0; j < 9; ++j) 
				this.addSlot(new SlotItemHandler(res, j + i * 9, 148 + j * 18, 184 + i * 18));
		
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
		return super.stillValid(this.access, player, BCBuildersBlocks.FILLER.get());
	}

	

}