package ct.buildcraft.lib.gui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class BCMenuBase_Neptune extends AbstractContainerMenu{

	protected BCMenuBase_Neptune(MenuType<?> p_38851_, int p_38852_) {
		super(p_38851_, p_38852_);
	}
	
	protected void addInventory(Inventory playerInventory, int yoffset, int xoffset) {
		for(int i = 0; i < 3; ++i) 
			for(int j = 0; j < 9; ++j) 
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 86 + i * 18));
		for(int k = 0; k < 9; ++k) 
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 144));
	}

	@Override
	public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player p_38874_) {
		return false;
	}

}
