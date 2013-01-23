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

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.BuildCraftFactory;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;
import buildcraft.core.proxy.CoreProxy;

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
		if (i == 1 || i == 0)
			return topTexture;
		else
			return sideTexture;
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9);

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking())
			return false;

		if (entityplayer.getCurrentEquippedItem() != null) {
			if (entityplayer.getCurrentEquippedItem().getItem() instanceof IItemPipe)
				return false;
		}

		if (!CoreProxy.proxy.isRenderWorld(world)) {
			entityplayer.openGui(BuildCraftFactory.instance, GuiIds.AUTO_CRAFTING_TABLE, world, i, j, k);
		}

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileAutoWorkbench();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
