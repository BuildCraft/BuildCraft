/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

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
import buildcraft.BuildCraftFactory;
import buildcraft.api.tools.IToolWrench;

public class BlockQuarry extends BlockLEDHatchBase {
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

	public void searchFrames(World world, int i, int j, int k) {
		int width2 = 1;
		if (!world.checkChunksExist(i - width2, j - width2, k - width2, i + width2, j + width2, k + width2)) {
			return;
		}

		Block block = world.getBlock(i, j, k);

		if (block != BuildCraftFactory.frameBlock) {
			return;
		}

		int meta = world.getBlockMetadata(i, j, k);

		if ((meta & 8) == 0) {
			world.setBlockMetadataWithNotify(i, j, k, meta | 8, 0);

			ForgeDirection[] dirs = ForgeDirection.VALID_DIRECTIONS;

			for (ForgeDirection dir : dirs) {
				switch (dir) {
				case UP:
						searchFrames(world, i, j + 1, k);
					break;
				case DOWN:
						searchFrames(world, i, j - 1, k);
					break;
				case SOUTH:
						searchFrames(world, i, j, k + 1);
					break;
				case NORTH:
						searchFrames(world, i, j, k - 1);
					break;
				case EAST:
						searchFrames(world, i + 1, j, k);
					break;
				case WEST:
					default:
						searchFrames(world, i - 1, j, k);
					break;
				}
			}
		}
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		if (BuildCraftFactory.quarryOneTimeUse) {
			return new ArrayList<ItemStack>();
		}
		return super.getDrops(world, x, y, z, metadata, fortune);
	}

	@Override
	public void breakBlock(World world, int i, int j, int k, Block block, int metadata) {
		if (world.isRemote) {
			return;
		}

		BuildCraftFactory.frameBlock.removeNeighboringFrames(world, i, j, k);

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

	@Override
	public int getIconGlowLevel(IBlockAccess access, int x, int y, int z) {
		if (renderPass < 2) {
			return -1;
		} else {
			TileQuarry tile = (TileQuarry) access.getTileEntity(x, y, z);
			return tile.getIconGlowLevel(renderPass);
		}
	}
}
