package buildcraft.datagen.silicon;

import buildcraft.datagen.base.BCBaseItemModelGenerator;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.client.SiliconItemModelPredicates;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class SiliconItemModelGenerator extends BCBaseItemModelGenerator {
    public SiliconItemModelGenerator(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
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
        getBuilder(((Item) BCSiliconItems.plugFacade.get()).getRegistryName().toString()).parent(BUILTIN_ENTITY);

        // chipsets
        withExistingParent(BCSiliconItems.chipsetRedstone.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftsilicon:items/redstone_chipset/red");
        withExistingParent(BCSiliconItems.chipsetDiamond.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftsilicon:items/redstone_chipset/diamond");
        withExistingParent(BCSiliconItems.chipsetGold.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftsilicon:items/redstone_chipset/gold");
        withExistingParent(BCSiliconItems.chipsetIron.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftsilicon:items/redstone_chipset/iron");
        withExistingParent(BCSiliconItems.chipsetQuartz.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftsilicon:items/redstone_chipset/quartz");

        // tables
        withExistingParent(BCSiliconBlocks.advancedCraftingTable.get().getRegistryName().toString(), new ResourceLocation("buildcraftsilicon:block/table/advanced_crafting"));
        withExistingParent(BCSiliconBlocks.assemblyTable.get().getRegistryName().toString(), new ResourceLocation("buildcraftsilicon:block/table/assembly"));
        withExistingParent(BCSiliconBlocks.chargingTable.get().getRegistryName().toString(), new ResourceLocation("buildcraftsilicon:block/table/charging"));
        withExistingParent(BCSiliconBlocks.integrationTable.get().getRegistryName().toString(), new ResourceLocation("buildcraftsilicon:block/table/integration"));
        withExistingParent(BCSiliconBlocks.programmingTable.get().getRegistryName().toString(), new ResourceLocation("buildcraftsilicon:block/table/programming"));

        // laser
        withExistingParent(BCSiliconBlocks.laser.get().getRegistryName().toString(), new ResourceLocation("buildcraftsilicon:block/laser"));

        // gate_copier
        ResourceLocation gateCopier = BCSiliconItems.gateCopier.get().getRegistryName();
        getBuilder(gateCopier.toString())
                .override()
                .model(
                        withExistingParent(gateCopier.getNamespace() + ":item/" + gateCopier.getPath() + "/empty", GENERATED)
                                .texture("layer0", "buildcraftsilicon:items/gatecopier/empty")
                )
                .predicate(SiliconItemModelPredicates.PREDICATE_HAS_DATA, 0)
                .end()
                .override()
                .model(
                        withExistingParent(gateCopier.getNamespace() + ":item/" + gateCopier.getPath() + "/full", GENERATED)
                                .texture("layer0", "buildcraftsilicon:items/gatecopier/full")
                )
                .predicate(SiliconItemModelPredicates.PREDICATE_HAS_DATA, 1)
                .end()
        ;

        // redstoneCrystal
        withExistingParent(BCSiliconItems.redstoneCrystal.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftsilicon:items/redstone_crystal");
    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Silicon Item Model Generator";
    }
}
