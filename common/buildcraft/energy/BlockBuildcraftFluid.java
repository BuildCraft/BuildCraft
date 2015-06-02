/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy;

import java.util.Random;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.core.fluids.FluidHelper;
import buildcraft.core.utils.IModelRegister;
import buildcraft.energy.render.EntityDropParticleFX;

public class BlockBuildcraftFluid extends BlockFluidClassic implements IModelRegister {

	protected float particleRed;
	protected float particleGreen;
	protected float particleBlue;
	protected boolean flammable;
	protected int flammability = 0;
	private MapColor mapColor;

	public BlockBuildcraftFluid(Fluid fluid, Material material, MapColor iMapColor) {
		super(fluid, material);

		mapColor = iMapColor;
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);
		if (!(world instanceof World))
			return;

		World worldobj = (World) world;
		if (flammable && worldobj.provider.getDimensionId() == -1) {
			worldobj.newExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 4F, true, true);
			worldobj.setBlockToAir(pos);
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
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return flammable ? 300 : 0;
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return flammability;
	}

	@Override
	public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return flammable;
	}

	@Override
	public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {
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
	public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
		super.randomDisplayTick(world, pos, state, rand);

		if (rand.nextInt(10) == 0 && World.doesBlockHaveSolidTopSurface(world, pos.down())
			&& !world.getBlockState(pos.down(2)).getBlock().getMaterial().blocksMovement()) {

			double px = pos.getX() + rand.nextFloat();
			double py = pos.getY() - 1.05D;
			double pz = pos.getZ() + rand.nextFloat();

			EntityFX fx = new EntityDropParticleFX(world, px, py, pz, particleRed, particleGreen, particleBlue);
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(fx);
		}
	}

	@Override
	public boolean canDisplace(IBlockAccess world, BlockPos pos) {
		if (world.getBlockState(pos).getBlock().getMaterial().isLiquid()) {
			return false;
		}
		return super.canDisplace(world, pos);
	}

	@Override
	public boolean displaceIfPossible(World world, BlockPos pos) {
		if (world.getBlockState(pos).getBlock().getMaterial().isLiquid()) {
			return false;
		}
		return super.displaceIfPossible(world, pos);
	}

	@Override
	public MapColor getMapColor(IBlockState state) {
		return mapColor;
	}

	// TODO: Remove when Forge fixes BlockStates
	@Override
	protected BlockState createBlockState() {
		return FluidHelper.createBlockState(this);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((Integer) state.getValue(LEVEL)).intValue();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getBlockState().getBaseState().withProperty(LEVEL, meta);
	}

	@Override
	protected void flowIntoBlock(World world, BlockPos pos, int meta) {
		if (meta < 0)
			return;
		if (displaceIfPossible(world, pos)) {
			world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, meta), 3);
		}
	}

	// Rendering methods
	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side) {
		if (side == EnumFacing.UP)
			return world.getBlockState(pos).getBlock() != this;
		else
			return super.shouldSideBeRendered(world, pos, side);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return FluidHelper.getExtendedState(state, world, pos);
	}

	@Override
	public void registerModels() {}
}
