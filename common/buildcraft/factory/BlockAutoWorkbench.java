/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.core.BlockBuildcraftEureka;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import eureka.api.EurekaKnowledge;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockAutoWorkbench extends BlockBuildcraftEureka {

	IIcon topTexture;
	IIcon sideTexture;

	public BlockAutoWorkbench() {
		super(Material.wood, "autoWorkbench");
		setHardness(3.0F);
	}

	@Override
	public IIcon getIcon(int i, int j) {
		if (i == 1 || i == 0) {
			return topTexture;
		} else {
			return sideTexture;
		}
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9);

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		if (entityplayer.getCurrentEquippedItem() != null) {
			if (entityplayer.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
				return false;
			}
		}
		// Drop though if the player doesn't hsa the research
		if (!EurekaKnowledge.isFinished(entityplayer, "autoWorkbench"))
			return false;

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftFactory.instance, GuiIds.AUTO_CRAFTING_TABLE, world, i, j, k);
		}

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileAutoWorkbench();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
	    topTexture = par1IconRegister.registerIcon("buildcraft:autoWorkbench_top");
	    sideTexture = par1IconRegister.registerIcon("buildcraft:autoWorkbench_side");
	}

	@Override
	public ItemStack[] getComponents() {
		return new ItemStack[]{new ItemStack(Blocks.crafting_table), new ItemStack(BuildCraftCore.woodenGearItem, 4)};
	}
}
