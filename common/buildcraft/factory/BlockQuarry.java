/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.BuildCraftFactory;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.utils.Utils;

public class BlockQuarry extends BlockBuildCraft {

	/*IIcon textureTop;
	IIcon textureFront;
	IIcon textureSide;*/

	public BlockQuarry() {
		super(Material.iron, new PropertyEnum[]{FACING_6_PROP});

		setHardness(10F);
		setResistance(10F);
		setDefaultState(getDefaultState().withProperty(FACING_PROP, EnumFacing.NORTH));
		// TODO: set proper sound
		//setStepSound(soundAnvilFootstep);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, entityliving, stack);

		EnumFacing orientation = Utils.get2dOrientation(entityliving);

		//TODO: Check if that is correct
		world.setBlockState(pos, state.withProperty(FACING_PROP, orientation.getOpposite()), 1);
		if (entityliving instanceof EntityPlayer) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileQuarry) {
				((TileQuarry) tile).placedBy = (EntityPlayer) entityliving;
			}
		}
	}

	/*@Override
	public IIcon getIcon(int i, int j) {
		// If no metadata is set, then this is an icon.
		if (j == 0 && i == 3) {
			return textureFront;
		}

		if (i == j && i > 1) {
			return textureFront;
		}

		switch (i) {
			case 1:
				return textureTop;
			default:
				return textureSide;
		}
	}*/

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileQuarry();
	}

	public void searchFrames(World world, BlockPos pos) {
		int width2 = 1;
		if (!Utils.checkChunksExist(world, pos.getX() - width2, pos.getY() - width2, pos.getZ() - width2, pos.getX() + width2, pos.getY() + width2, pos.getZ() + width2)) {
			return;
		}

		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (block != BuildCraftFactory.frameBlock) {
			return;
		}

		int meta = ((EnumFacing)state.getValue(FACING_PROP)).getIndex();

		if ((meta & 8) == 0) {
			world.setBlockState(pos, state.withProperty(FACING_PROP, EnumFacing.getFront(meta | 8)), 0);
			
			EnumFacing[] dirs = EnumFacing.values();

			for (EnumFacing dir : dirs) {
				searchFrames(world, pos.offset(dir));
			}
		}
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		if (BuildCraftFactory.quarryOneTimeUse) {
			return new ArrayList<ItemStack>();
		}
		return super.getDrops(world, pos, state, fortune);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (world.isRemote) {
			return;
		}

		BuildCraftFactory.frameBlock.removeNeighboringFrames(world, pos);

		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileQuarry tile = (TileQuarry) world.getTileEntity(pos);

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		// Restart the quarry if its a wrench
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, pos)) {

			tile.reinitalize();
			((IToolWrench) equipped).wrenchUsed(entityplayer, pos);
			return true;

		}

		return false;
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		textureSide = par1IconRegister.registerIcon("buildcraft:quarry_side");
		textureTop = par1IconRegister.registerIcon("buildcraft:quarry_top");
		textureFront = par1IconRegister.registerIcon("buildcraft:quarry_front");
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
