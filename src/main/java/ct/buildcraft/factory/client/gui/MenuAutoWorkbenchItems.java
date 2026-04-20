package ct.buildcraft.factory.client.gui;

import ct.buildcraft.core.BCCoreBlocks;
import ct.buildcraft.factory.BCFactoryGuis;
import ct.buildcraft.lib.gui.RecordContainerSlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MenuAutoWorkbenchItems extends AbstractContainerMenu {
	private static final int SLOTS_NUM = 9 + 9 + 9 + 1 + 1;//craftTable, bluePrint, Martial , Result, AssumedResult
	protected final ContainerLevelAccess access;
	
	public MenuAutoWorkbenchItems(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new SimpleContainer(SLOTS_NUM+9), ContainerLevelAccess.NULL);
	}
	
	public MenuAutoWorkbenchItems(int containerId, Inventory playerInventory, Container craft, ContainerLevelAccess access) {
		super(BCFactoryGuis.MENU_AUTOWORK_BENCH_ITEM.get(), containerId);
		this.access = ContainerLevelAccess.NULL;
		for(int i = 0; i < 3; ++i) 
			for(int j = 0; j < 9; ++j) 
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 95 + i * 18));
		
		for(int k = 0; k < 9; ++k) 
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 153));
		
		for(int j = 0; j<9;j++) {
			this.addSlot(new RecordContainerSlot(craft, j+9, 30 + (j%3)*18, -3 + 18*(j/3)));
		}
		for(int i = 0; i<9;i++) {
			this.addSlot(new Slot(craft , i+18, 8 + i * 18, 64));
		}
		this.addSlot(new ResultSlot(playerInventory.player, craft instanceof CraftingContainer a ? a : new CraftingContainer(this, 3, 3), craft, 27, 124, 15));
		this.addSlot(new RecordContainerSlot(craft, 28, 93, 7));

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
