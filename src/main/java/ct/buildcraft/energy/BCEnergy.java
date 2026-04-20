package ct.buildcraft.energy;


import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import ct.buildcraft.core.BCCore;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(BCEnergy.MODID)
public class BCEnergy {
	public static final String MODID = "buildcraftenergy";
	static final Logger LOGGER = LogUtils.getLogger();
	
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    
    
    public BCEnergy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        //TEST_CODE_START
        try {
//			test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        //TEST_CODE_END
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);
        BCEnergyFluids.registry(modEventBus);
        BCEnergyBlocks.init(modEventBus);
        BCEnergyGuis.init();
        BCEnergyWorldGen.preInit(modEventBus);
        BCEnergyConfig.preInit();
        ModLoadingContext.get().registerConfig(Type.COMMON, BCEnergyConfig.config);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(BCEnergyClientProxy.class);
        // Register the Deferred Register to the mod event bus so blocks get registered
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        MENUS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in

 //       MinecraftForge.EVENT_BUS.register(EntityBlockPump::new);
    }
    private void commonSetup(final FMLCommonSetupEvent event)
    {
    	BCCore.tabFluids.setItem(BCEnergyFluids.OIL_BUCKET.get(0).get());
    	BCEnergyFluids.init();
    	BCEnergyRecipes.init();
    	BCEnergyConfig.reloadConfig(MODID);
//    	event.enqueueWork(() -> 
//    	if(BCEnergyWorldGen.isTerraBlenderLoaded) {
//	    	Regions.register(new BCOverWorldRegion(40));
//  	  	SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MODID, BCSurfaceRuleData.oilDesertRule());
//    	});
    	//event.enqueueWork(BCEnergyWorldGen::registryFeature);
        
    }
    public void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(),
            new BCEnergyRecipes.BCEnergyRecipeProvider(event.getGenerator()));
        event.getGenerator().addProvider(event.includeClient(), 
        	new BCEnergyProvider.BlockModel(event.getGenerator(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeClient(), 
            new BCEnergyProvider.BlockState(event.getGenerator(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeClient(), 
                new BCEnergyProvider.ItemModel(event.getGenerator(), event.getExistingFileHelper()));
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
    }
    







}
