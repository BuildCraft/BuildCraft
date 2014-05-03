/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import buildcraft.energy.render.EntityDropParticleFX;

public class BlockBuildcraftFluid extends BlockFluidClassic {

	protected float particleRed;
	protected float particleGreen;
	protected float particleBlue;
	@SideOnly(Side.CLIENT)
	protected IIcon[] theIcon;
	protected boolean flammable;
	protected int flammability = 0;

	public BlockBuildcraftFluid(Fluid fluid, Material material) {
		super(fluid, material);
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return side != 0 && side != 1 ? this.theIcon[1] : this.theIcon[0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.theIcon = new IIcon[] {iconRegister.registerIcon("buildcraft:" + fluidName + "_still"),
				iconRegister.registerIcon("buildcraft:" + fluidName + "_flow")};
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		if (flammable && world.provider.dimensionId == -1) {
			world.newExplosion(null, x, y, z, 4F, true, true);
			world.setBlockToAir(x, y, z);
		}
	}

	public BlockBuildcraftFluid setFlammable(boolean flammable) {
		this.flammable = flammable;
		return this;
	}

	public BlockBuildcraftFluid setFlammability(int flammability) {
		this.flammability = flammability;
		return this;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return flammable ? 300 : 0;
	}

	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return flammability;
	}

	@Override
	public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return flammable;
	}

	@Override
	public boolean isFireSource(World world, int x, int y, int z, ForgeDirection side) {
		return flammable && flammability == 0;
	}

	public BlockBuildcraftFluid setParticleColor(float particleRed, float particleGreen, float particleBlue) {
		this.particleRed = particleRed;
		this.particleGreen = particleGreen;
		this.particleBlue = particleBlue;
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		super.randomDisplayTick(world, x, y, z, rand);

		if (rand.nextInt(10) == 0
				&& World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)
				&& !world.getBlock(x, y - 2, z).getMaterial().blocksMovement()) {

			double px = x + rand.nextFloat();
			double py = y - 1.05D;
			double pz = z + rand.nextFloat();

			EntityFX fx = new EntityDropParticleFX(world, px, py, pz, particleRed, particleGreen, particleBlue);
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(fx);
		}
	}

	@Override
	public boolean canDisplace(IBlockAccess world, int x, int y, int z) {
		if (world.getBlock(x, y, z).getMaterial().isLiquid()) {
			return false;
		}
		return super.canDisplace(world, x, y, z);
	}

	@Override
	public boolean displaceIfPossible(World world, int x, int y, int z) {
		if (world.getBlock(x, y, z).getMaterial().isLiquid()) {
			return false;
		}
		return super.displaceIfPossible(world, x, y, z);
	}
}
