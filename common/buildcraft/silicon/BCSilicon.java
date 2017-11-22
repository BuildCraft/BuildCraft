/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import java.util.function.Consumer;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;

import buildcraft.core.BCCore;

//@formatter:off
@Mod(
    modid = BCSilicon.MODID,
    name = "BuildCraft Silicon",
    version = BCLib.VERSION,
    dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]"
)
//@formatter:on
public class BCSilicon {
    public static final String MODID = "buildcraftsilicon";

    @Mod.Instance(MODID)
    public static BCSilicon INSTANCE = null;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        RegistryConfig.useOtherModConfigFor(MODID, BCCore.MODID);

        BCSiliconConfig.preInit();
        BCSiliconBlocks.preInit();
        BCSiliconItems.preInit();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCSiliconProxy.getProxy());

        BCSiliconProxy.getProxy().fmlPreInit();
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCSiliconProxy.getProxy().fmlInit();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {

    }

    static {
        startBatch();
        // Items
        registerTag("item.redstone_chipset").reg("redstone_chipset").locale("redstone_chipset").model("redstone_chipset/");
        // Item Blocks
        registerTag("item.block.laser").reg("laser").locale("laserBlock").model("laser");
        registerTag("item.block.assembly_table").reg("assembly_table").locale("assemblyTableBlock").model("assembly_table");
        registerTag("item.block.advanced_crafting_table").reg("advanced_crafting_table").locale("assemblyWorkbenchBlock").model("advanced_crafting_table");
        registerTag("item.block.integration_table").reg("integration_table").locale("integrationTableBlock").model("integration_table");
        registerTag("item.block.charging_table").reg("charging_table").locale("chargingTableBlock").model("charging_table");
        registerTag("item.block.programming_table").reg("programming_table").locale("programmingTableBlock").model("programming_table");
        // Blocks
        registerTag("block.laser").reg("laser").oldReg("laserBlock").locale("laserBlock").model("laser");
        registerTag("block.assembly_table").reg("assembly_table").oldReg("assemblyTableBlock").locale("assemblyTableBlock").model("assembly_table");
        registerTag("block.advanced_crafting_table").reg("advanced_crafting_table").oldReg("advancedCraftingTableBlock").locale("assemblyWorkbenchBlock").model("advanced_crafting_table");
        registerTag("block.integration_table").reg("integration_table").oldReg("integrationTableBlock").locale("integrationTableBlock").model("integration_table");
        registerTag("block.charging_table").reg("charging_table").oldReg("chargingTableBlock").locale("chargingTableBlock").model("charging_table");
        registerTag("block.programming_table").reg("programming_table").oldReg("programmingTableBlock").locale("programmingTableBlock").model("programming_table");
        // Tiles
        registerTag("tile.laser").reg("laser");
        registerTag("tile.assembly_table").reg("assembly_table");
        registerTag("tile.advanced_crafting_table").reg("advanced_crafting_table");
        registerTag("tile.integration_table").reg("integration_table");
        registerTag("tile.charging_table").reg("charging_table");
        registerTag("tile.programming_table").reg("programming_table");

        endBatch(TagManager.prependTags("buildcraftsilicon:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION).andThen(TagManager.setTab("buildcraft.main")));
    }

    private static TagEntry registerTag(String id) {
        return TagManager.registerTag(id);
    }

    private static void startBatch() {
        TagManager.startBatch();
    }

    private static void endBatch(Consumer<TagEntry> consumer) {
        TagManager.endBatch(consumer);
    }
}
