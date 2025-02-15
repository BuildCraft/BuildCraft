package buildcraft.lib;

import buildcraft.api.registry.EventBuildCraftReload;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.command.CommandBuildCraft;
import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.debug.ClientDebuggables;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.item.ItemDebugger;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.misc.FakePlayerProvider;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.MessageDebugRequest;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public enum BCLibEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer) {
            ServerPlayer playerMP = (ServerPlayer) entity;
            // Delay sending join messages to player as it makes it work when in single-player
            MessageUtil.doDelayedServer(() -> MarkerCache.onPlayerJoinWorld(playerMP));
        }
    }

    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (levelAccessor instanceof Level level) {
            MarkerCache.onWorldUnload(level);
            if (level instanceof ServerLevel serverLevel) {
                FakePlayerProvider.INSTANCE.unloadWorld(serverLevel);
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onReloadFinish(EventBuildCraftReload.FinishLoad event) {
        // Note: when you need to add server-side listeners the client listeners need to be moved to BCLibProxy
        GuideManager.INSTANCE.onRegistryReload(event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
//    public static void onConnectToServer(ClientConnectedToServerEvent event)
    public void onConnectToServer(ClientPlayerNetworkEvent.LoggingIn event) {
        BuildCraftObjectCaches.onClientJoinServer();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void renderWorldLast(RenderLevelStageEvent event) {
        // Calen 1.20.1:
        // AFTER_SKY is the correct state for this render is use dynamic laser
        // AFTER_TRANSLUCENT_BLOCKS is the correct state for this render is use static laser. if AFTER_CUTOUT_BLOCKS, quarry debug cuboids will hide translucent blocks behind them
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        float partialTicks = event.getPartialTick();

        DetachedRenderer.INSTANCE.renderWorldLastEvent(player, partialTicks, event.getPoseStack(), event.getCamera());
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            BCAdvDebugging.INSTANCE.onServerPostTick();
            MessageUtil.postServerTick();
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            BuildCraftObjectCaches.onClientTick();
            MessageUtil.postClientTick();
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player != null && ItemDebugger.isShowDebugInfo(player)) {
                HitResult mouseOver = mc.hitResult;
                if (mouseOver != null) {
                    IDebuggable debuggable = ClientDebuggables.getDebuggableObject(mouseOver);
//                    if (debuggable instanceof BlockEntity) {
                    if (debuggable instanceof BlockEntity && mouseOver instanceof BlockHitResult) {
                        BlockEntity tile = (BlockEntity) debuggable;
                        BlockHitResult result = (BlockHitResult) mouseOver;
//                        MessageManager.sendToServer(new MessageDebugRequest(tile.getBlockPos(), mouseOver.sideHit));
                        MessageManager.sendToServer(new MessageDebugRequest(tile.getBlockPos(), result.getDirection()));
                    } else if (debuggable instanceof Entity entity) {
                        // TODO: Support entities!
                        // Calen
                        MessageManager.sendToServer(new MessageDebugRequest(entity.getOnPos(), null));
                    }
                }
            }
        }
    }

    // Calen: from BCLib
    @SubscribeEvent
//    public static void serverStarting(FMLServerStartingEvent event)
    public void serverStarting(ServerStartingEvent event) {
//        event.registerServerCommand(new CommandBuildCraft());
        CommandDispatcher<CommandSourceStack> dispatcher = event.getServer().getCommands().getDispatcher();
        CommandBuildCraft.register(dispatcher);
    }

    // Calen: update guidebook not to early
    private static List<ResourceManagerReloadListener> reloadListeners = new ArrayList<>();

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void reload(RecipesUpdatedEvent event) {
        for (ResourceManagerReloadListener reloadListener : reloadListeners) {
            reloadListener.onResourceManagerReload(Minecraft.getInstance().getResourceManager());
        }
        GuiConfigManager.loadFromConfigFile();
    }

    // Calen: only client call
    public void addReloadListeners(ResourceManagerReloadListener reloadListener) {
        reloadListeners.add(reloadListener);
    }
}
