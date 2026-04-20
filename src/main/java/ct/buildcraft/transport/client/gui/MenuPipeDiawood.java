package ct.buildcraft.transport.client.gui;

import ct.buildcraft.lib.gui.RecordSlot;
import ct.buildcraft.transport.BCTransportBlocks;
import ct.buildcraft.transport.BCTransportGuis;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class MenuPipeDiawood extends AbstractContainerMenu {

	protected final ContainerLevelAccess access;
	protected final DataSlot modeData;
	protected final DataSlot filterData;
	
	public MenuPipeDiawood(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new ItemStackHandler(9), DataSlot.standalone(), DataSlot.standalone(), ContainerLevelAccess.NULL);
	}
	
	public MenuPipeDiawood(int containerId, Inventory playerInventory, IItemHandler dataInventory, DataSlot modeData, DataSlot filterData, ContainerLevelAccess access) {
		super(BCTransportGuis.MENU_PIPE_DIAMOND_WOOD.get(), containerId);
		this.access = ContainerLevelAccess.NULL;
		this.modeData = modeData;
		this.filterData = filterData;
//		this.addSlot(new SlotItemHandler(dataInventory,  0, 80, 41));
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 79 + i * 18));
			}
		}
		for(int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 137));
		}
		
		for(int i = 0; i < 9;i++) {
			this.addSlot(new RecordSlot(dataInventory, i, 8+18*i, 18));
		}
		  this.addDataSlot(modeData);

	}

	@Override
	public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return super.stillValid(this.access, player, BCTransportBlocks.pipeHolder.get());
	}

	@Override
	public boolean clickMenuButton(Player player, int mode) {
		modeData.set(mode);
		return true;
	}
	
	
	

}