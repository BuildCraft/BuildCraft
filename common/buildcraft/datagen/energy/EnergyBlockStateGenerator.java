package buildcraft.datagen.energy;

import buildcraft.datagen.base.BCBaseBlockStateGenerator;
import buildcraft.energy.BCEnergy;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.lib.fluid.BCFluidRegistryContainer;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class EnergyBlockStateGenerator extends BCBaseBlockStateGenerator {
    public EnergyBlockStateGenerator(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BCEnergy.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // engine
        builtinEntity(BCEnergyBlocks.engineStone.get(), "buildcraftenergy:block/engine/stone/back");
        builtinEntity(BCEnergyBlocks.engineIron.get(), "buildcraftenergy:block/engine/iron/back");

        // oil block
        for (BCFluidRegistryContainer container : BCEnergyFluids.allFluids) {
            simpleBlock(
                    container.getBlock(),
                    models().getBuilder(container.getBlock().getRegistryName().toString())
                            .texture("particle", container.getFluidType().getStillTexture())
            );
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "BuildCraft Energy BlockState Generator";
    }
}
