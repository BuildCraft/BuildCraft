package ct.buildcraft.transport.client.gui;

import ct.buildcraft.core.BCCoreBlocks;
import ct.buildcraft.lib.gui.RecordSlot;
import ct.buildcraft.transport.BCTransportGuis;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class MenuPipeDiamond extends AbstractContainerMenu {

	protected final ContainerLevelAccess access;
	
	public MenuPipeDiamond(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new ItemStackHandler(54), ContainerLevelAccess.NULL);
	}
	
	public MenuPipeDiamond(int containerId, Inventory playerInventory, IItemHandler dataInventory, ContainerLevelAccess access) {
		super(BCTransportGuis.MENU_PIPE_DIAMOND.get(), containerId);
		this.access = ContainerLevelAccess.NULL;
//		this.addSlot(new SlotItemHandler(dataInventory,  0, 80, 41));
		for(int i = 0; i < 3; ++i) 
			for(int j = 0; j < 9; ++j) 
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
		
		for(int k = 0; k < 9; ++k) 
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 162));
		
		for(int i = 0; i < 6;i++) 
			for(int j = 0; j<9;j++)
				this.addSlot(new RecordSlot(dataInventory, 9*i+j, 8+18*j, 18*i-18));

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