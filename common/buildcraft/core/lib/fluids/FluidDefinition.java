package buildcraft.core.lib.fluids;

import java.util.Locale;

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
        this(fluidName, fluidName, density, viscocity, createBucket, 0xFF_FF_FF_FF, 0xFF_FF_FF_FF);
    }

    public FluidDefinition(String fluidName, String textureSuffix, int density, int viscocity, boolean createBucket, int colourLight,
            int colourDark) {
        // Fluid itself
        if (!FluidRegistry.isFluidRegistered(fluidName)) {
            String modid = Loader.instance().activeModContainer().getModId();
            String fluidTextureBase = modid.toLowerCase(Locale.ROOT).replace("|", "") + ":blocks/fluids/" + textureSuffix;
            ResourceLocation still = new ResourceLocation(fluidTextureBase + "_still");
            ResourceLocation flow = new ResourceLocation(fluidTextureBase + "_flow");
            masterFluid = new BCFluid(fluidName, still, flow).setColour(colourLight, colourDark);
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
        FluidStack bucketFluid = new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
        if (createBucket) {
            bucket = new ItemBucketBuildcraft(block);
            bucket.setUnlocalizedName("bucket_" + fluidName);
            bucket.setRegistryName(Loader.instance().activeModContainer().getModId(), "fluid_bucket_" + fluidName);
            BCRegistry.INSTANCE.registerItem(bucket, true);
            FluidContainerRegistry.registerFluidContainer(bucketFluid, new ItemStack(bucket), new ItemStack(Items.bucket));
        } else {
            ItemStack stack = FluidContainerRegistry.fillFluidContainer(bucketFluid, new ItemStack(Items.bucket));
            if (stack == null) bucket = null;
            else bucket = stack.getItem();
        }
    }

    public final FluidStack createFluidStack(int amount) {
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
        private int colour = 0xFFFFFFFF, light = 0xFF_FF_FF_FF, dark = 0xFF_FF_FF_FF;
        private int heat;

        public BCFluid(String fluidName, ResourceLocation still, ResourceLocation flowing) {
            super(fluidName, still, flowing);
        }

        @Override
        public int getColor() {
            return colour;
        }

        public int getLightColour() {
            return light;
        }

        public int getDarkColour() {
            return dark;
        }

        public BCFluid setColour(int colour) {
            this.colour = colour;
            return this;
        }

        public BCFluid setColour(int light, int dark) {
            this.light = light;
            this.dark = dark;
            this.colour = 0xFF_FF_FF_FF;
            return this;
        }

        public BCFluid setHeat(int heat) {
            this.heat = heat;
            return this;
        }

        public int getHeatValue() {
            return heat;
        }
    }
}
