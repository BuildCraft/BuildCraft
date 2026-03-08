package buildcraft.datagen.factory;

import buildcraft.datagen.base.BCBaseAdvancementGenerator;
import buildcraft.factory.BCFactory;
import buildcraft.factory.BCFactoryBlocks;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class FactoryAdvancementGenerator extends BCBaseAdvancementGenerator {
    private static final String NAMESPACE = BCFactory.MODID;

    public FactoryAdvancementGenerator(DataGenerator generatorIn, ExistingFileHelper fileHelperIn) {
        super(generatorIn, fileHelperIn);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        // fluid_storage
        Advancement fluid_storage = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.tank.get(),
                        new TranslationTextComponent("advancements.buildcraftfactory.fluid_storage.title"),
                        new TranslationTextComponent("advancements.buildcraftfactory.fluid_storage.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(ROOT)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":fluid_storage");
        // flooding_the_world
        Advancement flooding_the_world = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.floodGate.get(),
                        new TranslationTextComponent("advancements.buildcraftfactory.flooding_the_world.title"),
                        new TranslationTextComponent("advancements.buildcraftfactory.flooding_the_world.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(fluid_storage)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":flooding_the_world");
        // draining_the_world
        Advancement draining_the_world = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.pump.get(),
                        new TranslationTextComponent("advancements.buildcraftfactory.draining_the_world.title"),
                        new TranslationTextComponent("advancements.buildcraftfactory.draining_the_world.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(fluid_storage)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":draining_the_world");
        // oil_platform
        Advancement oil_platform = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.pump.get(),
                        new TranslationTextComponent("advancements.buildcraftfactory.oil_platform.title"),
                        new TranslationTextComponent("advancements.buildcraftfactory.oil_platform.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(draining_the_world)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":oil_platform");
        // black_gold
        Advancement black_gold = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.pump.get(),
                        new TranslationTextComponent("advancements.buildcraftfactory.black_gold.title"),
                        new TranslationTextComponent("advancements.buildcraftfactory.black_gold.description"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false
                )
                .parent(oil_platform)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":black_gold");
        // heating_and_distilling
        Advancement heating_and_distilling = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.distiller.get(),
                        new TranslationTextComponent("advancements.buildcraftfactory.heating_and_distilling.title"),
                        new TranslationTextComponent("advancements.buildcraftfactory.heating_and_distilling.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(oil_platform)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":heating_and_distilling");
        // lazy_crafting
        Advancement lazy_crafting = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.autoWorkbenchItems.get(),
                        new TranslationTextComponent("advancements.buildcraftfactory.lazy_crafting.title"),
                        new TranslationTextComponent("advancements.buildcraftfactory.lazy_crafting.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(WRENCHED)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":lazy_crafting");
        // retired_hopper
        Advancement retired_hopper = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.chute.get(),
                        new TranslationTextComponent("advancements.buildcraftfactory.retired_hopper.title"),
                        new TranslationTextComponent("advancements.buildcraftfactory.retired_hopper.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(WRENCHED)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":retired_hopper");
    }

    @Override
    public String getName() {
        return "BuildCraft Factory Advancement Generator";
    }
}
