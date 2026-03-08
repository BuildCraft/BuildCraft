package buildcraft.datagen.silicon;

import buildcraft.datagen.base.BCBaseAdvancementGenerator;
import buildcraft.silicon.BCSilicon;
import buildcraft.silicon.BCSiliconBlocks;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class SiliconAdvancementGenerator extends BCBaseAdvancementGenerator {
    private static final String NAMESPACE = BCSilicon.MODID;

    public SiliconAdvancementGenerator(DataGenerator generatorIn, ExistingFileHelper fileHelperIn) {
        super(generatorIn, fileHelperIn);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        // fluid_storage
        Advancement laser_power = Advancement.Builder.advancement().display(
                        BCSiliconBlocks.laser.get(),
                        new TranslatableComponent("advancements.buildcraftsilicon.laser_power.title"),
                        new TranslatableComponent("advancements.buildcraftsilicon.laser_power.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(ROOT)
                .requirements(RequirementsStrategy.OR)
                .addCriterion("get_laser",
                        InventoryChangeTrigger.TriggerInstance.hasItems(BCSiliconBlocks.laser.get())
                )
                .save(consumer, NAMESPACE + ":laser_power");
        // precision_crafting
        Advancement precision_crafting = Advancement.Builder.advancement().display(
                        BCSiliconBlocks.assemblyTable.get(),
                        new TranslatableComponent("advancements.buildcraftsilicon.precision_crafting.title"),
                        new TranslatableComponent("advancements.buildcraftsilicon.precision_crafting.description"),
                        null,
                        FrameType.TASK,
                        true, true, false
                )
                .parent(laser_power)
                .requirements(RequirementsStrategy.OR)
                .addCriterion("code_trigger", IMPOSSIBLE)
                .save(consumer, NAMESPACE + ":precision_crafting");
    }

    @Override
    public String getName() {
        return "BuildCraft Silicon Advancement Generator";
    }
}
