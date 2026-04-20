package ct.buildcraft.energy.client.gui;

import ct.buildcraft.core.BCCoreBlocks;
import ct.buildcraft.energy.BCEnergyGuis;
import ct.buildcraft.energy.tile.TileEngineIron_BC8;
import ct.buildcraft.lib.fluid.Tank;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class $MenuEngineIron_BC8 extends AbstractContainerMenu {

	private final ContainerLevelAccess access;
	protected final ContainerData data;
	
	public $MenuEngineIron_BC8(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new SimpleContainerData(6), ContainerLevelAccess.NULL);
	}
	
	public $MenuEngineIron_BC8(int containerId, Inventory playerInventory, ContainerData data, ContainerLevelAccess access) {
		super(BCEnergyGuis.MENU_IRON.get(), containerId);
		this.access = access;
		this.data = data;
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 95 + i * 18));
			}
		}
		  
		for(int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 153));
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

	@Override
	public boolean clickMenuButton(Player player, int index) {
		return access.evaluate((level, pos) ->{
			BlockEntity be = level.getBlockEntity(pos);
			if(be instanceof TileEngineIron_BC8 engine) {
				Tank tank= engine.tankManager.get(index/2);
				if(tank!=null)
					tank.onGuiClicked(this, player);
			}
			return false;
		}).orElse(false);
		
	}
	
	
}
