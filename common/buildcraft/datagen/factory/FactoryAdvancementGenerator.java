package buildcraft.datagen.factory;

import buildcraft.datagen.base.BCBaseAdvancementGenerator;
import buildcraft.factory.BCFactory;
import buildcraft.factory.BCFactoryBlocks;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class FactoryAdvancementGenerator extends BCBaseAdvancementGenerator {
    private static final String NAMESPACE = BCFactory.MODID;

    public FactoryAdvancementGenerator(PackOutput output, ExistingFileHelper fileHelperIn) {
        super(output, fileHelperIn);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        // fluid_storage
        Advancement fluid_storage = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.tank.get(),
                        Component.translatable("advancements.buildcraftfactory.fluid_storage.title"),
                        Component.translatable("advancements.buildcraftfactory.fluid_storage.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(ROOT)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":fluid_storage");
        // flooding_the_world
        Advancement flooding_the_world = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.floodGate.get(),
                        Component.translatable("advancements.buildcraftfactory.flooding_the_world.title"),
                        Component.translatable("advancements.buildcraftfactory.flooding_the_world.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(fluid_storage)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":flooding_the_world");
        // draining_the_world
        Advancement draining_the_world = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.pump.get(),
                        Component.translatable("advancements.buildcraftfactory.draining_the_world.title"),
                        Component.translatable("advancements.buildcraftfactory.draining_the_world.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(fluid_storage)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":draining_the_world");
        // oil_platform
        Advancement oil_platform = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.pump.get(),
                        Component.translatable("advancements.buildcraftfactory.oil_platform.title"),
                        Component.translatable("advancements.buildcraftfactory.oil_platform.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(draining_the_world)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":oil_platform");
        // black_gold
        Advancement black_gold = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.pump.get(),
                        Component.translatable("advancements.buildcraftfactory.black_gold.title"),
                        Component.translatable("advancements.buildcraftfactory.black_gold.description"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false
                )
                .parent(oil_platform)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":black_gold");
        // heating_and_distilling
        Advancement heating_and_distilling = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.distiller.get(),
                        Component.translatable("advancements.buildcraftfactory.heating_and_distilling.title"),
                        Component.translatable("advancements.buildcraftfactory.heating_and_distilling.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(oil_platform)
                .requirements(RequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":heating_and_distilling");
        // lazy_crafting
        Advancement lazy_crafting = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.autoWorkbenchItems.get(),
                        Component.translatable("advancements.buildcraftfactory.lazy_crafting.title"),
                        Component.translatable("advancements.buildcraftfactory.lazy_crafting.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(WRENCHED)
                .requirements(RequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":lazy_crafting");
        // retired_hopper
        Advancement retired_hopper = Advancement.Builder.advancement().display(
                        BCFactoryBlocks.chute.get(),
                        Component.translatable("advancements.buildcraftfactory.retired_hopper.title"),
                        Component.translatable("advancements.buildcraftfactory.retired_hopper.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(WRENCHED)
                .requirements(RequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":retired_hopper");
    }

    @Override
    public String getName() {
        return "BuildCraft Factory Advancement Generator";
    }
}
