/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import buildcraft.BuildCraftCore;

public class BlockBuildTool extends Block {
	public BlockBuildTool() {
		super(Material.iron);
	}

	@Override
	public void registerBlockIcons(IIconRegister itemRegister) {
	}

	@Override
	public IIcon getIcon(int i, int j) {
		return BuildCraftCore.redLaserTexture;
	}
}
