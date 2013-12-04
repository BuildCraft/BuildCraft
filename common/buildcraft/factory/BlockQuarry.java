/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import buildcraft.BuildCraftFactory;
import buildcraft.api.core.Position;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.Box;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockQuarry extends BlockBuildCraft {

	Icon textureTop;
	Icon textureFront;
	Icon textureSide;

	public BlockQuarry(int i) {
		super(i, Material.iron);

		setHardness(10F);
		setResistance(10F);
		setStepSound(soundAnvilFootstep);
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, i, j, k, entityliving, stack);

		ForgeDirection orientation = Utils.get2dOrientation(new Position(entityliving.posX, entityliving.posY, entityliving.posZ), new Position(i, j, k));

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal(), 1);
		if (entityliving instanceof EntityPlayer) {
			TileQuarry tq = (TileQuarry) world.getBlockTileEntity(i, j, k);
			tq.placedBy = (EntityPlayer) entityliving;
		}
	}

	@Override
	public Icon getIcon(int i, int j) {
		// If no metadata is set, then this is an icon.
		if (j == 0 && i == 3)
			return textureFront;

		if (i == j && i > 1) // Front can't be top or bottom.
			return textureFront;

		switch (i) {
			case 1:
				return textureTop;
			default:
				return textureSide;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileQuarry();
	}

	public void searchFrames(World world, int i, int j, int k) {
		int width2 = 1;
		if (!world.checkChunksExist(i - width2, j - width2, k - width2, i + width2, j + width2, k + width2))
			return;

		int blockID = world.getBlockId(i, j, k);

		if (blockID != BuildCraftFactory.frameBlock.blockID)
			return;

		int meta = world.getBlockMetadata(i, j, k);

		if ((meta & 8) == 0) {
			world.setBlockMetadataWithNotify(i, j, k, meta | 8, 0);

			ForgeDirection[] dirs = ForgeDirection.VALID_DIRECTIONS;

			for (ForgeDirection dir : dirs) {
				switch (dir) {
					case UP:
						searchFrames(world, i, j + 1, k);
					case DOWN:
						searchFrames(world, i, j - 1, k);
					case SOUTH:
						searchFrames(world, i, j, k + 1);
					case NORTH:
						searchFrames(world, i, j, k - 1);
					case EAST:
						searchFrames(world, i + 1, j, k);
					case WEST:
					default:
						searchFrames(world, i - 1, j, k);
				}
			}
		}
	}

	private void markFrameForDecay(World world, int x, int y, int z) {
		if (world.getBlockId(x, y, z) == BuildCraftFactory.frameBlock.blockID) {
			world.setBlockMetadataWithNotify(x, y, z, 1, 0);
		}
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		if (BuildCraftFactory.quarryOneTimeUse) {
			return new ArrayList<ItemStack>();
		}
		return super.getBlockDropped(world, x, y, z, metadata, fortune);
	}

	@Override
	public void breakBlock(World world, int i, int j, int k, int par5, int par6) {

		if (!CoreProxy.proxy.isSimulating(world))
			return;

		TileEntity tile = world.getBlockTileEntity(i, j, k);
		if (tile instanceof TileQuarry) {
			TileQuarry quarry = (TileQuarry) tile;
			Box box = quarry.box;
			if (box.isInitialized() && Integer.MAX_VALUE != box.xMax) {
				// X - Axis
				for (int x = box.xMin; x <= box.xMax; x++) {
					markFrameForDecay(world, x, box.yMin, box.zMin);
					markFrameForDecay(world, x, box.yMax, box.zMin);
					markFrameForDecay(world, x, box.yMin, box.zMax);
					markFrameForDecay(world, x, box.yMax, box.zMax);
				}

				// Z - Axis
				for (int z = box.zMin + 1; z <= box.zMax - 1; z++) {
					markFrameForDecay(world, box.xMin, box.yMin, z);
					markFrameForDecay(world, box.xMax, box.yMin, z);
					markFrameForDecay(world, box.xMin, box.yMax, z);
					markFrameForDecay(world, box.xMax, box.yMax, z);
				}

				// Y - Axis
				for (int y = box.yMin + 1; y <= box.yMax - 1; y++) {

					markFrameForDecay(world, box.xMin, y, box.zMin);
					markFrameForDecay(world, box.xMax, y, box.zMin);
					markFrameForDecay(world, box.xMin, y, box.zMax);
					markFrameForDecay(world, box.xMax, y, box.zMax);
				}
			}
			quarry.destroy();
		}

		Utils.preDestroyBlock(world, i, j, k);

		// byte width = 1;
		// int width2 = width + 1;
		//
		// if (world.checkChunksExist(i - width2, j - width2, k - width2, i + width2, j + width2, k + width2)) {
		//
		// boolean frameFound = false;
		// for (int z = -width; z <= width; ++z) {
		//
		// for (int y = -width; y <= width; ++y) {
		//
		// for (int x = -width; x <= width; ++x) {
		//
		// int blockID = world.getBlockId(i + z, j + y, k + x);
		//
		// if (blockID == BuildCraftFactory.frameBlock.blockID) {
		// searchFrames(world, i + z, j + y, k + x);
		// frameFound = true;
		// break;
		// }
		// }
		// if (frameFound)
		// break;
		// }
		// if (frameFound)
		// break;
		// }
		// }

		super.breakBlock(world, i, j, k, par5, par6);
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		TileQuarry tile = (TileQuarry) world.getBlockTileEntity(i, j, k);

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking())
			return false;

		// Restart the quarry if its a wrench
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, i, j, k)) {

			tile.reinitalize();
			((IToolWrench) equipped).wrenchUsed(entityplayer, i, j, k);
			return true;

		}

		return false;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		textureSide = par1IconRegister.registerIcon("buildcraft:quarry_side");
		textureTop = par1IconRegister.registerIcon("buildcraft:quarry_top");
		textureFront = par1IconRegister.registerIcon("buildcraft:quarry_front");
	}
}
