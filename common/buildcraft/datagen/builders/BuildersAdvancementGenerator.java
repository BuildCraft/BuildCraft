package buildcraft.datagen.builders;

import buildcraft.builders.BCBuilders;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.core.BCCoreBlocks;
import buildcraft.datagen.base.BCBaseAdvancementGenerator;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class BuildersAdvancementGenerator extends BCBaseAdvancementGenerator {
    private static final String NAMESPACE = BCBuilders.MODID;

    public BuildersAdvancementGenerator(DataGenerator generatorIn, ExistingFileHelper fileHelperIn) {
        super(generatorIn, fileHelperIn);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        // architect
        Advancement architect = Advancement.Builder.advancement().display(
                        BCBuildersBlocks.architect.get(),
                        new TranslationTextComponent("advancements.buildcraftbuilders.architect.title"),
                        new TranslationTextComponent("advancements.buildcraftbuilders.architect.description"),
                        null,
                        FrameType.TASK,
                        true, true, false)
                .parent(MARKERS)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":architect");
        // shaping_the_world
        Advancement shaping_the_world = Advancement.Builder.advancement().display(
                        BCBuildersBlocks.quarry.get(),
                        new TranslationTextComponent("advancements.buildcraftbuilders.shaping_the_world.title"),
                        new TranslationTextComponent("advancements.buildcraftbuilders.shaping_the_world.description"),
                        null,
                        FrameType.TASK,
                        true, true, false)
                .parent(GEARS)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":shaping_the_world");
        // building_for_the_future
        Advancement building_for_the_future = Advancement.Builder.advancement().display(
                        BCBuildersBlocks.filler.get(),
                        new TranslationTextComponent("advancements.buildcraftbuilders.building_for_the_future.title"),
                        new TranslationTextComponent("advancements.buildcraftbuilders.building_for_the_future.description"),
                        null,
                        FrameType.GOAL,
                        true, true, false)
                .parent(shaping_the_world)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":building_for_the_future");
        // diggy_diggy_hole
        Advancement diggy_diggy_hole = Advancement.Builder.advancement().display(
                        BCBuildersBlocks.quarry.get(),
                        new TranslationTextComponent("advancements.buildcraftbuilders.diggy.title"),
                        new TranslationTextComponent("advancements.buildcraftbuilders.diggy.description"),
                        null,
                        FrameType.GOAL,
                        true, true, false)
                .parent(shaping_the_world)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":diggy_diggy_hole");
        // destroying_the_world
        Advancement destroying_the_world = Advancement.Builder.advancement().display(
                        BCBuildersBlocks.filler.get(),
                        new TranslationTextComponent("advancements.buildcraftbuilders.destroying_the_world.title"),
                        new TranslationTextComponent("advancements.buildcraftbuilders.destroying_the_world.description"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false)
                .parent(diggy_diggy_hole)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":destroying_the_world");
        // paving_the_way
        Advancement paving_the_way = Advancement.Builder.advancement().display(
                        BCCoreBlocks.markerPath.get(),
                        new TranslationTextComponent("advancements.buildcraftbuilders.paving_the_way.title"),
                        new TranslationTextComponent("advancements.buildcraftbuilders.paving_the_way.description"),
                        null,
                        FrameType.TASK,
                        true, true, false)
                .parent(architect)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":paving_the_way");
        // start_of_something_big
        Advancement start_of_something_big = Advancement.Builder.advancement().display(
                        BCBuildersBlocks.builder.get(),
                        new TranslationTextComponent("advancements.buildcraftbuilders.start_of_something_big.title"),
                        new TranslationTextComponent("advancements.buildcraftbuilders.start_of_something_big.description"),
                        null,
                        FrameType.GOAL,
                        true, true, false)
                .parent(architect)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":start_of_something_big");
    }

    @Override
    public String getName() {
        return "BuildCraft Builders Advancement Generator";
    }
}
