package ct.buildcraft.builders.gui;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.builders.BCBuildersGuis;
import ct.buildcraft.builders.tile.TileBuilder;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MenuBuilder extends AbstractContainerMenu {

	protected final ContainerLevelAccess access;
	protected final ContainerData data;
	
	public MenuBuilder(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new ItemStackHandler(1), new ItemStackHandler(27), new SimpleContainerData(8), new ItemStackHandler(24) ,ContainerLevelAccess.NULL);
	}
	
	public MenuBuilder(int containerId, Inventory playerInventory, IItemHandler blueprint, IItemHandler resources, ContainerData data, 
			 IItemHandler require, ContainerLevelAccess access) {
		super(BCBuildersGuis.MENU_BUILDER.get(), containerId);
		this.access = access;
		this.data = data;
		for(int i = 0; i < 3; ++i) 
			for(int j = 0; j < 9; ++j) 
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, -32 + j * 18, 107 + i * 18));
		
		for(int k = 0; k < 9; ++k) 
			this.addSlot(new Slot(playerInventory, k, -32 + k * 18, 165));
		this.addSlot(new SlotItemHandler(blueprint, 0 ,40, -6));
		for(int i = 0; i < 3; ++i) 
			for(int j = 0; j < 9; ++j) 
				this.addSlot(new SlotItemHandler(resources, j+i*9 , -32 +j*18, 39+i*18 ));
        for(int y = 0; y < 6; y++) {
            for(int x = 0; x < 4; x++) {
                this.addSlot(new SlotItemHandler(require, x + y * 4, 179 + x * 18, 18 + y * 18));
            }
        }
		this.addDataSlots(data);
		
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
		return super.stillValid(this.access, player, BCBuildersBlocks.BUILDER.get());
	}

	@Override
	public boolean clickMenuButton(Player player, int index) {
		return access.evaluate((level, pos) ->{
			BlockEntity be = level.getBlockEntity(pos);
			if(be instanceof TileBuilder tile) {
				Tank tank= tile.tankManager.get(index/2);
				int amount0 = tank.getFluidAmount();
				if(tank!=null)
					tank.onGuiClicked(this, player);
				if(amount0 != tank.getFluidAmount())
					tile.markChunkDirty();
			}
			return true;
		}).orElse(false);
		
	}

	

}