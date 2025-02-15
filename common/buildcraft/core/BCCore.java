package buildcraft.core;

import buildcraft.core.client.CoreItemModelPredicates;
import buildcraft.core.marker.PathCache;
import buildcraft.core.marker.VolumeCache;
import buildcraft.lib.BCLibItems;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.Consumer;

//@formatter:off
//@Mod(
//        modid = BCCore.MODID,
//        name = "BuildCraft Core",
//        version = BCLib.VERSION,
//        updateJSON = "https://mod-buildcraft.com/version/versions.json",
//        dependencies = "required-after:buildcraftlib@[" + BCLib.VERSION + "]",
//        guiFactory = "buildcraft.core.client.ConfigGuiFactoryBC"
//)
//@formatter:on
@Mod(BCCore.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BCCore {
    public static final String MODID = "buildcraftcore";
    public static final String MOD_VERSION = ModList.get().getModContainerById(MODID).get().getModInfo().getVersion().toString();

    public static BCCore INSTANCE = null;

    public static CreativeTabManager.CreativeTabBC mainTab;

    static {
        BCLibItems.enableGuide();
        BCLibItems.enableDebugger();
    }

    public BCCore() {
        INSTANCE = this;
    }

    @SubscribeEvent
//    public static void preInit(FMLPreInitializationEvent event)
    public static void preInit(FMLConstructModEvent event) {
        BCCoreConfig.cinit();

//        CreativeTabBC tab = CreativeTabManager.createTab("buildcraft.main");
        mainTab = CreativeTabManager.createTab("buildcraft.main");

        BCCoreBlocks.preInit();
        BCCoreItems.preInit();
        BCCoreStatements.preInit();
//        BCCoreRecipes.fmlPreInit(); // 1.18.2: use datagen

        BCCoreProxy.getProxy().fmlPreInit();

//        tab.setItem(BCCoreItems.wrench);
        mainTab.setItem(BCCoreItems.wrench);

        // 1.18.2: the item object not created yet
//        setItemTab(BCLibItems.guide, tab);
//        setItemTab(BCLibItems.guideNote, tab);
//        setItemTab(BCLibItems.debugger, tab);

//        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCCoreProxy.getProxy());

//        OreDictionary.registerOre("craftingTableWood", Blocks.CRAFTING_TABLE); // 1.18.2: use datagen
        MinecraftForge.EVENT_BUS.register(BCCoreEventDist.INSTANCE);
//        BCCoreConfig.saveConfigs();
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
//        BCCoreConfig.saveConfigs();
        // Calen: moved to BCLib#<cinit>
//        BCLibItems.guide.setCreativeTab(CreativeTabManager.getTab("buildcraft.main"));

        BCCoreProxy.getProxy().fmlInit();

        MarkerCache.registerCache(VolumeCache.INSTANCE);
        MarkerCache.registerCache(PathCache.INSTANCE);
    }

    @SubscribeEvent
//    public static void postInit(FMLPostInitializationEvent event)
    public static void postInit(FMLLoadCompleteEvent event) {
//        BCCoreConfig.saveConfigs();
        BCCoreConfig.saveCoreConfigs();
        BCCoreConfig.saveObjConfigs();
        BCCoreProxy.getProxy().fmlPostInit();
        BCCoreConfig.postInit();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(BCCoreBlocks.markerVolume.get(), RenderType.cutout());
        CoreItemModelPredicates.register(event);
    }

    @SubscribeEvent
    public static void onRegisterEvent(RegisterEvent event) {
        ResourceKey<? extends Registry<?>> registry = event.getRegistryKey();
        if (registry == Registries.CREATIVE_MODE_TAB) {
            // Creative Tab
            Registry.register(event.getVanillaRegistry(), mainTab.getId(), mainTab);
        } else if (registry == Registries.BLOCK) {
            // GUI
            BCCoreMenuTypes.registerAll();
        }
    }

    // Calen: use TagManager object for thread safety
    // java.util.NoSuchElementException
    // at buildcraftcore.registry.TagManager.endBatch(TagManager.java:172) ~[%2387!/:?] {re:classloading}
    private static final TagManager tagManager = new TagManager();

    static {
        startBatch();
        // Items
//        registerTag("item.wrench").reg("wrench").locale("wrenchItem").model("wrench");
        registerTag("item.wrench").reg("wrench").locale("wrenchItem");
//        registerTag("item.diamond.shard").reg("diamond_shard").locale("diamondShard").model("diamond_shard").tab("vanilla.materials");
        registerTag("item.diamond_shard").reg("diamond_shard").locale("diamondShard").tab("vanilla.materials");
//        registerTag("item.gear.wood").reg("gear_wood").locale("woodenGearItem").oreDict("gearWood").oldReg("woodenGearItem").model("gears/wood");
        registerTag("item.gear.wood").reg("gear_wood").locale("woodenGearItem").oreDict("gearWood");
//        registerTag("item.gear.stone").reg("gear_stone").locale("stoneGearItem").oreDict("gearStone").oldReg("stoneGearItem").model("gears/stone");
        registerTag("item.gear.stone").reg("gear_stone").locale("stoneGearItem").oreDict("gearStone");
//        registerTag("item.gear.iron").reg("gear_iron").locale("ironGearItem").oreDict("gearIron").oldReg("ironGearItem").model("gears/iron");
        registerTag("item.gear.iron").reg("gear_iron").locale("ironGearItem").oreDict("gearIron");
//        registerTag("item.gear.gold").reg("gear_gold").locale("goldGearItem").oreDict("gearGold").oldReg("goldGearItem").model("gears/gold");
        registerTag("item.gear.gold").reg("gear_gold").locale("goldGearItem").oreDict("gearGold");
//        registerTag("item.gear.diamond").reg("gear_diamond").locale("diamondGearItem").oreDict("gearDiamond").oldReg("diamondGearItem").model("gears/diamond");
        registerTag("item.gear.diamond").reg("gear_diamond").locale("diamondGearItem").oreDict("gearDiamond");
//        registerTag("item.list").reg("list").locale("list").model("list_");
        registerTag("item.list").reg("list").locale("list");
//        registerTag("item.map_location").reg("map_location").locale("mapLocation").model("map_location/");
        registerTag("item.map_location").reg("map_location").locale("mapLocation");
//        registerTag("item.paintbrush").reg("paintbrush").locale("paintbrush").model("paintbrush/");
        registerTag("item.paintbrush.clean").reg("paintbrush_clean").locale("paintbrush");
        registerTag("item.paintbrush.white").reg("paintbrush_white").locale("paintbrush");
        registerTag("item.paintbrush.orange").reg("paintbrush_orange").locale("paintbrush");
        registerTag("item.paintbrush.magenta").reg("paintbrush_magenta").locale("paintbrush");
        registerTag("item.paintbrush.light_blue").reg("paintbrush_light_blue").locale("paintbrush");
        registerTag("item.paintbrush.yellow").reg("paintbrush_yellow").locale("paintbrush");
        registerTag("item.paintbrush.lime").reg("paintbrush_lime").locale("paintbrush");
        registerTag("item.paintbrush.pink").reg("paintbrush_pink").locale("paintbrush");
        registerTag("item.paintbrush.light_gray").reg("paintbrush_light_gray").locale("paintbrush");
        registerTag("item.paintbrush.cyan").reg("paintbrush_cyan").locale("paintbrush");
        registerTag("item.paintbrush.purple").reg("paintbrush_purple").locale("paintbrush");
        registerTag("item.paintbrush.blue").reg("paintbrush_blue").locale("paintbrush");
        registerTag("item.paintbrush.brown").reg("paintbrush_brown").locale("paintbrush");
        registerTag("item.paintbrush.green").reg("paintbrush_green").locale("paintbrush");
        registerTag("item.paintbrush.red").reg("paintbrush_red").locale("paintbrush");
        registerTag("item.paintbrush.black").reg("paintbrush_black").locale("paintbrush");
        registerTag("item.paintbrush.gray").reg("paintbrush_gray").locale("paintbrush");
        registerTag("item.marker_connector").reg("marker_connector").locale("markerConnector");
//                .model("marker_connector");
        registerTag("item.volume_box").reg("volume_box").locale("volume_box");
//                .model("volume_box");
        registerTag("item.goggles").reg("goggles").locale("goggles");
//                .model("goggles");
        registerTag("item.fragile_fluid_shard").reg("fragile_fluid_shard").locale("fragile_fluid_shard");
//                .model("fragile_fluid_shard");
        // Item Blocks
//        registerTag("item.block.marker.volume").reg("marker_volume").locale("marker_volume").oldReg("markerBlock").model("marker_volume");
        registerTag("item.block.marker.volume").reg("marker_volume").locale("markerBlock");
//                .model("marker_volume");
//        registerTag("item.block.marker.path").reg("marker_path").locale("marker_path").oldReg("pathMarkerBlock").model("marker_path");
        registerTag("item.block.marker.path").reg("marker_path").locale("pathMarkerBlock");
//                .model("marker_path");
        registerTag("item.block.spring.water").reg("spring_water").locale("spring.water");
//                .model("spring");
        registerTag("item.block.spring.oil").reg("spring_oil").locale("spring.oil");
//                .model("spring");
        registerTag("item.block.power_tester").reg("power_tester").locale("power_tester");
//                .model("power_tester");
        registerTag("item.block.decorated").reg("decorated").locale("decorated");
//                .model("decorated/");
        registerTag("item.block.engine.bc.wood").reg("engine_wood").locale("engineWood");
//                .model("");
        registerTag("item.block.engine.bc.creative").reg("engine_creative").locale("engineCreative");
//                .model("");
        // Blocks
        registerTag("block.spring.water").reg("spring_water").locale("spring.oil");
        registerTag("block.spring.oil").reg("spring_oil").locale("spring.oil");
        registerTag("block.decorated").reg("decorated").locale("decorated");
//        registerTag("block.engine.bc").reg("engine").locale("engineBlock");
        registerTag("block.engine.bc.wood").reg("engine_wood").locale("engineWood");
//        registerTag("block.engine.bc.wood").locale("engine_wood");
        // Calen: moved to energy
//        registerTag("block.engine.bc.stone").locale("engineStone");
////        registerTag("block.engine.bc.stone").locale("engine_stone");
//        registerTag("block.engine.bc.iron").locale("engineIron");
////        registerTag("block.engine.bc.iron").locale("engine_iron");
//        registerTag("block.engine.bc.creative").locale("engine_creative");
        registerTag("block.engine.bc.creative").reg("engine_creative").locale("engineCreative");
//        registerTag("block.marker.volume").reg("marker_volume").locale("marker_volume").oldReg("markerBlock").model("marker_volume");
        registerTag("block.marker.volume").reg("marker_volume").locale("markerBlock");
//        registerTag("block.marker.path").reg("marker_path").locale("marker_path").oldReg("pathMarkerBlock").model("marker_path");
        registerTag("block.marker.path").reg("marker_path").locale("pathMarkerBlock");
//        registerTag("block.power_tester").reg("power_tester").locale("power_tester").model("power_tester");
        registerTag("block.power_tester").reg("power_tester").locale("power_tester");
        // Tiles
//        registerTag("tile.marker.volume").reg("marker.volume").oldReg("buildcraft.builders.Marker", "Marker");
        registerTag("tile.marker.volume").reg("marker_volume");
//        registerTag("tile.marker.path").reg("marker.path");
        registerTag("tile.marker.path").reg("marker_path");
//        registerTag("tile.engine.wood").reg("engine.wood");
        registerTag("tile.engine.wood").reg("engine_wood");
//        registerTag("tile.engine.creative").reg("engine.creative");
        registerTag("tile.engine.creative").reg("engine_creative");
        registerTag("tile.power_tester").reg("power_tester");

//        endBatch(TagManager.prependTags("buildcraftcore:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION).andThen(TagManager.setTab("buildcraft.main")));
        endBatch(TagManager.prependTags("buildcraftcore:", EnumTagType.REGISTRY_NAME).andThen(TagManager.setTab("buildcraft.main")));
//        endBatch(TagManager.prependTags("buildcraftcore:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));
//        engine.model("");// Clear model so that subtypes can set it properly
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
