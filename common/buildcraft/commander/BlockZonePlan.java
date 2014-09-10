/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

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

import buildcraft.BuildCraftBuilders;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.utils.Utils;

public class BlockZonePlan extends BlockBuildCraft {

	private IIcon blockTextureSide;
	private IIcon blockTextureFront;

	public BlockZonePlan() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileZonePlan();
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
			entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.MAP,
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
		blockTextureSide = par1IconRegister.registerIcon("buildcraft:zonePlan_side");
		blockTextureFront = par1IconRegister.registerIcon("buildcraft:zonePlan_front");
	}

	@Override
	public IIcon getIcon(int i, int j) {
		if (j == 0 && i == 3) {
			return blockTextureFront;
		}

		if (i == j) {
			return blockTextureFront;
		}

		return blockTextureSide;
	}

}
