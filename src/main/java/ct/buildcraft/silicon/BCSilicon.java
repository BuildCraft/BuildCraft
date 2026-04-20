/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon;

import ct.buildcraft.api.BCModules;
import ct.buildcraft.api.facades.FacadeAPI;
import ct.buildcraft.builders.BCBuildersConfig;
import ct.buildcraft.lib.CreativeTabManager;
import ct.buildcraft.lib.CreativeTabManager.CreativeTabBC;
import ct.buildcraft.silicon.plug.FacadeStateManager;
import ct.buildcraft.transport.BCTransport;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BCSilicon.MODID)
public class BCSilicon {
    public static final String MODID = "buildcraftsilicon";

    public static CreativeTabBC tabPlugs = BCTransport.tabPlugs;
    public static CreativeTabBC tabFacades = (CreativeTabBC) CreativeTabManager.createTab("buildcraft.facades").setRecipeFolderName("facades");
    
    public BCSilicon() {
 //       RegistryConfig.useOtherModConfigFor(MODID, BCCore.MODID);

    	IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    	modEventBus.addListener(BCSilicon::commonSetup);
    	modEventBus.addListener(BCSilicon::postInit);
    	modEventBus.addListener(BCSilicon::gatherData);
    	BCSiliconSprites.fmlPreInit();
        FacadeAPI.registry = FacadeStateManager.INSTANCE;

        BCSiliconConfig.preInit();
        BCSiliconStatements.preInit();
        BCSiliconPlugs.preInit();
        BCSiliconBlocks.registry(modEventBus);
        BCSiliconItems.registry(modEventBus);
        BCSiliconGuis.preInit(modEventBus);
        BCSiliconRecipes.preInit(modEventBus);
        


        ModLoadingContext.get().registerConfig(Type.COMMON, BCSiliconConfig.config);
        MinecraftForge.EVENT_BUS.register(this);
 //       NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCSiliconProxy.getProxy());

    }

    public static void commonSetup(FMLCommonSetupEvent evt) {
        FacadeStateManager.init();
        BCBuildersConfig.reloadConfig(MODID);
    }

    public static void postInit(FMLLoadCompleteEvent evt) {
 /*       if (BCSiliconItems.plugFacade != null) {
            FacadeBlockStateInfo state = FacadeStateManager.previewState;
            FacadeInstance inst = FacadeInstance.createSingle(state, false);
            tabFacades.setItem(BCSiliconItems.plugFacade.createItemStack(inst));
        }*/

        if (!BCModules.TRANSPORT.isLoaded()) {
            tabPlugs.setItem(BCSiliconItems.PLUG_GATE_ITEM.get());
        }
    }
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
            event.includeServer(),
            new BCSiliconRecipesProvider(event.getGenerator())
        );
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        public ClientModEvents() {
        	BCSiliconSprites.fmlPreInit();
            BCSiliconModels.fmlPreInit();
        }
        
    	@SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
    		BCSiliconGuis.clientInit(event);
            event.enqueueWork(BCSiliconItems::registerItemProperties);
    		BCSiliconModels.init();
        }
        
        @SubscribeEvent
        public static void registryRender(EntityRenderersEvent.RegisterRenderers e) {
        	BCSiliconModels.onBlockEntityRender(e);
        	
        }
        
        @SubscribeEvent
        public static void RegisterItemColor(RegisterColorHandlersEvent.Item event) {
        	BCSiliconModels.RegisterItemColor(event);
        }
        
        @SubscribeEvent
        public static void onModelBake(BakingCompleted event) {
        	BCSiliconModels.onModelBake(event);
        }
        	
    }


}
