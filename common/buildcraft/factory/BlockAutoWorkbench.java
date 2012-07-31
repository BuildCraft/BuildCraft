/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import java.util.ArrayList;

import buildcraft.mod_BuildCraftFactory;
import buildcraft.api.APIProxy;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockAutoWorkbench extends BlockBuildCraft {

	int topTexture;
	int sideTexture;

	public BlockAutoWorkbench(int i) {
		super(i, Material.wood);
		topTexture = 2 * 16 + 11;
		sideTexture = 2 * 16 + 12;
		setHardness(1.0F);
	}

	@Override
	public int getBlockTextureFromSide(int i) {
		if (i == 1 || i == 0) {
			return topTexture;
		} else {
			return sideTexture;
		}
	}

	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		super.blockActivated(world, i, j, k, entityplayer);

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking())
			return false;

		if (entityplayer.getCurrentEquippedItem() != null) {
			if (entityplayer.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
				return false;
			}
		}

		if (!APIProxy.isClient(world))
			entityplayer.openGui(mod_BuildCraftFactory.instance, GuiIds.AUTO_CRAFTING_TABLE, world, i, j, k);

		return true;
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileAutoWorkbench();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
