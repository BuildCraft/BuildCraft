/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.builders.schematic.GlobalSavedDataSnapshots;
import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.builders.schematic.RulesLoader;
import buildcraft.builders.schematic.SchematicsLoader;
import buildcraft.core.marker.volume.AddonsRegistry;
import net.minecraft.util.ResourceLocation;
import java.util.function.Consumer;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.builders.bpt.PerSaveBptStorage;
import buildcraft.core.BCCore;
import buildcraft.lib.BCLib;
import buildcraft.lib.registry.RegistryHelper;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;

//@formatter:off
@Mod(modid = BCBuilders.MODID,
   name = "BuildCraft Builders",
   version = BCLib.VERSION,
   dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]")
//@formatter:on
public class BCBuilders {
    public static final String MODID = "buildcraftbuilders";

    @Mod.Instance(MODID)
    public static BCBuilders INSTANCE = null;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);

        BCBuildersItems.preInit();
        BCBuildersBlocks.preInit();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCBuildersProxy.getProxy());
        AddonsRegistry.INSTANCE.register(new ResourceLocation("buildcraftbuilders", "filling_planner"), AddonFillingPlanner.class);
        BCBuildersProxy.getProxy().fmlPreInit();
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCBuildersProxy.getProxy().fmlInit();
        BCBuildersRecipes.init();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {
        RulesLoader.INSTANCE.loadAll();
        SchematicsLoader.INSTANCE.loadAll();
        GlobalSavedDataSnapshots.get(evt.getSide());
    }

    @Mod.EventHandler
    public static void onServerStopping(FMLServerStoppingEvent event) {
        PerSaveBptStorage.onServerStopping();
    }

    static {
        startBatch();
        // Items
        registerTag("item.schematic.single").reg("schematic_single").locale("schematicSingle").model("schematic_single/");
        registerTag("item.blueprint").reg("blueprint").locale("blueprintItem").model("blueprint/");
        registerTag("item.snapshot").reg("snapshot").locale("snapshot").model("snapshot/");
        registerTag("item.filling_planner").reg("filling_planner").locale("fillingPlannerItem").model("filling_planner");
        // Item Blocks
        registerTag("item.block.architect").reg("architect").locale("architectBlock").model("architect");
        registerTag("item.block.builder").reg("builder").locale("builderBlock").model("builder");
        registerTag("item.block.filler").reg("filler").locale("fillerBlock").model("filler");
        registerTag("item.block.library").reg("library").locale("libraryBlock").model("library");
        registerTag("item.block.frame").reg("frame").locale("frameBlock").model("frame");
        registerTag("item.block.quarry").reg("quarry").locale("quarryBlock").model("quarry");
        // Blocks
        registerTag("block.architect").reg("architect").locale("architectBlock").model("architect");
        registerTag("block.builder").reg("builder").locale("builderBlock").model("builder");
        registerTag("block.filler").reg("filler").locale("fillerBlock").model("filler");
        registerTag("block.library").reg("library").locale("libraryBlock").model("library");
        registerTag("block.frame").reg("frame").locale("frameBlock").model("frame");
        registerTag("block.quarry").reg("quarry").locale("quarryBlock").model("quarry");
        // Tiles
        registerTag("tile.architect").reg("architect");
        registerTag("tile.library").reg("library");
        registerTag("tile.builder").reg("builder");
        registerTag("tile.filler").reg("filler");
        registerTag("tile.quarry").reg("quarry");

        endBatch(TagManager.prependTags("buildcraftbuilders:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION).andThen(TagManager.setTab("buildcraft.main")));
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
