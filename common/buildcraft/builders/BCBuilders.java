/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.builders.client.BuildersItemModelPredicates;
import buildcraft.builders.client.render.RenderArchitectTable;
import buildcraft.builders.client.render.RenderBuilder;
import buildcraft.builders.client.render.RenderFiller;
import buildcraft.builders.client.render.RenderQuarry;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.RulesLoader;
import buildcraft.core.BCCore;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.Consumer;

//@formatter:off
//@Mod(
//    modid = BCBuilders.MODID,
//    name = "BuildCraft Builders",
//    version = BCLib.VERSION,
//    dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]"
//)
@Mod(BCBuilders.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
//@formatter:on
public class BCBuilders {
    public static final String MODID = "buildcraftbuilders";

    // @Mod.Instance(MODID)
    public static BCBuilders INSTANCE = null;

    public BCBuilders() {
        INSTANCE = this;
    }

    @SubscribeEvent
//    public static void preInit(FMLPreInitializationEvent evt)
    public static void preInit(FMLConstructModEvent evt) {
        RegistryConfig.useOtherModConfigFor(MODID, BCCore.MODID);

        BCBuildersConfig.preInit();
        BCBuildersRegistries.preInit();
        BCBuildersItems.fmlPreInit();
        BCBuildersBlocks.fmlPreInit();
        BCBuildersStatements.preInit();
        BCBuildersSchematics.preInit();

//        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCBuilQeventdersProxy.getProxy());

        BCBuildersProxy.getProxy().fmlPreInit();

        MinecraftForge.EVENT_BUS.register(BCBuildersEventDist.INSTANCE);
    }

    @SubscribeEvent
//    public static void init(FMLInitializationEvent evt)
    public static void init(FMLCommonSetupEvent evt) {
        BCBuildersProxy.getProxy().fmlInit();
        BCBuildersRegistries.init();
//        BCBuildersRecipes.init(); //
//        BCBuildersBlocks.fmlInit(); // 1.18.2: tiles reg with blocks together
    }

    @SubscribeEvent
//    public static void postInit(FMLPostInitializationEvent evt)
    public static void postInit(FMLLoadCompleteEvent evt) {
        BCBuildersConfig.saveConfigs();
        BCBuildersProxy.getProxy().fmlPostInit();
        RulesLoader.loadAll();
        BCBuildersConfig.saveConfigs();
    }

    @SubscribeEvent
    @OnlyIn(Dist.DEDICATED_SERVER)
//    public static void onServerStarting(FMLServerStartingEvent event)
    public static void onServerStarting(FMLDedicatedServerSetupEvent event) {
        GlobalSavedDataSnapshots.reInit(Dist.DEDICATED_SERVER);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onTesrReg(RegisterRenderers event) {
        BlockEntityRenderers.register(BCBuildersBlocks.architectTile.get(), RenderArchitectTable::new);
        BlockEntityRenderers.register(BCBuildersBlocks.builderTile.get(), RenderBuilder::new);
        BlockEntityRenderers.register(BCBuildersBlocks.fillerTile.get(), RenderFiller::new);
        BlockEntityRenderers.register(BCBuildersBlocks.quarryTile.get(), RenderQuarry::new);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(BCBuildersBlocks.frame.get(), RenderType.cutout());
        BuildersItemModelPredicates.register(event);
    }

    @SubscribeEvent
    public static void onRegisterEvent(RegisterEvent event) {
        ResourceKey<? extends Registry<?>> registry = event.getRegistryKey();
        if (registry == Registries.BLOCK) {
            // GUI
            BCBuildersMenuTypes.registerAll();
        }
    }

    private static final TagManager tagManager = new TagManager();

    static {
        startBatch();
        // Items
//        registerTag("item.schematic.single").reg("schematic_single").locale("schematicSingle").model("schematic_single/");
        registerTag("item.schematic.single").reg("schematic_single").locale("schematicSingle");
//        registerTag("item.snapshot").reg("snapshot").locale("snapshot").model("snapshot/");
        registerTag("item.snapshot.blueprint").reg("snapshot_blueprint").locale("snapshot");
//        registerTag("item.snapshot.blueprint.clean").reg("snapshot_blueprint_clean").locale("snapshot").model("snapshot/");
//        registerTag("item.snapshot.blueprint.used").reg("snapshot_blueprint_used").locale("snapshot").model("snapshot/");
        registerTag("item.snapshot.template").reg("snapshot_template").locale("snapshot");
//        registerTag("item.snapshot.template.clean").reg("snapshot_template_clean").locale("snapshot").model("snapshot/");
//        registerTag("item.snapshot.template.used").reg("snapshot_template_used").locale("snapshot").model("snapshot/");
//        registerTag("item.filler_planner").reg("filler_planner").oldReg("filling_planner").locale("buildcraft.filler_planner").model("filler_planner");
        registerTag("item.filler_planner").reg("filler_planner").locale("buildcraft.filler_planner");
        // Item Blocks
        registerTag("item.block.architect").reg("architect").locale("architectBlock");
//                .model("architect");
        registerTag("item.block.builder").reg("builder").locale("builderBlock");
//                .model("builder");
        registerTag("item.block.filler").reg("filler").locale("fillerBlock");
//                .model("filler");
        registerTag("item.block.library").reg("library").locale("libraryBlock");
//                .model("library");
        registerTag("item.block.replacer").reg("replacer").locale("replacerBlock");
//                .model("replacer");
        registerTag("item.block.frame").reg("frame").locale("frameBlock");
//                .model("frame");
        registerTag("item.block.quarry").reg("quarry").locale("quarryBlock");
//                .model("quarry");
        // Blocks
        registerTag("block.architect").reg("architect").locale("architectBlock");
//                .model("architect");
        registerTag("block.builder").reg("builder").locale("builderBlock");
//                .model("builder");
        registerTag("block.filler").reg("filler").locale("fillerBlock");
//                .model("filler");
        registerTag("block.library").reg("library").locale("libraryBlock");
//                .model("library");
        registerTag("block.replacer").reg("replacer").locale("replacerBlock");
//                .model("replacer");
        registerTag("block.frame").reg("frame").locale("frameBlock");
//                .model("frame");
        registerTag("block.quarry").reg("quarry").locale("quarryBlock");
//                .model("quarry");
        // Tiles
        registerTag("tile.architect").reg("architect");
        registerTag("tile.builder").reg("builder");
        registerTag("tile.library").reg("library");
        registerTag("tile.replacer").reg("replacer");
        registerTag("tile.filler").reg("filler");
        registerTag("tile.quarry").reg("quarry");

//        endBatch(TagManager.prependTags("buildcraftbuilders:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION).andThen(TagManager.setTab("buildcraft.main")));
        endBatch(TagManager.prependTags("buildcraftbuilders:", EnumTagType.REGISTRY_NAME).andThen(TagManager.setTab("buildcraft.main")));
    }

    private static TagEntry registerTag(String id) {
//        return TagManager.registerTag(id);
        return tagManager.registerTag(id);
    }

    private static void startBatch() {
//        TagManager.startBatch();
        tagManager.startBatch();
    }

    private static void endBatch(Consumer<TagEntry> consumer) {
//        TagManager.endBatch(consumer);
        tagManager.endBatch(consumer);
    }
}
