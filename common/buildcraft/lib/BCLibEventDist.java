package buildcraft.lib;

import buildcraft.api.tiles.IDebuggable;
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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.util.ArrayList;
import java.util.List;

public enum BCLibEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerMP = (ServerPlayerEntity) entity;
            // Delay sending join messages to player as it makes it work when in single-player
            MessageUtil.doDelayedServer(() -> MarkerCache.onPlayerJoinWorld(playerMP));
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        IWorld levelAccessor = event.getWorld();
        if (levelAccessor instanceof World) {
            World level = (World) levelAccessor;
            MarkerCache.onWorldUnload(level);
            if (level instanceof ServerWorld) {
                ServerWorld serverLevel = (ServerWorld) level;
                FakePlayerProvider.INSTANCE.unloadWorld(serverLevel);
            }
        }
    }

    // Calen: When the event is posted, MinecraftForge.EVENT_BUS will be shut down.
    // If we unlock the event bus, some subscribers will be called wrongly.
    // Moved to BCLibEventDistModBus.
//    @SubscribeEvent
//    @OnlyIn(Dist.CLIENT)
//    public void onReloadFinish(EventBuildCraftReload.FinishLoad event) {
//        // Note: when you need to add server-side listeners the client listeners need to be moved to BCLibProxy
//        GuideManager.INSTANCE.onRegistryReload(event);
//    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
//    public static void onConnectToServer(ClientConnectedToServerEvent event)
    public void onConnectToServer(ClientPlayerNetworkEvent.LoggedInEvent event) {
        BuildCraftObjectCaches.onClientJoinServer();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void renderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) return;
        float partialTicks = event.getPartialTicks();

        MatrixStack poseStack = event.getMatrixStack();

        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();

        DetachedRenderer.INSTANCE.renderWorldLastEvent(player, partialTicks, poseStack, camera);
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
            ClientPlayerEntity player = mc.player;
            if (player != null && ItemDebugger.isShowDebugInfo(player)) {
                RayTraceResult mouseOver = mc.hitResult;
                if (mouseOver != null) {
                    IDebuggable debuggable = ClientDebuggables.getDebuggableObject(mouseOver);
//                    if (debuggable instanceof TileEntity) {
                    if (debuggable instanceof TileEntity && mouseOver instanceof BlockRayTraceResult) {
                        TileEntity tile = (TileEntity) debuggable;
                        BlockRayTraceResult result = (BlockRayTraceResult) mouseOver;
//                        MessageManager.sendToServer(new MessageDebugRequest(tile.getBlockPos(), mouseOver.sideHit));
                        MessageManager.sendToServer(new MessageDebugRequest(tile.getBlockPos(), result.getDirection()));
                    } else if (debuggable instanceof Entity) {
                        Entity entity = (Entity) debuggable;
                        // Calen 1.18.2
                        MessageManager.sendToServer(new MessageDebugRequest(entity.getUUID()));
                    }
                }
            }
        }
    }

    // Calen: from BCLib
    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent event) {
//        event.registerServerCommand(new CommandBuildCraft());
        CommandDispatcher<CommandSource> dispatcher = event.getServer().getCommands().getDispatcher();
        CommandBuildCraft.register(dispatcher);
    }

    // Calen: update guidebook not too early
    private List<IResourceManagerReloadListener> reloadListeners = new ArrayList<>();

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void reload(RecipesUpdatedEvent event) {
        for (IResourceManagerReloadListener reloadListener : reloadListeners) {
            reloadListener.onResourceManagerReload(Minecraft.getInstance().getResourceManager());
        }
        GuiConfigManager.loadFromConfigFile();
    }

    // Calen: only client call
    public void addReloadListeners(IResourceManagerReloadListener reloadListener) {
        reloadListeners.add(reloadListener);
    }
}
