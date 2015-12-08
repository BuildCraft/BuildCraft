/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.tests.testcase;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSpriteRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.tests.BuildCraftTests;
import buildcraft.tests.GuiTestIds;

public class BlockTestCase extends BlockContainer {

	public BlockTestCase() {
		super(Material.ground);

		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileTestCase();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(TextureAtlasSpriteRegister par1IconRegister) {
		this.blockIcon = par1IconRegister.registerIcon("buildcraft:testcase");
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, EntityPlayer entityplayer, int par6, float par7,
			float par8, float par9) {

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftTests.instance, GuiTestIds.TESTCASE_ID, world, pos);
		}

		return true;
	}
}
