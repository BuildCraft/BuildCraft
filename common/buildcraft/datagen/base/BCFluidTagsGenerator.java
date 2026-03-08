package buildcraft.datagen.base;

import buildcraft.api.BCModules;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.stream.Stream;

public class BCFluidTagsGenerator extends FluidTagsProvider {

    public BCFluidTagsGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, BCModules.BUILDCRAFT, existingFileHelper);
    }

    @Override
    protected void addTags() {
        TagAppender<Fluid> oil = tag(OreDictionaryTags.OIL);
        addAllOptional(oil, BCEnergyFluids.getAllStill().stream().map(reg -> reg));
        addAllOptional(oil, BCEnergyFluids.getAllFlow().stream().map(reg -> reg));
    }

    private static void addAllOptional(TagAppender<Fluid> tag, Stream<RegistryObject<?>> allToAdd) {
        allToAdd.forEach(reg -> tag.addOptional(reg.getId()));
    }

    @Override
    public String getName() {
        return "BuildCraft Fluid Tags Generator";
    }
}
