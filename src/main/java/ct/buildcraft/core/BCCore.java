package ct.buildcraft.core;

import java.util.HashMap;
import java.util.Map;

import ct.buildcraft.api.BCModules;
import ct.buildcraft.api.enums.EnumSpring;
import ct.buildcraft.api.items.FluidItemDrops;
import ct.buildcraft.core.client.RenderTickListener;
import ct.buildcraft.core.client.model.ModelEngine;
import ct.buildcraft.core.client.render.RenderEngine_BC8;
import ct.buildcraft.core.client.render.RenderMarkerVolume;
import ct.buildcraft.core.client.render.RenderVolumeBoxes;
import ct.buildcraft.core.list.ContainerList;
import ct.buildcraft.core.list.GuiList;
import ct.buildcraft.core.marker.PathCache;
import ct.buildcraft.core.marker.VolumeCache;
import ct.buildcraft.core.marker.volume.MessageVolumeBoxes;
import ct.buildcraft.energy.BCEnergyFluids;
import ct.buildcraft.energy.tile.TileSpringOil;
import ct.buildcraft.lib.BCLibEventDist;
import ct.buildcraft.lib.CreativeTabManager;
import ct.buildcraft.lib.CreativeTabManager.CreativeTabBC;
import ct.buildcraft.lib.client.render.DetachedRenderer;
import ct.buildcraft.lib.client.render.DetachedRenderer.RenderMatrixType;
import ct.buildcraft.lib.marker.MarkerCache;
import ct.buildcraft.lib.net.MessageManager;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(BCCore.MODID)
public class BCCore {
	public static final String MODID = "buildcraftcore";
    public static final CreativeTabBC BUILDCRAFT_TAB = CreativeTabManager.createTab("buildcraft.main");
    public static final CreativeTabBC tabFluids = CreativeTabManager.createTab("buildcraft.fluid");
	
    public static final Map<String,Object> ENGINE_MAP = new HashMap<>();
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BCCore.MODID);
    public static final RegistryObject<MenuType<ContainerList>> LIST_MENU = MENUS.register("list_menu", () -> new MenuType<>(ContainerList::new));
    
    public BCCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::init);
//        modEventBus.addListener(this::gatherData);//DataGenerator

        BCCoreBlocks.registry(modEventBus);
        BCCoreItems.registry(modEventBus);

        MENUS.register(modEventBus);
//        BCCoreRecipes.init();
        BCCoreConfig.registry();
        ModLoadingContext.get().registerConfig(Type.COMMON, BCCoreConfig.config);
        MessageManager.registerMessageClass(BCModules.CORE, MessageVolumeBoxes.class, MessageVolumeBoxes.HANDLER, MessageVolumeBoxes::toBytes, MessageVolumeBoxes::new/*, Side.CLIENT*/);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(BCLibEventDist.class);
		IEventBus eventBus = MinecraftForge.EVENT_BUS;
		eventBus.addListener(RenderTickListener::renderOverlay);
		eventBus.addListener(RenderTickListener::renderLast);
		BCCoreStatements.preInit();
    }

    public void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
            event.includeServer(),
            new BCCoreRecipes(event.getGenerator())
        );
    }
    
    public void init(final FMLCommonSetupEvent event)
    {
        MarkerCache.registerCache(VolumeCache.INSTANCE);
        MarkerCache.registerCache(PathCache.INSTANCE);
    	EnumSpring.OIL.liquidBlock = BCEnergyFluids.OIL_BLOCK.get(0).get().defaultBlockState();
    	EnumSpring.OIL.tileConstructor = TileSpringOil::new;
    	BCCoreConfig.reloadConfig(MODID);
        BUILDCRAFT_TAB.setItem(BCCoreItems.WRENCH.get());
        FluidItemDrops.item = BCCoreItems.FRAGILE_FLUID_SHARD.get();
    }
    
    
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
    	public static final ResourceLocation TRUNK_LIGHT = new ResourceLocation("buildcraftcore:blocks/engine/trunk_light");
    	public static final ResourceLocation CHAMBER = new ResourceLocation("buildcraftlib:blocks/engine/chamber_base");
    	public static final ResourceLocation TRUNK = new ResourceLocation("buildcraftcore:blocks/engine/trunk");
    	public static final ResourceLocation ENGINE_MODEL = new ResourceLocation("buildcraftlib:block/engine_base");
    	
    	public ClientModEvents() {

		}
    	
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
        	BCCoreSprites.init();
        	DetachedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_WORLD_ORIGIN, RenderVolumeBoxes.INSTANCE);
            event.enqueueWork(
                    () -> {
                    	BCCoreItems.registerItemProperties();
                    	MenuScreens.register(LIST_MENU.get(), GuiList::new);
                    }
            );
        }
    	
        @SubscribeEvent
        public static void registryRender(EntityRenderersEvent.RegisterRenderers e) {

        	e.registerBlockEntityRenderer(BCCoreBlocks.ENGINE_REDSTONE_TILE_BC8.get(), RenderEngine_BC8::new);
        	e.registerBlockEntityRenderer(BCCoreBlocks.ENGINE_CREATIVE_TILE_BC8.get(), RenderEngine_BC8::new);
        	e.registerBlockEntityRenderer(BCCoreBlocks.MARKER_VOLUME_TILE_BC8.get(), RenderMarkerVolume::new);
        }
        
        @SubscribeEvent
        public static void registrtTexture(Pre e){
        	
        	if("textures/atlas/blocks.png".equals(e.getAtlas().location().getPath())) {
        		e.addSprite(TRUNK_LIGHT);
        		e.addSprite(CHAMBER);
        	}
        }
        
        @SubscribeEvent
        public static void onModelBakePre(RegisterAdditional event) {
        	event.register(ENGINE_MODEL);
        }
        
        @SubscribeEvent
        public static void onModelBake(BakingCompleted event) {
        	ModelEngine.init(event.getModels().get(ENGINE_MODEL));
        	event.getModels().put(new ModelResourceLocation("buildcraftcore:engine#type=wood"), new ModelEngine(RenderEngine_BC8.REDSTONE_BACK, RenderEngine_BC8.REDSTONE_SIDE));
        	event.getModels().put(new ModelResourceLocation("buildcraftcore:engine#type=creative"), new ModelEngine(RenderEngine_BC8.CREATIVE_BACK, RenderEngine_BC8.CREATIVE_SIDE));
        	event.getModels().put(new ModelResourceLocation("buildcraftcore:engine#type=stone"), new ModelEngine(RenderEngine_BC8.STONE_BACK, RenderEngine_BC8.STONE_SIDE));
        	event.getModels().put(new ModelResourceLocation("buildcraftcore:engine#type=iron"), new ModelEngine(RenderEngine_BC8.IRON_BACK, RenderEngine_BC8.IRON_SIDE));
        	ModelEngine.release();
        }
        
        @SubscribeEvent
        public static void RegisterItemColor(RegisterColorHandlersEvent.Item event) {
        	event.register(new DynamicFluidContainerModel.Colors(), BCCoreItems.FRAGILE_FLUID_SHARD.get());
        }
        
    }


}

