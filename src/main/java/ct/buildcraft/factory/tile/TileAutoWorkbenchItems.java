/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.factory.tile;

import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.factory.container.ContainerAutoCraftItems;
import ct.buildcraft.lib.gui.ItemProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileAutoWorkbenchItems extends TileAutoWorkbenchBase implements MenuProvider, Container{
    public TileAutoWorkbenchItems(BlockPos pos, BlockState state) {
        super(BCFactoryBlocks.ENTITYBLOCKAUTOBENCH.get(), pos, state, 3, 3);
    }

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new ContainerAutoCraftItems(id, inventory, invResult, invBlueprint, invMaterialFilter, 
				invMaterials, new ItemProvider(i -> resultClient, 1), ContainerLevelAccess.create(level, worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
	}

	@Override
	public void clearContent() {
	}

	@Override
	public int getContainerSize() {
		return 10;
	}

	@Override
	public boolean isEmpty() {
		return invMaterialFilter.isEmpty() && invResult.getStackInSlot(0).isEmpty();
	}

	@Override
	public ItemStack getItem(int index) {
		if(index == 0)
			return invResult.getStackInSlot(0);
		return invMaterialFilter.getStackInSlot(index-1);
	}

	@Override
	public ItemStack removeItem(int index, int num) {
		if(index == 0)
			return invResult.extractItem(0, num, false);
		return invMaterialFilter.extractItem(index-1, num, false);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		if(index == 0)
			return invResult.extractItem(0, 64, false);
		return invMaterialFilter.extractItem(index-1, 64, false);
	}

	@Override
	public void setItem(int index, ItemStack item) {
		if(index == 0)
			return;
		invMaterialFilter.setStackInSlot(index-1, item);
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack item) {
		return index == 0 ? false : true;
	}

	@Override
	public boolean stillValid(Player plyer) {
		return !this.remove;
	}
}
