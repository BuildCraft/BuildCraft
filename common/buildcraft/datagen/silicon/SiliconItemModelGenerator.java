package buildcraft.datagen.silicon;

import buildcraft.datagen.base.BCBaseItemModelGenerator;
import buildcraft.silicon.BCSilicon;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.client.SiliconItemModelPredicates;
import buildcraft.silicon.item.ItemPluggableFacade;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class SiliconItemModelGenerator extends BCBaseItemModelGenerator {
    public SiliconItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BCSilicon.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // gates
        BCSiliconItems.variantGateMap.values().forEach(
                reg ->
                        getBuilder(reg.get().getRegistryName().toString()).parent(BUILTIN_ENTITY)
        );

        // plugLens
        getBuilder(BCSiliconItems.plugLens.get().getRegistryName().toString()).parent(BUILTIN_ENTITY);
        // plugPulsar
        getBuilder(BCSiliconItems.plugPulsar.get().getRegistryName().toString()).parent(BUILTIN_ENTITY);
        // plugLightSensor
        getBuilder(BCSiliconItems.plugLightSensor.get().getRegistryName().toString()).parent(BUILTIN_ENTITY);
        // plugFacade
        getBuilder(((ItemPluggableFacade) BCSiliconItems.plugFacade.get()).getRegistryName().toString()).parent(BUILTIN_ENTITY);

        // chipsets
        withExistingParent(BCSiliconItems.chipsetRedstone.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftsilicon:item/redstone_chipset/red");
        withExistingParent(BCSiliconItems.chipsetDiamond.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftsilicon:item/redstone_chipset/diamond");
        withExistingParent(BCSiliconItems.chipsetGold.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftsilicon:item/redstone_chipset/gold");
        withExistingParent(BCSiliconItems.chipsetIron.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftsilicon:item/redstone_chipset/iron");
        withExistingParent(BCSiliconItems.chipsetQuartz.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftsilicon:item/redstone_chipset/quartz");

        // tables
        withExistingParent(BCSiliconBlocks.advancedCraftingTable.get().getRegistryName().toString(), new ResourceLocation("buildcraftsilicon:block/table/advanced_crafting"));
        withExistingParent(BCSiliconBlocks.assemblyTable.get().getRegistryName().toString(), new ResourceLocation("buildcraftsilicon:block/table/assembly"));
        withExistingParent(BCSiliconBlocks.chargingTable.get().getRegistryName().toString(), new ResourceLocation("buildcraftsilicon:block/table/charging"));
        withExistingParent(BCSiliconBlocks.integrationTable.get().getRegistryName().toString(), new ResourceLocation("buildcraftsilicon:block/table/integration"));
        withExistingParent(BCSiliconBlocks.programmingTable.get().getRegistryName().toString(), new ResourceLocation("buildcraftsilicon:block/table/programming"))
                .texture("glass", "minecraft:block/white_stained_glass");

        // laser
        withExistingParent(BCSiliconBlocks.laser.get().getRegistryName().toString(), new ResourceLocation("buildcraftsilicon:block/laser"));

        // gate_copier
        ResourceLocation gateCopier = BCSiliconItems.gateCopier.get().getRegistryName();
        getBuilder(gateCopier.toString())
                .override()
                .model(
                        withExistingParent(gateCopier.getNamespace() + ":item/" + gateCopier.getPath() + "/empty", GENERATED)
                                .texture("layer0", "buildcraftsilicon:item/gatecopier/empty")
                )
                .predicate(SiliconItemModelPredicates.PREDICATE_HAS_DATA, 0)
                .end()
                .override()
                .model(
                        withExistingParent(gateCopier.getNamespace() + ":item/" + gateCopier.getPath() + "/full", GENERATED)
                                .texture("layer0", "buildcraftsilicon:item/gatecopier/full")
                )
                .predicate(SiliconItemModelPredicates.PREDICATE_HAS_DATA, 1)
                .end()
        ;
    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Silicon Item Model Generator";
    }
}
