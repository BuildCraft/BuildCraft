package buildcraft.datagen.base;

import buildcraft.api.BCModules;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.concurrent.CompletableFuture;

public class BCFluidTagsGenerator extends FluidTagsProvider {

    public BCFluidTagsGenerator(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, BCModules.BUILDCRAFT, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider) {
        tag(OreDictionaryTags.OIL)
                .add(BCEnergyFluids.getAllStill().stream().map(RegistryObject::get).toArray(BCFluid.Source[]::new))
                .add(BCEnergyFluids.getAllFlow().stream().map(RegistryObject::get).toArray(BCFluid.Flowing[]::new))
        ;
    }

    @Override
    public String getName() {
        return "BuildCraft Fluid Tags Generator";
    }
}
