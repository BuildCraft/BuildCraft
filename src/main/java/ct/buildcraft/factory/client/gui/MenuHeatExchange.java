package ct.buildcraft.factory.client.gui;

import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.factory.BCFactoryGuis;
import ct.buildcraft.factory.tile.TileHeatExchange;
import ct.buildcraft.lib.fluid.Tank;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class MenuHeatExchange extends AbstractContainerMenu {

	private final ContainerLevelAccess access;
	protected ContainerData data;

	public MenuHeatExchange(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new ItemStackHandler(4), new SimpleContainerData(8), DataSlot.standalone(), ContainerLevelAccess.NULL);
		
	}

	public MenuHeatExchange(int containerId, Inventory playerInventory, IItemHandler item,
			ContainerData tank, DataSlot bg, ContainerLevelAccess access) {
		super(BCFactoryGuis.MENU_HEAT_EXCHANGE.get(), containerId);
		this.access = access;
		this.data = tank;
		for(int i = 0; i < 3; ++i) 
			for(int j = 0; j < 9; ++j) 
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 89 + i * 18));
		
		for(int k = 0; k < 9; ++k) 
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 147));
		addDataSlots(tank);
		
		
	}

	@Override
	public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return super.stillValid(this.access, player, BCFactoryBlocks.HEATEXCHANGE_BLOCK.get());
	}

	@Override
	public boolean clickMenuButton(Player player, int index) {
		return access.evaluate((level, pos) ->{
			BlockEntity be = level.getBlockEntity(pos);
			if(be instanceof TileHeatExchange tile) {
				Tank tank= tile.getSectionTank(index/2);
				if(tank!=null)
					tank.onGuiClicked(this, player);
			}
			return false;
		}).orElse(false);
	}
	
	

}
