/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import net.minecraft.block.BlockStationary;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquid;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	public void registerIcons(IconRegister iconRegister) {
		this.theIcon = new Icon[]{iconRegister.registerIcon("buildcraft:oil"), iconRegister.registerIcon("buildcraft:oil_flow")};
	}

	@Override
	public int getFireSpreadSpeed(World world, int x, int y, int z, int metadata, ForgeDirection face) {
		return 300;
	}

	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, int metadata, ForgeDirection face) {
		return 1;
	}

	@Override
	public boolean isFlammable(IBlockAccess world, int x, int y, int z, int metadata, ForgeDirection face) {
		return true;
	}

	@Override
	public boolean isFireSource(World world, int x, int y, int z, int metadata, ForgeDirection side) {
		return true;
	}
}
