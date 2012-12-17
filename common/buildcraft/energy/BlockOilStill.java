/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import net.minecraft.block.BlockStationary;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraftforge.liquids.ILiquid;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.core.DefaultProps;

public class BlockOilStill extends BlockStationary implements ILiquid {

	public BlockOilStill(int i, Material material) {
		super(i, material);

		setHardness(100F);
		setLightOpacity(3);
	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.oilModel;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int stillLiquidId() {
		return BuildCraftEnergy.oilStill.blockID;
	}

	@Override
	public boolean isMetaSensitive() {
		return false;
	}

	@Override
	public int stillLiquidMeta() {
		return 0;
	}

	@Override
	public boolean isBlockReplaceable(World world, int i, int j, int k) {
		return true;
	}

}
