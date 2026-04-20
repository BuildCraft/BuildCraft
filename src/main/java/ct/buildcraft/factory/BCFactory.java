package ct.buildcraft.factory;

import ct.buildcraft.factory.client.render.RenderDistiller;
import ct.buildcraft.factory.client.render.RenderHeatExchange;
import ct.buildcraft.factory.client.render.RenderMiningWell;
import ct.buildcraft.factory.client.render.RenderPump;
import ct.buildcraft.factory.client.render.RenderTank;
import ct.buildcraft.factory.tile.TileDistiller;
import ct.buildcraft.factory.tile.TileTank;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BCFactory.MODID)
public class BCFactory
{
    public static final String MODID = "buildcraftfactory";

    public BCFactory()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

/*        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::init);
        modEventBus.addListener(this::postInit);*/
//        modEventBus.addListener(this::gatherData);//DataGenerator
        BCFactoryBlocks.registry(modEventBus);
        BCFactoryItems.registry(modEventBus);
        BCFactoryGuis.registry(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        vaildID();
    }
    
    public void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
            event.includeServer(),
            new BCFactoryRecipesProvider(event.getGenerator())
        );
    }

    @SuppressWarnings("unused")
	private void vaildID() {
    	int i0 = TileTank.NET_FLUID_DELTA;
    	int i1 = TileDistiller.NET_TANK_GAS_OUT;
    	int i2 = TileDistiller.NET_TANK_IN;
    	int i3 = TileDistiller.NET_TANK_LIQUID_OUT;
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
        	BCFactorySprites.init();
        	BCFactoryModels.init();
        }
        
        @SubscribeEvent
        public static void registryRender(EntityRenderersEvent.RegisterRenderers e) {
        	e.registerBlockEntityRenderer(BCFactoryBlocks.ENTITYBLOCKTANK.get(), RenderTank::new);
        	e.registerBlockEntityRenderer(BCFactoryBlocks.ENTITYBLOCKPUMP.get(), RenderPump::new);
        	e.registerBlockEntityRenderer(BCFactoryBlocks.ENTITYBLOCKMININGWELL.get(), RenderMiningWell::new);
        	e.registerBlockEntityRenderer(BCFactoryBlocks.ENTITYBLOCKDISTILLER.get(), RenderDistiller::new);
        	e.registerBlockEntityRenderer(BCFactoryBlocks.ENTITYBLOCKHEATEXCHANGE.get(), RenderHeatExchange::new);
        }
        
        @SubscribeEvent
        public static void registrtTexture(Pre e){
        	if("textures/atlas/blocks.png".equals(e.getAtlas().location().getPath())) {
        		BCFactorySprites.registrtTexture(e);
        	}
        }
    }

    

}
