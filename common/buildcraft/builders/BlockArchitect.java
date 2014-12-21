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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.utils.Utils;

public class BlockArchitect extends BlockBuildCraft {

	public BlockArchitect() {
		super(Material.iron, CreativeTabBuildCraft.BLOCKS);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileArchitect();
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

		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, pos)) {

			//TOOD: Need test
			switch (side) {
			case WEST:
				world.setBlockState(pos, state.withProperty(FACING_PROP, EnumFacing.SOUTH), 0);
				break;
			case EAST:
				world.setBlockState(pos, state.withProperty(FACING_PROP, EnumFacing.NORTH), 0);
				break;
			case NORTH:
				world.setBlockState(pos, state.withProperty(FACING_PROP, EnumFacing.WEST), 0);
				break;
			case SOUTH:
			default:
				world.setBlockState(pos, state.withProperty(FACING_PROP, EnumFacing.EAST), 0);
				break;
			}

			world.markBlockForUpdate(pos);
			((IToolWrench) equipped).wrenchUsed(entityplayer, pos);
			return true;
		} else if (equipped instanceof ItemConstructionMarker) {
			ItemConstructionMarker.link(entityplayer.getCurrentEquippedItem(), world, pos);

			return true;
		} else {

			if (!world.isRemote) {
				entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.ARCHITECT_TABLE, world, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;

		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		Utils.preDestroyBlock(world, pos, state);

		super.breakBlock(world, pos, state);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, entityliving, stack);

		EnumFacing orientation = Utils.get2dOrientation(entityliving);

		//TODO: Check if that is correct
		world.setBlockState(pos, state.withProperty(FACING_PROP, orientation.getOpposite()), 1);
	}

	@Override
	public boolean isNormalCube() {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos) {
		return 1;
	}

	/* MULTI TEXTURE */
	/*@Override
	public String getIconPrefix() {
		return "architect_";
	}

	@Override
	public int getFrontSide(IBlockAccess world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z);
	}*/

}
