package ct.buildcraft.energy.client.gui;

import ct.buildcraft.core.BCCoreBlocks;
import ct.buildcraft.energy.BCEnergyGuis;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class $MenuEngineStone_BC8 extends AbstractContainerMenu {

	private final ContainerLevelAccess access;
	protected final ContainerData data;
	
	public $MenuEngineStone_BC8(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new ItemStackHandler(1), new SimpleContainerData(2), ContainerLevelAccess.NULL);
	}
	
	public $MenuEngineStone_BC8(int containerId, Inventory playerInventory, IItemHandler dataInventory, ContainerData data, ContainerLevelAccess access) {
		super(BCEnergyGuis.MENU_STONE.get(), containerId);
		this.access = ContainerLevelAccess.NULL;
		this.data = data;
		this.addSlot(new SlotItemHandler(dataInventory,  0, 80, 41));
	      for(int i = 0; i < 3; ++i) {
	          for(int j = 0; j < 9; ++j) {
	             this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
	          }
	       }
		  
	      for(int k = 0; k < 9; ++k) {
	          this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
	       }
	      
		  
		  this.addDataSlots(data);

		}

	@Override
	public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return super.stillValid(this.access, player, BCCoreBlocks.ENGINE_BC8.get());
	}
	
	protected double getBurnProgress(float partialTicks) {
		if (partialTicks <= 0) {
			return data.get(0)/1000000;
		} else if (partialTicks >= 1) {
			return data.get(1)/1000000;
		} else {
			double a = data.get(0) * (1 - partialTicks) /1000000;
			double b = data.get(0) * partialTicks / 1000000;
			return a + b;
		}
	}

}
