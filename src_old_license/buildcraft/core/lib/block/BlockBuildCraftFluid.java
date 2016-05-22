/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.block;

import buildcraft.core.lib.client.render.EntityDropParticleFX;
import buildcraft.core.lib.utils.ICustomStateMapper;
import buildcraft.core.lib.utils.ModelHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockBuildCraftFluid extends BlockFluidClassic implements ICustomStateMapper {

    protected float particleRed;
    protected float particleGreen;
    protected float particleBlue;
    protected boolean flammable;
    protected boolean dense = false;
    protected int flammability = 0;

    public BlockBuildCraftFluid(Fluid fluid, Material material) {
        super(fluid, material);
        int colour = fluid.getColor();
        setParticleColor(colour >> 16 & 0xFF, colour >> 8 & 0xFF, colour & 0xFF);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        meta = (meta % 16 + 16) % 16;
        return getDefaultState().withProperty(LEVEL, meta);
    }

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (flammable && world.provider.getDimension() == -1) {
			world.setBlockToAir(pos);
			world.newExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 4F, true, true);
		}
	}

	@Override
    public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB boundingBox, Material materialIn) {
        if (materialIn == Material.WATER) return Boolean.TRUE;
        return null;
    }

	@Override
	public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos blockpos, IBlockState iblockstate, Entity entity, double yToTest, Material materialIn, boolean testingHead) {
		return materialIn == Material.WATER ? true : null;
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

    public BlockBuildCraftFluid setParticleColor(float particleRed, float particleGreen, float particleBlue) {
        this.particleRed = particleRed;
        this.particleGreen = particleGreen;
        this.particleBlue = particleBlue;
        return this;
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.randomDisplayTick(stateIn, worldIn, pos, rand);


		if (rand.nextInt(10) == 0 && worldIn.isSideSolid(pos.down(), EnumFacing.UP) && !worldIn.getBlockState(pos.down(2)).getMaterial().blocksMovement()) {

			double px = pos.getX() + rand.nextFloat();
			double py = pos.getY() - 1.05D;
			double pz = pos.getZ() + rand.nextFloat();

			Particle particle = new EntityDropParticleFX(worldIn, px, py, pz, particleRed, particleGreen, particleBlue);
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(particle);
		}
	}

    @Override
    public boolean canDisplace(IBlockAccess world, BlockPos pos) {
        if (world.getBlockState(pos).getMaterial().isLiquid()) {
            return false;
        }
        return super.canDisplace(world, pos);
    }

    @Override
    public boolean displaceIfPossible(World world, BlockPos pos) {
        if (world.getBlockState(pos).getMaterial().isLiquid()) {
            return false;
        }
        return super.displaceIfPossible(world, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setCusomStateMappers() {
        final ModelResourceLocation loc = ModelHelper.getBlockResourceLocation(this);
        ModelLoader.setCustomStateMapper(this, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return loc;
            }
        });
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosion) {
        return false;
    }

//    @Override
//    public EnumWorldBlockLayer getBlockLayer() {
//        return EnumWorldBlockLayer.CUTOUT;
//    }
}
