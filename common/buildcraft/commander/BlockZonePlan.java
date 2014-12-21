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
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.utils.Utils;

public class BlockZonePlan extends BlockBuildCraft {

	/*private IIcon blockTextureSide;
	private IIcon blockTextureFront;*/

	public BlockZonePlan() {
		super(Material.iron, new PropertyEnum[]{FACING_PROP});
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileZonePlan();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float hitX, float hitY, float hitZ) {

		BlockInteractionEvent event = new BlockInteractionEvent(entityplayer, pos, state);
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return false;
		}

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.MAP,
					world, pos.getX(), pos.getY(), pos.getZ());
		}

		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, entityliving, stack);

		EnumFacing orientation = Utils.get2dOrientation(entityliving);
		world.setBlockState(pos, state.withProperty(FACING_PROP, orientation.getOpposite()), 1);
	}

	/*@Override
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
	}*/

}
