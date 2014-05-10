/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.BuildCraftBuilders;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;

public class BlockBlueprintLibrary extends BlockContainer {

	private IIcon textureTop;
    private IIcon textureSide;

    public BlockBlueprintLibrary() {
		super(Material.wood);
		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
		setHardness(5F);
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9);

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		TileEntity tile = world.getTileEntity(i, j, k);
		if (tile instanceof TileBlueprintLibrary) {
			TileBlueprintLibrary tileBlueprint = (TileBlueprintLibrary) tile;

			if (!world.isRemote) {
				entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.BLUEPRINT_LIBRARY, world, i, j, k);
			}
		}

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileBlueprintLibrary();
	}

	@Override
	public IIcon getIcon(int i, int j) {
		switch (i) {
		case 0:
		case 1:
			return textureTop;
		default:
			return textureSide;
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		if (!world.isRemote && entityliving instanceof EntityPlayer) {
			TileEntity tile = world.getTileEntity(i, j, k);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
	    textureTop = par1IconRegister.registerIcon("buildcraft:library_topbottom");
        textureSide = par1IconRegister.registerIcon("buildcraft:library_side");
	}
}
