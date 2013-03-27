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

	@SideOnly(Side.CLIENT)
	private Icon[] field_94425_a;

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
	
	@Override
    	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister){
		this.field_94425_a = new Icon[] {iconRegister.registerIcon("buildcraft:oil"), iconRegister.registerIcon("buildcraft:oil_flow")};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTextureFromSideAndMetadata(int par1, int par2) {
		return par1 != 0 && par1 != 1 ? this.field_94425_a[1] : this.field_94425_a[0];
	}

}
