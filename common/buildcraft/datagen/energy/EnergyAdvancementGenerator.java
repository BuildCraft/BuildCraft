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
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.EnterBlockTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.PositionTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Items;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class EnergyAdvancementGenerator extends BCBaseAdvancementGenerator {
    private static final String NAMESPACE = BCEnergy.MODID;

    public EnergyAdvancementGenerator(DataGenerator generatorIn, ExistingFileHelper fileHelperIn) {
        super(generatorIn, fileHelperIn);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        // engine
        Advancement engine = Advancement.Builder.advancement().display(
                        BCEnergyBlocks.engineStone.get(),
                        new TranslationTextComponent("advancements.buildcraftcore.engine.title"),
                        new TranslationTextComponent("advancements.buildcraftcore.engine.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(GUIDE)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "redstone_engine",
                        InventoryChangeTrigger.Instance.hasItems(BCCoreBlocks.engineWood.get())
                )
                .addCriterion(
                        "strirling_engine",
                        InventoryChangeTrigger.Instance.hasItems(BCEnergyBlocks.engineStone.get())
                )
                .addCriterion(
                        "combustion_engine",
                        InventoryChangeTrigger.Instance.hasItems(BCEnergyBlocks.engineIron.get())
                )
                .save(consumer, NAMESPACE + ":engine");
        // powering_up
        Advancement powering_up = Advancement.Builder.advancement().display(
                        BCEnergyBlocks.engineStone.get(),
                        new TranslationTextComponent("advancements.buildcraftenergy.poweringUp.title"),
                        new TranslationTextComponent("advancements.buildcraftenergy.poweringUp.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(engine)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":powering_up");
        // lava_power
        Advancement lava_power = Advancement.Builder.advancement().display(
                        Items.LAVA_BUCKET,
                        new TranslationTextComponent("advancements.buildcraftenergy.lava_power.title"),
                        new TranslationTextComponent("advancements.buildcraftenergy.lava_power.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(engine)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":lava_power");
        // ice_cool
        Advancement ice_cool = Advancement.Builder.advancement().display(
                        Items.WATER_BUCKET,
                        new TranslationTextComponent("advancements.buildcraftenergy.ice_cool.title"),
                        new TranslationTextComponent("advancements.buildcraftenergy.ice_cool.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(powering_up)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":ice_cool");
        // fine_riches
        Advancement fine_riches = Advancement.Builder.advancement().display(
                        BCEnergyItems.globOil.get(),
                        new TranslationTextComponent("advancements.buildcraftenergy.fine_riches.title"),
                        new TranslationTextComponent("advancements.buildcraftenergy.fine_riches.description"),
                        null,
                        FrameType.GOAL,
                        true, true, false
                )
                .parent(ROOT)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "oil_desert_biome",
                        PositionTrigger.Instance.located(
                                LocationPredicate.inBiome(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_DESERT)
                        )
                )
                .addCriterion(
                        "oil_ocean_biome",
                        PositionTrigger.Instance.located(
                                LocationPredicate.inBiome(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_OCEAN)
                        )
                )
                .save(consumer, NAMESPACE + ":fine_riches");
        // sticky_dipping
        Advancement sticky_dipping = Advancement.Builder.advancement().display(
                        BCEnergyItems.globOil.get(),
                        new TranslationTextComponent("advancements.buildcraftenergy.sticky_dipping.title"),
                        new TranslationTextComponent("advancements.buildcraftenergy.sticky_dipping.description"),
                        null,
                        FrameType.TASK,
                        true, true, true
                )
                .parent(fine_riches)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "oil",
                        EnterBlockTrigger.Instance.entersBlock(BCEnergyFluids.crudeOil[0].get().getReg().getBlock())
                )
                .save(consumer, NAMESPACE + ":sticky_dipping");
        // refine_and_redefine
        Advancement refine_and_redefine = Advancement.Builder.advancement().display(
                        BCEnergyItems.globOil.get(),
                        new TranslationTextComponent("advancements.buildcraftenergy.refine_and_redefine.title"),
                        new TranslationTextComponent("advancements.buildcraftenergy.refine_and_redefine.description"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false
                )
                .parent(sticky_dipping)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":refine_and_redefine");
        // to_much_power
        Advancement to_much_power = Advancement.Builder.advancement().display(
                        BCCoreItems.wrench.get(),
                        new TranslationTextComponent("advancements.buildcraftenergy.to_much_power.title"),
                        new TranslationTextComponent("advancements.buildcraftenergy.to_much_power.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(powering_up)
                .requirements(IRequirementsStrategy.OR)
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
