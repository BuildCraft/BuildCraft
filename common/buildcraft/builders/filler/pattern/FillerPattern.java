/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.filler.pattern;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.core.IBox;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.filler.IPatternIterator;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.StringUtils;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public abstract class FillerPattern implements IFillerPattern {

	public static final Set<FillerPattern> patterns = new HashSet<FillerPattern>();
	private final String tag;
	private Icon icon;

	public FillerPattern(String tag) {
		this.tag = tag;
		patterns.add(this);
	}

	/**
	 * stackToPlace contains the next item that can be place in the world. Null
	 * if there is none. IteratePattern is responsible to decrementing the stack
	 * size if needed. Return true when the iteration process is finished.
	 */
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		return true;
	}

	@Override
	public String getDisplayName() {
		return StringUtils.localize("fillerpattern." + tag);
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:" + tag;
	}

	public void registerIcon(IconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:fillerPatterns/" + tag);
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String toString() {
		return "Pattern: " + getUniqueTag();
	}

	@Override
	public IPatternIterator createPatternIterator(TileEntity tile, IBox box, ForgeDirection orientation) {
		return new PatternIterator(tile, box);
	}

	protected class PatternIterator implements IPatternIterator {

		private final IBox box;
		private final TileEntity tile;

		public PatternIterator(TileEntity tile, IBox box) {
			this.box = box;
			this.tile = tile;
		}

		@Override
		public boolean iteratePattern(ItemStack stackToPlace) {
			return FillerPattern.this.iteratePattern(tile, box, stackToPlace);
		}
	}

	/**
	 * Attempt to fill blocks in the area.
	 *
	 * Return false if the process failed.
	 *
	 */
	public static boolean fill(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, ItemStack stackToPlace, World world) {
		boolean found = false;
		int lastX = 0, lastY = 0, lastZ = 0;

		for (int y = yMin; y <= yMax && !found; ++y) {
			for (int x = xMin; x <= xMax && !found; ++x) {
				for (int z = zMin; z <= zMax && !found; ++z) {
					if (!BlockUtil.canChangeBlock(world, x, y, z))
						return false;
					if (BlockUtil.isSoftBlock(world, x, y, z)) {
						lastX = x;
						lastY = y;
						lastZ = z;

						found = true;
					}
				}
			}
		}

		if (found && stackToPlace != null) {
			breakBlock(world, lastX, lastY, lastZ);
			stackToPlace.getItem().onItemUse(stackToPlace, CoreProxy.proxy.getBuildCraftPlayer(world), world, lastX, lastY - 1, lastZ, 1, 0.0f, 0.0f, 0.0f);
		}

		return found;
	}

	/**
	 * Attempt to remove the blocks in the area.
	 *
	 * Return false if is the process failed.
	 *
	 */
	public static boolean empty(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, World world) {
		int lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE, lastZ = Integer.MAX_VALUE;

		for (int y = yMax; y >= yMin; y--) {
			boolean found = false;
			for (int x = xMin; x <= xMax; ++x) {
				for (int z = zMin; z <= zMax; ++z) {
					if (!BlockUtil.canChangeBlock(world, x, y, z))
						return false;
					if (!BlockUtil.isSoftBlock(world, x, y, z)) {
						found = true;
						lastX = x;
						lastY = y;
						lastZ = z;
					}
				}
			}

			if (found) {
				break;
			}
		}

		if (lastX != Integer.MAX_VALUE) {
			breakBlock(world, lastX, lastY, lastZ);
			return true;
		}

		return false;
	}

	/**
	 * Attempt to fill the area defined by the box, from the top down.
	 *
	 * This differs from Fill in how it handles blockage and the order of
	 * iteration.
	 *
	 * Return false if is the process failed.
	 */
	public static boolean flatten(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, World world, ItemStack stackToPlace) {
		int lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE, lastZ = Integer.MAX_VALUE;

		boolean found = false;
		for (int x = xMin; x <= xMax && !found; ++x) {
			for (int z = zMin; z <= zMax && !found; ++z) {
				for (int y = yMax; y >= yMin; --y) {
					if (!BlockUtil.canChangeBlock(world, x, y, z) || !BlockUtil.isSoftBlock(world, x, y, z)) {
						break;
					} else {
						found = true;
						lastX = x;
						lastY = y;
						lastZ = z;
					}
				}
			}
		}

		if (found && stackToPlace != null) {
			breakBlock(world, lastX, lastY, lastZ);
			stackToPlace.getItem().onItemUse(stackToPlace, CoreProxy.proxy.getBuildCraftPlayer(world), world, lastX, lastY - 1, lastZ, 1, 0.0f, 0.0f, 0.0f);
		}
		return found;
	}

	private static void breakBlock(World world, int x, int y, int z) {
		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		if (block != null)
			world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
		if (BuildCraftBuilders.fillerDestroy) {
			world.setBlockToAir(x, y, z);
		} else if (BlockUtil.isToughBlock(world, x, y, z)) {
			BlockUtil.breakBlock(world, x, y, z, BuildCraftBuilders.fillerLifespanTough);
		} else {
			BlockUtil.breakBlock(world, x, y, z, BuildCraftBuilders.fillerLifespanNormal);
		}
	}
}
