/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.utils.Utils;

public class BlockFiller extends BlockBuildCraft {

	public IFillerPattern currentPattern;
	/*private IIcon textureSides;
	private IIcon textureTopOn;
	private IIcon textureTopOff;*/

	public BlockFiller() {
		super(Material.iron);

		setHardness(5F);
		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float hitX, float hitY, float hitZ) {

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		BlockInteractionEvent event = new BlockInteractionEvent(entityplayer, pos, state);
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return false;
		}

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.FILLER, world, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;

	}

	/*@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		int m = world.getBlockMetadata(x, y, z);
		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile != null && tile instanceof TileFiller) {
			TileFiller filler = (TileFiller) tile;
			if (side == 1 || side == 0) {
				if (!filler.hasWork()) {
					return textureTopOff;
				} else {
					return textureTopOn;
				}
			} else if (filler.currentPattern != null) {
				return filler.currentPattern.getIcon();
			} else {
				return textureSides;
			}
		}

		return getIcon(side, m);
	}

	@Override
	public IIcon getIcon(int i, int j) {
		if (i == 0 || i == 1) {
			return textureTopOn;
		} else {
			return textureSides;
		}
	}*/

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileFiller();
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		Utils.preDestroyBlock(world, pos, state);
		super.breakBlock(world, pos, state);
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
	    textureTopOn = par1IconRegister.registerIcon("buildcraft:blockFillerTopOn");
        textureTopOff = par1IconRegister.registerIcon("buildcraft:blockFillerTopOff");
        textureSides = par1IconRegister.registerIcon("buildcraft:blockFillerSides");
	}*/


	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos) {
		return 1;
	}
}
