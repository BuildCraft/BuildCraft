/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BlockHatched;

public class BlockQuarry extends BlockHatched {
	public BlockQuarry() {
		super(Material.iron);

		setHardness(10F);
		setResistance(10F);
		setStepSound(soundTypeAnvil);
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, i, j, k, entityliving, stack);
		if (entityliving instanceof EntityPlayer) {
			TileEntity tile = world.getTileEntity(i, j, k);
			if (tile instanceof TileQuarry) {
				((TileQuarry) tile).placedBy = (EntityPlayer) entityliving;
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileQuarry();
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		if (BuildCraftBuilders.quarryOneTimeUse) {
			return new ArrayList<ItemStack>();
		}
		return super.getDrops(world, x, y, z, metadata, fortune);
	}

	@Override
	public void breakBlock(World world, int i, int j, int k, Block block, int metadata) {
		if (world.isRemote) {
			return;
		}

		BuildCraftBuilders.frameBlock.removeNeighboringFrames(world, i, j, k);

		super.breakBlock(world, i, j, k, block, metadata);
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		if (super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9)) {
			return true;
		}

		TileQuarry tile = (TileQuarry) world.getTileEntity(i, j, k);

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		// Restart the quarry if its a wrench
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, i, j, k)) {

			tile.reinitalize();
			((IToolWrench) equipped).wrenchUsed(entityplayer, i, j, k);
			return true;

		}

		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return false;
	}
}
