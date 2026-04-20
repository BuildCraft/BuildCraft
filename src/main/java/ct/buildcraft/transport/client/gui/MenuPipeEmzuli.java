package ct.buildcraft.transport.client.gui;

import ct.buildcraft.core.BCCoreBlocks;
import ct.buildcraft.lib.gui.RecordSlot;
import ct.buildcraft.transport.BCTransportGuis;

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

public class MenuPipeEmzuli extends AbstractContainerMenu {

	protected final ContainerLevelAccess access;
	final ContainerData data;
	
	public MenuPipeEmzuli(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new ItemStackHandler(4), new SimpleContainerData(4),ContainerLevelAccess.NULL);
	}
	
	public MenuPipeEmzuli(int containerId, Inventory playerInventory, IItemHandler dataInventory, ContainerData data, ContainerLevelAccess access) {
		super(BCTransportGuis.MENU_PIPE_EMZULI.get(), containerId);
		this.access = ContainerLevelAccess.NULL;
		this.data = data;
		for(int i = 0; i < 3; ++i) 
			for(int j = 0; j < 9; ++j) 
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
		
		for(int k = 0; k < 9; ++k) 
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
		
		for(int i = 0; i < 2;i++) 
			for(int j = 0; j<2;j++)
				this.addSlot(new RecordSlot(dataInventory, 2*i+j, 25+109*j, 28	*i + 21));
		
		addDataSlots(data);
	}

	@Override
	public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return super.stillValid(this.access, player, BCCoreBlocks.ENGINE_BC8.get());
	}

	@Override
	public boolean clickMenuButton(Player p_38875_, int index) {
		int o = data.get(index);
		data.set(index, o==128 ? 0 : o == 15 ? 128 : o+1);
		return true;
	}

	
	

}