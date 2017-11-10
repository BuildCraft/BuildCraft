/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import buildcraft.core.lib.render.EntityDropParticleFX;
import buildcraft.core.lib.utils.ResourceUtils;

public class BlockBuildCraftFluid extends BlockFluidClassic {

	protected float particleRed;
	protected float particleGreen;
	protected float particleBlue;
	@SideOnly(Side.CLIENT)
	protected IIcon[] theIcon;
	protected boolean flammable;
	protected boolean dense = false;
	protected int flammability = 0;
	private MapColor mapColor;

	public BlockBuildCraftFluid(Fluid fluid, Material material, MapColor iMapColor) {
		super(fluid, material);

		mapColor = iMapColor;
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return side != 0 && side != 1 ? this.theIcon[1] : this.theIcon[0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		String prefix = ResourceUtils.getObjectPrefix(Block.blockRegistry.getNameForObject(this));
		prefix = prefix.substring(0, prefix.indexOf(":") + 1) + "fluids/";
		this.theIcon = new IIcon[]{iconRegister.registerIcon(prefix + fluidName + "_still"),
				iconRegister.registerIcon(prefix + fluidName + "_flow")};
	}

	public static boolean isFluidExplosive(World world, int x, int z) {
		return world.provider.dimensionId == -1;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		if (flammable && isFluidExplosive(world, x, z)) {
			world.setBlock(x, y, z, Blocks.air, 0, 2); // Do not cause block updates!
			world.newExplosion(null, x, y, z, 4F, true, true);
		} else {
			super.updateTick(world, x, y, z, rand);
		}
	}

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		world.setBlock(x, y, z, Blocks.air, 0, 2); // Do not cause block updates!
		for (ForgeDirection fd : ForgeDirection.VALID_DIRECTIONS) {
			Block block = world.getBlock(x + fd.offsetX, y + fd.offsetY, z + fd.offsetZ);
			if (block instanceof BlockBuildCraftFluid) {
				world.scheduleBlockUpdate(x + fd.offsetX, y + fd.offsetY, z + fd.offsetZ, block, 2);
			} else {
				world.notifyBlockOfNeighborChange(x + fd.offsetX, y + fd.offsetY, z + fd.offsetZ, block);
			}
		}
		onBlockDestroyedByExplosion(world, x, y, z, explosion);
	}


	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		if (!dense || entity == null) {
			return;
		}

		entity.motionY = Math.min(0.0, entity.motionY);

		if (entity.motionY < -0.05) {
			entity.motionY *= 0.05;
		}

		entity.motionX = Math.max(-0.05, Math.min(0.05, entity.motionX * 0.05));
		entity.motionY -= 0.05;
		entity.motionZ = Math.max(-0.05, Math.min(0.05, entity.motionZ * 0.05));
	}

	public BlockBuildCraftFluid setDense(boolean dense) {
		this.dense = dense;
		return this;
	}

	public BlockBuildCraftFluid setFlammable(boolean flammable) {
		this.flammable = flammable;
		return this;
	}

	public BlockBuildCraftFluid setFlammability(int flammability) {
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

	public BlockBuildCraftFluid setParticleColor(float particleRed, float particleGreen, float particleBlue) {
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

	@Override
	public MapColor getMapColor(int meta) {
		return mapColor;
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}
}
