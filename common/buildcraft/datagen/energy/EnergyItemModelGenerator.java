package buildcraft.datagen.energy;

import buildcraft.datagen.base.BCBaseItemModelGenerator;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.energy.BCEnergyItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class EnergyItemModelGenerator extends BCBaseItemModelGenerator {
    public EnergyItemModelGenerator(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Block Items
        getBuilder(BCEnergyBlocks.engineStone.get().getRegistryName().toString()).parent(BUILTIN_ENTITY);
        getBuilder(BCEnergyBlocks.engineIron.get().getRegistryName().toString()).parent(BUILTIN_ENTITY);
        getBuilder(BCEnergyBlocks.engineRf.get().getRegistryName().toString()).parent(BUILTIN_ENTITY);
        getBuilder(BCEnergyBlocks.mjDynamo.get().getRegistryName().toString()).parent(BUILTIN_ENTITY);

        // Items
        withExistingParent(BCEnergyItems.globOil.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftenergy:items/glob_oil");
        withExistingParent(BCEnergyItems.oilPlacer.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftenergy:items/glob_oil");
    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Energy Item Model Generator";
    }
}
