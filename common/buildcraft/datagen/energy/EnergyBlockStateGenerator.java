package buildcraft.datagen.energy;

import buildcraft.datagen.base.BCBaseBlockStateGenerator;
import buildcraft.energy.BCEnergy;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.lib.fluid.BCFluid;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class EnergyBlockStateGenerator extends BCBaseBlockStateGenerator {
    public EnergyBlockStateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, BCEnergy.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // engine
        builtinEntity(BCEnergyBlocks.engineStone.get(), "buildcraftenergy:blocks/engine/stone/back");
        builtinEntity(BCEnergyBlocks.engineIron.get(), "buildcraftenergy:blocks/engine/iron/back");
        builtinEntity(BCEnergyBlocks.engineRf.get(), "buildcraftenergy:blocks/engine/rf/back");
        builtinEntity(BCEnergyBlocks.mjDynamo.get(), "buildcraftenergy:blocks/mj_dynamo/back");

        // oil block
        for (RegistryObject<BCFluid.Source> fluid : BCEnergyFluids.allStill) {
            simpleBlock(
                    fluid.get().getReg().getBlock(),
                    models().getBuilder(fluid.get().getReg().getBlock().getRegistryName().toString())
                            .texture("particle", fluid.get().getSource().getAttributes().getStillTexture())
            );
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "BuildCraft Energy BlockState Generator";
    }
}
