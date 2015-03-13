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
	public BlockRequester() {
		super(Material.iron);
		setRotatable(true);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileRequester();
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7,
			float par8, float par9) {
		if (super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9)) {
			return true;
		}

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftRobotics.instance, GuiIds.REQUESTER,
					world, i, j, k);
		}

		return true;
	}
}
