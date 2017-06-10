/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import java.util.function.Consumer;

import net.minecraft.world.DimensionType;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCLog;

import buildcraft.lib.block.VanillaPaintHandlers;
import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.dimension.BlankWorldProvider;
import buildcraft.lib.dimension.DimensionRunner;
import buildcraft.lib.dimension.FakeWorldServer;
import buildcraft.lib.expression.ExpressionDebugManager;
import buildcraft.lib.item.ItemManager;
import buildcraft.lib.list.ListMatchHandlerFluid;
import buildcraft.lib.list.VanillaListHandlers;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.net.MessageContainer;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.MessageMarker;
import buildcraft.lib.net.MessageUpdateTile;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;
import buildcraft.lib.net.cache.MessageObjectCacheReply;
import buildcraft.lib.net.cache.MessageObjectCacheReq;
import buildcraft.lib.particle.MessageParticleVanilla;
import buildcraft.lib.registry.MigrationManager;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;

//@formatter:off
@Mod(modid = BCLib.MODID,
     name = "BuildCraft Lib",
     version = BCLib.VERSION,
     acceptedMinecraftVersions = "(gradle_replace_mcversion,)",
     dependencies = "required-after:forge@(gradle_replace_forgeversion,)")
//@formatter:on
public class BCLib {
    public static final String MODID = "buildcraftlib";
    public static final String VERSION = "${version}";
    public static final String MC_VERSION = "${mcversion}";
    public static final String GIT_COMMIT_HASH = "${git_commit_hash}";
    public static final String GIT_BRANCH = "${git_branch}";

    public static DimensionType blueprintDimensionType;
    public static final int DIMENSION_ID = -26042011;

    public static final boolean DEV = VERSION.startsWith("$") || Boolean.getBoolean("buildcraft.dev");

    public static DimensionRunner dimensionRunner;

    @Instance(MODID)
    public static BCLib INSTANCE;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        BCLog.logger.info("");
        BCLog.logger.info("Starting BuildCraft " + BCLib.VERSION);
        BCLog.logger.info("Copyright (c) the BuildCraft team, 2011-2017");
        BCLog.logger.info("http://www.mod-buildcraft.com");
        BCLog.logger.info("");

        ExpressionDebugManager.logger = BCLog.logger::info;

        BCModules.fmlPreInit();
        BCLibRegistries.fmlPreInit();
        BCLibProxy.getProxy().fmlPreInit();
        BCLibItems.fmlPreInit();

        BuildCraftObjectCaches.fmlPreInit();
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCLibProxy.getProxy());

        MessageManager.addMessageType(MessageUpdateTile.class, MessageUpdateTile.HANDLER, Side.CLIENT, Side.SERVER);
        MessageManager.addMessageType(MessageContainer.class, MessageContainer.HANDLER, Side.CLIENT, Side.SERVER);
        MessageManager.addMessageType(MessageMarker.class, MessageMarker.HANDLER, Side.CLIENT);
        MessageManager.addMessageType(MessageParticleVanilla.class, MessageParticleVanilla.HANDLER, Side.CLIENT);
        MessageManager.addMessageType(MessageObjectCacheReq.class, MessageObjectCacheReq.HANDLER, Side.SERVER);
        MessageManager.addMessageType(MessageObjectCacheReply.class, MessageObjectCacheReply.HANDLER, Side.CLIENT);

        MinecraftForge.EVENT_BUS.register(BCLibEventDist.INSTANCE);
        blueprintDimensionType = DimensionType.register("The dimension of blueprints", "", DIMENSION_ID, BlankWorldProvider.class, true);
        DimensionManager.registerDimension(DIMENSION_ID, blueprintDimensionType);
    }

    @Mod.EventHandler
    public static void serverStart(FMLServerStartingEvent event) {
        FakeWorldServer fake = new FakeWorldServer(event.getServer());
        fake.init();
        DimensionManager.setWorld(DIMENSION_ID, fake, event.getServer());
        dimensionRunner = new DimensionRunner(fake);
        dimensionRunner.start();
    }

    @Mod.EventHandler
    public static void serverStop(FMLServerStoppedEvent event) {
        FakeWorldServer.INSTANCE = null;
        dimensionRunner.terminate();
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCLibProxy.getProxy().fmlInit();

        BCLibRegistries.fmlInit();
        VanillaListHandlers.fmlInit();
        VanillaPaintHandlers.fmlInit();
        VanillaRotationHandlers.fmlInit();

        ItemManager.fmlInit();

        BCLibRecipes.fmlInit();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {
        BCLibProxy.getProxy().fmlPostInit();
        BuildCraftObjectCaches.fmlPostInit();
        MessageManager.fmlPostInit();
        VanillaListHandlers.fmlPostInit();
        MarkerCache.postInit();
        ListMatchHandlerFluid.fmlPostInit();
    }

    @Mod.EventHandler
    public static void missingMappings(FMLMissingMappingsEvent evt) {
        MigrationManager.INSTANCE.missingMappingEvent(evt);
    }

    static {
        startBatch();
        registerTag("item.guide").reg("guide").locale("guide").model("guide").tab("vanilla.misc");
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
