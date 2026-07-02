/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.lib;

import ct.buildcraft.api.BCModules;
import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.compat.ic2.Ic2Compat;
import ct.buildcraft.lib.block.VanillaRotationHandlers;
import ct.buildcraft.lib.expression.ExpressionDebugManager;
import ct.buildcraft.lib.list.VanillaListHandlers;
import ct.buildcraft.lib.marker.MarkerCache;
import ct.buildcraft.lib.misc.ExpressionCompat;
import ct.buildcraft.lib.net.MessageManager;
import ct.buildcraft.lib.net.cache.BuildCraftObjectCaches;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BCLib.MODID)
public class BCLib {
    public static final String MODID = "buildcraftlib";
    public static final String VERSION = "$version";
    public static final String MC_VERSION = "${mcversion}";
    public static final String GIT_BRANCH = "${git_branch}";
    public static final String GIT_COMMIT_HASH = "${git_commit_hash}";
    public static final String GIT_COMMIT_MSG = "${git_commit_msg}";
    public static final String GIT_COMMIT_AUTHOR = "${git_commit_author}";

    public static final boolean DEV = true;// = VERSION.startsWith("$") || Boolean.getBoolean("buildcraft.dev");

    public BCLib() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::init);
        modEventBus.addListener(this::postInit);
//        modEventBus.addListener(this::gatherData);//DataGenerator

        try {
            BCLog.logger.info("");
        } catch (NoSuchFieldError e) {
            throw throwBadClass(e, BCLog.class);
        }
        BCLog.logger.info("Starting BuildCraft " + BCLib.VERSION);
        BCLog.logger.info("Copyright (c) the BuildCraft team, 2011-2018");
        BCLog.logger.info("https://www.mod-buildcraft.com");
        if (!GIT_COMMIT_HASH.startsWith("${")) {
            BCLog.logger.info("Detailed Build Information:");
            BCLog.logger.info("  Branch " + GIT_BRANCH);
            BCLog.logger.info("  Commit " + GIT_COMMIT_HASH);
            BCLog.logger.info("    " + GIT_COMMIT_MSG);
            BCLog.logger.info("    committed by " + GIT_COMMIT_AUTHOR);
        }
        BCLog.logger.info("");
        BCLog.logger.info("Loaded Modules:");
        for (BCModules module : BCModules.VALUES) {
            if (module.isLoaded()) {
                BCLog.logger.info("  - " + module.lowerCaseName);
            }
        }
        BCLog.logger.info("Missing Modules:");
        for (BCModules module : BCModules.VALUES) {
            if (!module.isLoaded()) {
                BCLog.logger.info("  - " + module.lowerCaseName);
            }
        }
        BCLibRegistries.fmlPreInit();

        ExpressionDebugManager.logger = BCLog.logger::info;
        ExpressionCompat.setup();

        
        
        
//        BCLibItems.fmlPreInit();

        BuildCraftObjectCaches.fmlPreInit();
//        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCLibProxy.getProxy());

//        MinecraftForge.EVENT_BUS.register(MigrationManager.INSTANCE);
  //      MinecraftForge.EVENT_BUS.register(FluidManager);
        //TODO
        // Set max chunk limit for quarries: 1 chunk for quarry itself and 5 * 5 chunks square for working area
//        ForgeChunkManager.getConfig().get(MODID, "maximumChunksPerTicket", 26);
 //       ForgeChunkManager.syncConfigDefaults();
 //      ForgeChunkManager.setForcedChunkLoadingCallback(BCLib.MODID, ChunkLoaderManager::rebindTickets);

        ExpressionDebugManager.logger = BCLog.logger::info;
        ExpressionCompat.setup();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(BCLibEventDist.class);
        
        if(ModList.get().isLoaded("ic2"))
        	Ic2Compat.preInit();
    }

    public void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), new BCTagsProvider.BlockTag(event.getGenerator(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeServer(), new BCTagsProvider.FluidTag(event.getGenerator(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeServer(), new BCTagsProvider.BiomeTag(event.getGenerator(), event.getExistingFileHelper()));
    }

    public void init(final FMLCommonSetupEvent event) {
    	BCLibRegistries.fmlInit();
    	BCLibProxy.MessageRegistry();
    	VanillaListHandlers.fmlInit();
  //  	VanillaPaintHandlers.fmlInit();
        VanillaRotationHandlers.fmlInit();
    }
    
    public void postInit(FMLLoadCompleteEvent evt) {
    	
//        ReloadableRegistryManager.loadAll();

//        VanillaListHandlers.fmlPostInit();
        MarkerCache.postInit();
    	BuildCraftObjectCaches.fmlPostInit();
    	MessageManager.fmlPostInit();
        if(ModList.get().isLoaded("ic2"))
        	Ic2Compat.init();
    }

    public static Error throwBadClass(Error e, Class<?> cls) throws Error {
        throw new Error(
            "Bad " + cls + " loaded from " + cls.getClassLoader() + " domain: " + cls.getProtectionDomain(), e
        );
    }

}
