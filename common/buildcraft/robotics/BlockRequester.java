/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.utils.Utils;

public class BlockRequester extends BlockBuildCraft {

	private IIcon blockTextureDefault;
	private IIcon blockTextureSide;

	public BlockRequester() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileRequester();
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7,
			float par8, float par9) {

		BlockInteractionEvent event = new BlockInteractionEvent(entityplayer, this);
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return false;
		}

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftRobotics.instance, GuiIds.REQUESTER,
					world, i, j, k);
		}

		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, i, j, k, entityliving, stack);

		ForgeDirection orientation = Utils.get2dOrientation(entityliving);

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal(), 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		blockTextureDefault = par1IconRegister.registerIcon("buildcraft:commander_side");
		blockTextureSide = par1IconRegister.registerIcon("buildcraft:requester_side");
	}

	@Override
	public IIcon getIcon(int i, int j) {
		if (i == 0 || i == 1) {
			return blockTextureDefault;
		} else {
			return blockTextureSide;
		}
	}

}
