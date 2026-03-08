package buildcraft.datagen.core;

import buildcraft.core.BCCore;
import buildcraft.core.BCCoreBlocks;
import buildcraft.core.BCCoreItems;
import buildcraft.datagen.base.BCBaseAdvancementGenerator;
import buildcraft.lib.BCLibItems;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class CoreAdvancementGenerator extends BCBaseAdvancementGenerator {
    private static final String NAMESPACE = BCCore.MODID;

    public CoreAdvancementGenerator(DataGenerator generatorIn, ExistingFileHelper fileHelperIn) {
        super(generatorIn, fileHelperIn);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        // root
        Advancement root = Advancement.Builder.advancement().display(
                        BCCoreItems.gearWood.get(),
                        new TranslationTextComponent("advancements.buildcraftcore.root.title"),
                        new TranslationTextComponent("advancements.buildcraftcore.root.description"),
                        new ResourceLocation("minecraft:textures/gui/advancements/backgrounds/adventure.png"),
                        FrameType.TASK,
                        false, false, false)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("has_stick",
                        InventoryChangeTrigger.Instance.hasItems(tag(Tags.Items.RODS_WOODEN))
                )
                .save(consumer, NAMESPACE + ":root");
        ROOT = root;
        // gears
        Advancement gears = Advancement.Builder.advancement().display(
                        BCCoreItems.gearDiamond.get(),
                        new TranslationTextComponent("advancements.buildcraftcore.gears.title"),
                        new TranslationTextComponent("advancements.buildcraftcore.gears.description"),
                        null,
                        FrameType.GOAL,
                        true, true, false)
                .parent(root)
                .requirements(IRequirementsStrategy.AND)
                .addCriterion("gear_wood",
                        InventoryChangeTrigger.Instance.hasItems(tag(OreDictionaryTags.GEAR_WOOD))
                )
                .addCriterion("gear_stone",
                        InventoryChangeTrigger.Instance.hasItems(tag(OreDictionaryTags.GEAR_STONE))
                )
                .addCriterion("gear_iron",
                        InventoryChangeTrigger.Instance.hasItems(tag(OreDictionaryTags.GEAR_IRON))
                )
                .addCriterion("gear_gold",
                        InventoryChangeTrigger.Instance.hasItems(tag(OreDictionaryTags.GEAR_GOLD))
                )
                .addCriterion("gear_diamond",
                        InventoryChangeTrigger.Instance.hasItems(tag(OreDictionaryTags.GEAR_DIAMOND))
                )
                .save(consumer, NAMESPACE + ":gears");
        GEARS = gears;
        // wrenched
        Advancement wrenched = Advancement.Builder.advancement().display(
                        BCCoreItems.wrench.get(),
                        new TranslationTextComponent("advancements.buildcraftcore.wrenched.title"),
                        new TranslationTextComponent("advancements.buildcraftcore.wrenched.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(root)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion(
                        "code_trigger",
                        IMPOSSIBLE
                )
                .save(consumer, NAMESPACE + ":wrenched");
        WRENCHED = wrenched;
        // free_power
        Advancement free_power = Advancement.Builder.advancement().display(
                        BCCoreBlocks.engineWood.get(),
                        new TranslationTextComponent("advancements.buildcraftcore.freePowar.title"),
                        new TranslationTextComponent("advancements.buildcraftcore.freePowar.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(wrenched)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":free_power");
        // guide
        Advancement guide = Advancement.Builder.advancement().display(
                        BCLibItems.guide.get(),
                        new TranslationTextComponent("advancements.buildcraftcore.guide.title"),
                        new TranslationTextComponent("advancements.buildcraftcore.guide.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(root)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":guide");
        GUIDE = guide;
        // markers
        Advancement markers = Advancement.Builder.advancement().display(
                        BCCoreBlocks.markerVolume.get(),
                        new TranslationTextComponent("advancements.buildcraftcore.markers.title"),
                        new TranslationTextComponent("advancements.buildcraftcore.markers.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(guide)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":markers");
        MARKERS = markers;
        // list
        Advancement list = Advancement.Builder.advancement().display(
                        BCCoreItems.list.get(),
                        new TranslationTextComponent("advancements.buildcraftcore.list.title"),
                        new TranslationTextComponent("advancements.buildcraftcore.list.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(guide)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":list");
        // paper
        Advancement paper = Advancement.Builder.advancement().display(
                        Items.PAPER,
                        new TranslationTextComponent("advancements.buildcraftcore.paper.title"),
                        new TranslationTextComponent("advancements.buildcraftcore.paper.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(list)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":paper");
        // goggles
        Advancement goggles = Advancement.Builder.advancement().display(
                        // TODO Calen goggles texture
//                        BCCoreItems.GOOGLES.get(),
                        Items.IRON_HELMET,
                        new TranslationTextComponent("advancements.buildcraftcore.goggles.title"),
                        new TranslationTextComponent("advancements.buildcraftcore.goggles.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(guide)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":goggles");
        // path_markers
        Advancement path_markers = Advancement.Builder.advancement().display(
                        BCCoreBlocks.markerPath.get(),
                        new TranslationTextComponent("advancements.buildcraftcore.path_markers.title"),
                        new TranslationTextComponent("advancements.buildcraftcore.path_markers.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(markers)
                .requirements(IRequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":path_markers");
    }

    @Override
    public String getName() {
        return "BuildCraft Core Advancement Generator";
    }
}
