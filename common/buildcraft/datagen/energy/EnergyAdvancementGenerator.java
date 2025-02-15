package buildcraft.datagen.energy;

import buildcraft.core.BCCoreBlocks;
import buildcraft.core.BCCoreItems;
import buildcraft.datagen.base.BCBaseAdvancementGenerator;
import buildcraft.energy.BCEnergy;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.energy.BCEnergyItems;
import buildcraft.energy.generation.biome.BCBiomeRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class EnergyAdvancementGenerator extends BCBaseAdvancementGenerator {
    private static final String NAMESPACE = BCEnergy.MODID;

    public EnergyAdvancementGenerator(PackOutput output, ExistingFileHelper fileHelperIn) {
        super(output, fileHelperIn);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        // engine
        Advancement engine = Advancement.Builder.advancement().display(
                        BCEnergyBlocks.engineStone.get(),
                        Component.translatable("advancements.buildcraftcore.engine.title"),
                        Component.translatable("advancements.buildcraftcore.engine.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(GUIDE)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "redstone_engine",
                        InventoryChangeTrigger.TriggerInstance.hasItems(BCCoreBlocks.engineWood.get())
                )
                .addCriterion(
                        "strirling_engine",
                        InventoryChangeTrigger.TriggerInstance.hasItems(BCEnergyBlocks.engineStone.get())
                )
                .addCriterion(
                        "combustion_engine",
                        InventoryChangeTrigger.TriggerInstance.hasItems(BCEnergyBlocks.engineIron.get())
                )
                .save(consumer, NAMESPACE + ":engine");
        // powering_up
        Advancement powering_up = Advancement.Builder.advancement().display(
                        BCEnergyBlocks.engineStone.get(),
                        Component.translatable("advancements.buildcraftenergy.poweringUp.title"),
                        Component.translatable("advancements.buildcraftenergy.poweringUp.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(engine)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":powering_up");
        // lava_power
        Advancement lava_power = Advancement.Builder.advancement().display(
                        Items.LAVA_BUCKET,
                        Component.translatable("advancements.buildcraftenergy.lava_power.title"),
                        Component.translatable("advancements.buildcraftenergy.lava_power.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(engine)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":lava_power");
        // ice_cool
        Advancement ice_cool = Advancement.Builder.advancement().display(
                        Items.WATER_BUCKET,
                        Component.translatable("advancements.buildcraftenergy.ice_cool.title"),
                        Component.translatable("advancements.buildcraftenergy.ice_cool.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(powering_up)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":ice_cool");
        // fine_riches
        Advancement fine_riches = Advancement.Builder.advancement().display(
                        BCEnergyItems.globOil.get(),
                        Component.translatable("advancements.buildcraftenergy.fine_riches.title"),
                        Component.translatable("advancements.buildcraftenergy.fine_riches.description"),
                        null,
                        FrameType.GOAL,
                        true, true, false
                )
                .parent(ROOT)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "oil_desert_biome",
                        PlayerTrigger.TriggerInstance.located(
                                LocationPredicate.inBiome(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_DESERT)
                        )
                )
                .addCriterion(
                        "oil_ocean_biome",
                        PlayerTrigger.TriggerInstance.located(
                                LocationPredicate.inBiome(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_OCEAN)
                        )
                )
                .save(consumer, NAMESPACE + ":fine_riches");
        // sticky_dipping
        Advancement sticky_dipping = Advancement.Builder.advancement().display(
                        BCEnergyItems.globOil.get(),
                        Component.translatable("advancements.buildcraftenergy.sticky_dipping.title"),
                        Component.translatable("advancements.buildcraftenergy.sticky_dipping.description"),
                        null,
                        FrameType.TASK,
                        true, true, true
                )
                .parent(fine_riches)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "oil",
                        EnterBlockTrigger.TriggerInstance.entersBlock(BCEnergyFluids.crudeOil[0].get().getReg().getBlock())
                )
                .save(consumer, NAMESPACE + ":sticky_dipping");
        // refine_and_redefine
        Advancement refine_and_redefine = Advancement.Builder.advancement().display(
                        BCEnergyItems.globOil.get(),
                        Component.translatable("advancements.buildcraftenergy.refine_and_redefine.title"),
                        Component.translatable("advancements.buildcraftenergy.refine_and_redefine.description"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false
                )
                .parent(sticky_dipping)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":refine_and_redefine");
        // to_much_power
        Advancement to_much_power = Advancement.Builder.advancement().display(
                        BCCoreItems.wrench.get(),
                        Component.translatable("advancements.buildcraftenergy.to_much_power.title"),
                        Component.translatable("advancements.buildcraftenergy.to_much_power.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(powering_up)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":to_much_power");
    }

    @Override
    public String getName() {
        return "BuildCraft Energy Advancement Generator";
    }
}
