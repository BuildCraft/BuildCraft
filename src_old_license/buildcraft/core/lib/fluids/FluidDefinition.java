package buildcraft.core.lib.fluids;

import java.util.Locale;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.BCRegistry;
import buildcraft.core.lib.block.BlockBuildCraftFluid;

public class FluidDefinition {
    public final BCFluid fluid;
    public final BlockBuildCraftFluid block;
    public final MaterialBuildCraftLiquid material;
    public final ItemBucketBuildcraft bucket;

    public FluidDefinition(String fluidName, int density, int viscocity) {
        this(fluidName, fluidName, density, viscocity, 0xFF_FF_FF_FF, 0xFF_FF_FF_FF);
    }

    public FluidDefinition(String fluidName, String textureSuffix, int density, int viscocity, int colourLight, int colourDark) {
        // Fluid itself
        String modid = Loader.instance().activeModContainer().getModId();
        String fluidTextureBase = modid.toLowerCase(Locale.ROOT).replace("|", "") + ":blocks/fluids/" + textureSuffix;
        ResourceLocation still = new ResourceLocation(fluidTextureBase + "_still");
        ResourceLocation flow = new ResourceLocation(fluidTextureBase + "_flow");
        fluid = new BCFluid(fluidName, still, flow).setColour(colourLight, colourDark);
        fluid.setDensity(density).setViscosity(viscocity);
        if (density < 0) fluid.setGaseous(true);
        FluidRegistry.registerFluid(fluid);
        material = new MaterialBuildCraftLiquid(MapColor.BLACK);
        block = new BlockBuildCraftFluid(fluid, material).setFlammability(0);
        block.setRegistryName(Loader.instance().activeModContainer().getModId(), "fluid_block_" + fluidName);
        block.setUnlocalizedName("blockFluid_" + fluidName);
        BCRegistry.INSTANCE.registerBlock(block, true);
        fluid.setBlock(block);

        // Bucket
        FluidStack bucketFluid = new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
        bucket = new ItemBucketBuildcraft(block, fluid);
        bucket.setUnlocalizedName("bucket_" + fluidName);
        bucket.setRegistryName(Loader.instance().activeModContainer().getModId(), "fluid_bucket_" + fluidName);
        BCRegistry.INSTANCE.registerItem(bucket, true);
        FluidContainerRegistry.registerFluidContainer(bucketFluid, new ItemStack(bucket), new ItemStack(Items.BUCKET));

        BucketHandler.INSTANCE.buckets.put(block.getDefaultState().withProperty(BlockFluidClassic.LEVEL, 0), bucket);
    }

    public final FluidStack createFluidStack(int amount) {
        return new FluidStack(fluid, amount);
    }

    @SideOnly(Side.CLIENT)
    public void textureStitchPre(TextureStitchEvent.Pre event) {
        TextureAtlasSprite still = event.getMap().getTextureExtry(fluid.getStill().toString());
        if (still == null) event.getMap().registerSprite(fluid.getStill());

        TextureAtlasSprite flow = event.getMap().getTextureExtry(fluid.getFlowing().toString());
        if (flow == null) event.getMap().registerSprite(fluid.getFlowing());
    }

    public static class BCFluid extends Fluid {
        private int colour = 0xFFFFFFFF, light = 0xFF_FF_FF_FF, dark = 0xFF_FF_FF_FF;
        private int heat;
        private boolean heatable;

        public BCFluid(String fluidName, ResourceLocation still, ResourceLocation flowing) {
            super(fluidName, still, flowing);
        }

        @Override
        public String getLocalizedName(FluidStack stack) {
            if (heat <= 0 && !isHeatable()) return super.getLocalizedName(stack);
            String name = super.getLocalizedName(stack);
            String heatString = I18n.translateToLocalFormatted("buildcraft.fluid.heat_" + heat);
            return name + heatString;
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

        public BCFluid setHeatable(boolean value) {
            heatable = value;
            return this;
        }

        public boolean isHeatable() {
            return heatable;
        }
    }
}
