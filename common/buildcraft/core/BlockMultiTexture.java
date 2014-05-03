/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.core.render.IconFlipped;
import buildcraft.core.render.RenderBlockMultiTexture;

/**
 * This whole class may need tweaking
 */
public abstract class BlockMultiTexture extends BlockBuildCraft {

	private static Map<String, IIcon> iconMap = new HashMap<String, IIcon>();

	public BlockMultiTexture(Material material, CreativeTabBuildCraft tab) {
		super(material, tab);
	}

	public static int getUVTopForFront(int front) {
		switch (front) {
			case 2:
				return 3;
			case 3:
				return 0;
			case 4:
				return 1;
			case 5:
				return 2;
			default:
				return 0;
		}
	}

	public abstract int getFrontSide(IBlockAccess world, int x, int y, int z);

	public abstract String getIconPrefix();

	//TODO Remove once we get past 1.7.2! Bug #37106
	public boolean shouldFlipU(IBlockAccess world, int x, int y, int z, int side) {
		int front = getFrontSide(world, x, y, z);
		return (front == 4 && side == 2) || (front == 5 && side == 5) || (front == 5 && side == 2) || (front == 2 && side == 5) || (front == 2 && side == 2) || (front == 3 && side == 5) || shouldFlipU(side, getFrontSide(world, x, y, z));
	}

	public boolean shouldFlipV(IBlockAccess world, int x, int y, int z, int side) {
		return shouldFlipV(side, getFrontSide(world, x, y, z));
	}

	public boolean shouldFlipU(int side, int front) {
		return front == 3;
	}

	public boolean shouldFlipV(int side, int front) {
		return false;
	}

	public String getIconPostfix(IBlockAccess world, int x, int y, int z, int side) {
		return getIconPostfix(side, getFrontSide(world, x, y, z));
	}

	public String getIconPostfix(int side, int front) {
		ForgeDirection forgeSide = ForgeDirection.getOrientation(side);
		ForgeDirection forgeFront = ForgeDirection.getOrientation(front);
		ForgeDirection forgeBack = forgeFront.getOpposite();
		ForgeDirection forgeLeft = forgeFront.getRotation(ForgeDirection.UP).getOpposite();
		ForgeDirection forgeRight = forgeFront.getRotation(ForgeDirection.UP);

		// This would be a switch statement if it could be... :(
		if (forgeSide == forgeFront) {
			return "front";
		} else if (forgeSide == forgeLeft) {
			return "leftSide";
		} else if (forgeSide == forgeRight) {
			return "rightSide";
		} else if (forgeSide == forgeBack) {
			return "back";
		} else if (forgeSide == ForgeDirection.UP) {
			return "top";
		} else if (forgeSide == ForgeDirection.DOWN) {
			return "bottom";
		}

		// If all else fails
		return "front";
	}

	private String getIconName(int side) {
		return getIconName(side, ForgeDirection.WEST.ordinal());
	}

	private String getIconName(int side, int front) {
		return getIconPrefix() + getIconPostfix(side, front);
	}

	@Override
	public int getRenderType() {
		return RenderBlockMultiTexture.renderID;
	}

	@Override
	public void registerBlockIcons(IIconRegister register) {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			String name = getIconName(i);
			iconMap.put(name, register.registerIcon("buildcraft:" + name));
		}
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		IconFlipped icon = new IconFlipped(iconMap.get(getIconName(side, ForgeDirection.SOUTH.ordinal())));
		icon.flipU(shouldFlipU(side, ForgeDirection.SOUTH.ordinal()));
		icon.flipV(shouldFlipV(side, ForgeDirection.SOUTH.ordinal()));
		return icon;
	}

	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		IconFlipped icon = new IconFlipped(iconMap.get(getIconName(side, getFrontSide(world, x, y, z))));
		icon.flipU(shouldFlipU(world, x, y, z, side));
		icon.flipV(shouldFlipV(world, x, y, z, side));
		return icon;
	}

}
