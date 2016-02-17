package buildcraft.core.lib.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.core.BCRegistry;
import buildcraft.core.lib.block.BlockBuildCraftFluid;

public class FluidDefinition {
    public final Fluid fluid;
    public final Block block;
    public final Material material;
    public final Item bucket;

    // Master version. These will be null if BC did not register the fluid.
    public final BCFluid masterFluid;
    public final BlockBuildCraftFluid masterBlock;
    public final MaterialBuildCraftLiquid masterMaterial;

    public FluidDefinition(String fluidName, int density, int viscocity, boolean createBucket) {
        this(fluidName, fluidName, density, viscocity, createBucket, 0xFFFFFFFF);
    }

    public FluidDefinition(String fluidName, String textureSuffix, int density, int viscocity, boolean createBucket, int colour) {
        // Fluid itself
        if (!FluidRegistry.isFluidRegistered(fluidName)) {
            String fluidTextureBase = "buildcraftenergy:blocks/fluids/" + textureSuffix;
            ResourceLocation still = new ResourceLocation(fluidTextureBase + "_still");
            ResourceLocation flow = new ResourceLocation(fluidTextureBase + "_flow");
            masterFluid = new BCFluid(fluidName, still, flow).setColour(colour);
            fluid = masterFluid;
            fluid.setDensity(density).setViscosity(viscocity);
            if (density < 0) fluid.setGaseous(true);
            FluidRegistry.registerFluid(fluid);
        } else {
            BCLog.logger.warn("Not using BuildCraft fluid " + fluidName + " - issues might occur!");
            masterFluid = null;
            fluid = FluidRegistry.getFluid(fluidName);
            createBucket = false;
        }

        // Block + Material
        if (fluid.getBlock() == null) {
            material = masterMaterial = new MaterialBuildCraftLiquid(MapColor.blackColor);
            masterBlock = new BlockBuildCraftFluid(fluid, material).setFlammability(0);
            block = masterBlock;
            block.setRegistryName(Loader.instance().activeModContainer().getModId(), "fluid_block_" + fluidName);
            block.setUnlocalizedName("blockFluid_" + fluidName);
            BCRegistry.INSTANCE.registerBlock(block, true);
            fluid.setBlock(block);
        } else {
            block = fluid.getBlock();
            masterBlock = null;
            material = block.getMaterial();
            masterMaterial = null;
        }

        // Bucket
        if (createBucket) {
            bucket = new ItemBucketBuildcraft(block);
            bucket.setUnlocalizedName("bucket_" + fluidName);
            bucket.setRegistryName(Loader.instance().activeModContainer().getModId(), "fluid_bucket_" + fluidName);
            BCRegistry.INSTANCE.registerItem(bucket, true);
            FluidStack stack = new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
            FluidContainerRegistry.registerFluidContainer(stack, new ItemStack(bucket), new ItemStack(Items.bucket));
        } else {
            bucket = null;
        }
    }

    public FluidStack createFluidStack(int amount) {
        return new FluidStack(fluid, amount);
    }

    @SideOnly(Side.CLIENT)
    public void textureStitchPre(TextureStitchEvent.Pre event) {
        TextureAtlasSprite still = event.map.getTextureExtry(fluid.getStill().toString());
        if (still == null) event.map.registerSprite(fluid.getStill());

        TextureAtlasSprite flow = event.map.getTextureExtry(fluid.getFlowing().toString());
        if (flow == null) event.map.registerSprite(fluid.getFlowing());
    }

    public static class BCFluid extends Fluid {
        private int colour = 0xFFFFFFFF;

        public BCFluid(String fluidName, ResourceLocation still, ResourceLocation flowing) {
            super(fluidName, still, flowing);
        }

        @Override
        public int getColor() {
            return colour;
        }

        public BCFluid setColour(int colour) {
            this.colour = colour;
            return this;
        }
    }
}
