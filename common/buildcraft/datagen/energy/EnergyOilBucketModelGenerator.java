package buildcraft.datagen.energy;

import buildcraft.datagen.base.BCBaseItemModelGenerator;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.energy.event.ChristmasHandler;
import buildcraft.lib.fluid.BCFluid;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.loaders.DynamicBucketModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;

public class EnergyOilBucketModelGenerator extends BCBaseItemModelGenerator {
    public EnergyOilBucketModelGenerator(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerBuckets();
    }

    protected void registerBuckets() {
        for (RegistryObject<BCFluid.Source> fluid : BCEnergyFluids.allStill) {
            registerBucket(fluid.get());
        }
    }

    protected void registerBucket(BCFluid.Source fluid) {
        int density = fluid.getAttributes().getDensity();
        boolean isGaseous = ChristmasHandler.isEnabled() ? (density > 0) : (density < 0);
        // normal
        withExistingParent(
                fluid.getReg().getBucket().getRegistryName().toString(),
                new ResourceLocation("forge", "item/bucket_drip")
        )
                .customLoader(DynamicBucketModelBuilder::begin)
                .flipGas(isGaseous)
                .fluid(fluid.getSource())
        ;
        // christmas
        withExistingParent(
                fluid.getReg().getBucket().getRegistryName().toString() + "_christmas",
                new ResourceLocation("forge", "item/bucket_drip")
        )
                .customLoader(DynamicBucketModelBuilder::begin)
                .flipGas(false)
                .fluid(fluid.getSource())
        ;
    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Energy Oil Bucket Model Generator";
    }
}
