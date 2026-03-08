package buildcraft.datagen.transport;

import buildcraft.core.BCCoreItems;
import buildcraft.datagen.base.BCBaseAdvancementGenerator;
import buildcraft.datagen.core.CoreAdvancementGenerator;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.gate.EnumGateLogic;
import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.EnumGateModifier;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.transport.BCTransport;
import buildcraft.transport.BCTransportItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class TransportAdvancementGenerator extends BCBaseAdvancementGenerator {
    private static final String NAMESPACE = BCTransport.MODID;

    public TransportAdvancementGenerator(DataGenerator generatorIn, ExistingFileHelper fileHelperIn) {
        super(generatorIn, fileHelperIn);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        // pipe_dream
        Advancement pipe_dream = Advancement.Builder.advancement().display(
                        (Item) BCTransportItems.pipeStructure.get(null).get(),
                        new TranslationTextComponent("advancements.buildcrafttransport.pipe_dream.title"),
                        new TranslationTextComponent("advancements.buildcrafttransport.pipe_dream.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(CoreAdvancementGenerator.ROOT)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":pipe_dream");
        // plugging_the_gap
        Advancement plugging_the_gap = Advancement.Builder.advancement().display(
                        BCTransportItems.plugBlocker.get(),
                        new TranslationTextComponent("advancements.buildcrafttransport.plugging_the_gap.title"),
                        new TranslationTextComponent("advancements.buildcrafttransport.plugging_the_gap.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(pipe_dream)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":plugging_the_gap");
        // pipe_logic
        Advancement pipe_logic = Advancement.Builder.advancement().display(
                        BCSiliconItems.variantGateMap.get(new GateVariant(new CompoundNBT())).get(),
                        new TranslationTextComponent("advancements.buildcrafttransport.pipe_logic.title"),
                        new TranslationTextComponent("advancements.buildcrafttransport.pipe_logic.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(plugging_the_gap)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":pipe_logic");
        // all_plugged_up
        Advancement all_plugged_up = Advancement.Builder.advancement().display(
                        BCSiliconItems.plugLightSensor.get(),
                        new TranslationTextComponent("advancements.buildcrafttransport.all_plugged_up.title"),
                        new TranslationTextComponent("advancements.buildcrafttransport.all_plugged_up.description"),
                        null,
                        FrameType.GOAL,
                        true, true, false
                )
                .parent(pipe_logic)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":all_plugged_up");
        // categorizing_with_colors
        Advancement categorizing_with_colors = Advancement.Builder.advancement().display(
                        (Item) BCTransportItems.pipeItemEmzuli.get(null).get(),
                        new TranslationTextComponent("advancements.buildcrafttransport.categorizing_with_colors.title"),
                        new TranslationTextComponent("advancements.buildcrafttransport.categorizing_with_colors.description"),
                        null,
                        FrameType.GOAL,
                        true, true, false
                )
                .parent(plugging_the_gap)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":categorizing_with_colors");
        // logic_transportation
        ItemStack wireStack_logic_transportation = new ItemStack(BCTransportItems.wire.get());
        ColourUtil.addColourTagToStack(wireStack_logic_transportation, DyeColor.byId(0));
        Advancement logic_transportation = Advancement.Builder.advancement().display(
                        wireStack_logic_transportation,
                        new TranslationTextComponent("advancements.buildcrafttransport.logic_transportation.title"),
                        new TranslationTextComponent("advancements.buildcrafttransport.logic_transportation.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(pipe_dream)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":logic_transportation");
        // colorful_electrician
        ItemStack wireStack_colorful_electrician = new ItemStack(BCTransportItems.wire.get());
        ColourUtil.addColourTagToStack(wireStack_colorful_electrician, DyeColor.byId(5));
        Advancement colorful_electrician = Advancement.Builder.advancement().display(
                        wireStack_colorful_electrician,
                        new TranslationTextComponent("advancements.buildcrafttransport.colorful_electrician.title"),
                        new TranslationTextComponent("advancements.buildcrafttransport.colorful_electrician.description"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false
                )
                .parent(logic_transportation)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":colorful_electrician");
        // extended_logic
        Advancement extended_logic = Advancement.Builder.advancement().display(
                        BCSiliconItems.variantGateMap.get(new GateVariant(EnumGateLogic.OR, EnumGateMaterial.GOLD, EnumGateModifier.DIAMOND)).get(),
                        new TranslationTextComponent("advancements.buildcrafttransport.extended_logic.title"),
                        new TranslationTextComponent("advancements.buildcrafttransport.extended_logic.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(pipe_logic)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":extended_logic");
        // pipe_diversification
        Advancement pipe_diversification = Advancement.Builder.advancement().display(
                        (Item) BCTransportItems.pipeItemDiaWood.get(null).get(),
                        new TranslationTextComponent("advancements.buildcrafttransport.pipe_diversification.title"),
                        new TranslationTextComponent("advancements.buildcrafttransport.pipe_diversification.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(pipe_dream)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":pipe_diversification");
        // pipe_fanatic
        Advancement pipe_fanatic = Advancement.Builder.advancement().display(
                        (Item) BCTransportItems.pipeItemDiamond.get(null).get(),
                        new TranslationTextComponent("advancements.buildcrafttransport.pipe_fanatic.title"),
                        new TranslationTextComponent("advancements.buildcrafttransport.pipe_fanatic.description"),
                        null,
                        FrameType.GOAL,
                        true, true, false
                )
                .parent(pipe_diversification)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":pipe_fanatic");
        // sealing_fluids
        Advancement sealing_fluids = Advancement.Builder.advancement().display(
                        BCTransportItems.waterproof.get(),
                        new TranslationTextComponent("advancements.buildcrafttransport.sealing_fluids.title"),
                        new TranslationTextComponent("advancements.buildcrafttransport.sealing_fluids.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(pipe_diversification)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":sealing_fluids");
        // too_many_pipe_filters
        Advancement too_many_pipe_filters = Advancement.Builder.advancement().display(
                        Items.GREEN_DYE,
                        new TranslationTextComponent("advancements.buildcrafttransport.too_many_pipe_filters.title"),
                        new TranslationTextComponent("advancements.buildcrafttransport.too_many_pipe_filters.description"),
                        null,
                        FrameType.TASK,
                        true, true, true
                )
                .parent(pipe_diversification)
                .requirements(IRequirementsStrategy.OR)
                .rewards(AdvancementRewards.Builder.recipe(BCCoreItems.list.get().getRegistryName()))
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":too_many_pipe_filters");
    }

    @Override
    public String getName() {
        return "BuildCraft Transport Advancement Generator";
    }
}
