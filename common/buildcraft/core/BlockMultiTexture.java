/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import buildcraft.core.render.IconFlipped;
import buildcraft.core.render.RenderBlockMultiTexture;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashMap;
import java.util.Map;

/**
 * This whole class may need tweaking
 */
public abstract class BlockMultiTexture extends BlockBuildCraft {

	private static Map<String, IIcon> iconMap = new HashMap<String, IIcon>();

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

	public BlockMultiTexture(Material material) {
		super(material);
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
		ForgeDirection forge_side = ForgeDirection.getOrientation(side);
		ForgeDirection forge_front = ForgeDirection.getOrientation(front);
		ForgeDirection forge_back = forge_front.getOpposite();
		ForgeDirection forge_left = forge_front.getRotation(ForgeDirection.UP).getOpposite();
		ForgeDirection forge_right = forge_front.getRotation(ForgeDirection.UP);

		// This would be a switch statement if it could be... :(
		if (forge_side == forge_front) {
			return "front";
		} else if (forge_side == forge_left) {
			return "leftSide";
		} else if (forge_side == forge_right) {
			return "rightSide";
		} else if (forge_side == forge_back) {
			return "back";
		} else if (forge_side == ForgeDirection.UP) {
			return "top";
		} else if (forge_side == ForgeDirection.DOWN) {
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
