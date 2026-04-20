package ct.buildcraft.transport.client.gui;

import ct.buildcraft.core.BCCoreBlocks;
import ct.buildcraft.lib.gui.RecordSlot;
import ct.buildcraft.transport.BCTransportGuis;
import ct.buildcraft.transport.BCTransportSprites;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MenuFilteredBuffer extends AbstractContainerMenu {

	protected final ContainerLevelAccess access;
	
	public MenuFilteredBuffer(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new ItemStackHandler(9), new ItemStackHandler(9), ContainerLevelAccess.NULL);
	}
	
	public MenuFilteredBuffer(int containerId, Inventory playerInventory, IItemHandler filter, IItemHandler main, ContainerLevelAccess access) {
		super(BCTransportGuis.MENU_FILTERED_BUFFER.get(), containerId);
		this.access = ContainerLevelAccess.NULL;
		for(int i = 0; i < 3; ++i) 
			for(int j = 0; j < 9; ++j) 
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 86 + i * 18));
		
		for(int k = 0; k < 9; ++k) 
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 144));
		
		for(int j = 0; j<9;j++) {
			Slot typeSlot = new RecordSlot(filter, j, 8+18*j, 27).setBackground(InventoryMenu.BLOCK_ATLAS, BCTransportSprites.FILTERED_BUFFER_EMPTY_SLOT_GUI);
			this.addSlot(typeSlot);
			this.addSlot(new SlotItemHandler(main, j, 8+18*j, 61));
		}

	}

	@Override
	public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return super.stillValid(this.access, player, BCCoreBlocks.ENGINE_BC8.get());
	}

	

}