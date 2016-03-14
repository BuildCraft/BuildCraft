/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.client.render.EntityDropParticleFX;
import buildcraft.core.lib.utils.ICustomStateMapper;
import buildcraft.core.lib.utils.ModelHelper;

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
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block) {
        super.onNeighborBlockChange(world, pos, state, block);
        if (flammable && world.provider.getDimensionId() == -1) {
            world.setBlockToAir(pos);
            world.newExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 4F, true, true);
        }
    }

    // @Override
    // public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side) {
    // return true;
    // }

    @Override
    public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB boundingBox, Material materialIn) {
        if (materialIn == Material.water) return Boolean.TRUE;
        return null;
    }

    @Override
    public Boolean isEntityInsideMaterial(World world, BlockPos blockpos, IBlockState iblockstate, Entity entity, double yToTest, Material materialIn,
            boolean testingHead) {
        if (materialIn == Material.water) return Boolean.TRUE;
        return null;
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
    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.randomDisplayTick(world, pos, state, rand);

        if (rand.nextInt(10) == 0 && World.doesBlockHaveSolidTopSurface(world, pos.down()) && !world.getBlockState(pos.down(2)).getBlock()
                .getMaterial().blocksMovement()) {

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

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.TRANSLUCENT;
    }
}
