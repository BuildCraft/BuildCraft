/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import java.util.function.Consumer;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.api.EnumBuildCraftModule;
import buildcraft.api.core.BCLog;

import buildcraft.lib.block.VanillaPaintHandlers;
import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.chunkload.ChunkLoaderManager;
import buildcraft.lib.expression.ExpressionDebugManager;
import buildcraft.lib.expression.minecraft.ExpressionCompat;
import buildcraft.lib.fluid.FluidManager;
import buildcraft.lib.list.VanillaListHandlers;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;
import buildcraft.lib.registry.MigrationManager;
import buildcraft.lib.registry.RegistrationHelper;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;

//@formatter:off
@Mod(
    modid = BCLib.MODID,
    name = "BuildCraft Lib",
    version = BCLib.VERSION,
    updateJSON = "https://mod-buildcraft.com/version/versions.json",
    acceptedMinecraftVersions = "(gradle_replace_mcversion,)",
    dependencies = "required-after:forge@(gradle_replace_forgeversion,)"
)
//@formatter:on
public class BCLib {
    public static final String MODID = "buildcraftlib";
    public static final String VERSION = "${version}";
    public static final String MC_VERSION = "${mcversion}";
    public static final String GIT_BRANCH = "${git_branch}";
    public static final String GIT_COMMIT_HASH = "${git_commit_hash}";
    public static final String GIT_COMMIT_MSG = "${git_commit_msg}";
    public static final String GIT_COMMIT_AUTHOR = "${git_commit_author}";

    public static final boolean DEV = VERSION.startsWith("$") || Boolean.getBoolean("buildcraft.dev");

    @Instance(MODID)
    public static BCLib INSTANCE;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        BCLog.logger.info("");
        BCLog.logger.info("Starting BuildCraft " + BCLib.VERSION);
        BCLog.logger.info("Copyright (c) the BuildCraft team, 2011-2017");
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
        for (EnumBuildCraftModule module : EnumBuildCraftModule.VALUES) {
            if (module.isLoaded()) {
                BCLog.logger.info("  - " + module.name);
            }
        }
        BCLog.logger.info("Missing Modules:");
        for (EnumBuildCraftModule module : EnumBuildCraftModule.VALUES) {
            if (!module.isLoaded()) {
                BCLog.logger.info("  - " + module.name);
            }
        }
        BCLog.logger.info("");

        ExpressionDebugManager.logger = BCLog.logger::info;
        ExpressionCompat.setup();

        BCLibRegistries.fmlPreInit();
        BCLibProxy.getProxy().fmlPreInit();
        BCLibItems.fmlPreInit();

        BuildCraftObjectCaches.fmlPreInit();
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCLibProxy.getProxy());

        MinecraftForge.EVENT_BUS.register(BCLibEventDist.class);
        MinecraftForge.EVENT_BUS.register(MigrationManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FluidManager.class);

        ForgeChunkManager.setForcedChunkLoadingCallback(BCLib.INSTANCE, ChunkLoaderManager::rebindTickets);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCLibProxy.getProxy().fmlInit();

        BCLibRegistries.fmlInit();
        VanillaListHandlers.fmlInit();
        VanillaPaintHandlers.fmlInit();
        VanillaRotationHandlers.fmlInit();

        RegistrationHelper.registerOredictEntries();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {
        BCLibProxy.getProxy().fmlPostInit();
        BuildCraftObjectCaches.fmlPostInit();
        VanillaListHandlers.fmlPostInit();
        MarkerCache.postInit();
        MessageManager.fmlPostInit();
    }

    @Mod.EventHandler
    public static void serverStarting(FMLServerStartingEvent event) {
//        event.registerServerCommand(new CommandBuildCraft());
    }

    static {
        startBatch();
        registerTag("item.guide").reg("guide").locale("buildcraft.guide").model("guide").tab("vanilla.misc");
        registerTag("item.guide.note").reg("guide_note").locale("buildcraft.guide_note").model("guide_note")
            .tab("vanilla.misc");
        registerTag("item.debugger").reg("debugger").locale("debugger").model("debugger").tab("vanilla.misc");
        endBatch(TagManager.prependTags("buildcraftlib:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));
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
