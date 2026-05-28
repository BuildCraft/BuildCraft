/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import buildcraft.BuildCraftFabric;

/**
 * Fabric ClientModInitializer for the BuildCraft lib module.
 * Replaces the client-only @SubscribeEvent methods in {@link BCLibEventDist} and
 * the {@code @SidedProxy} client proxy in {@link BCLibProxy}.
 *
 * Event mapping (Forge → Fabric):
 *   TextureStitchEvent.Pre/Post → STUB (use ClientSpriteRegistryCallback or ModelLoadingPlugin)
 *   ModelBakeEvent              → STUB (ModelLoadingPlugin#onInitializeModelLoader)
 *   RenderWorldLastEvent        → {@link WorldRenderEvents#LAST}
 *   ClientTickEvent (END)       → {@link ClientTickEvents#END_CLIENT_TICK}
 *   ClientConnectedToServer     → {@link ClientPlayConnectionEvents#JOIN}
 */
@Environment(EnvType.CLIENT)
public class BCLibClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BuildCraftFabric.LOGGER.info("[BCLib] Initializing buildcraft-lib client");

        // Forge: ClientConnectedToServerEvent
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // STUB(R.Chen): BuildCraftObjectCaches.onClientJoinServer();
        });

        // Forge: ClientTickEvent (Phase.END)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // STUB(R.Chen): BuildCraftObjectCaches.onClientTick(); MessageUtil.postClientTick();
            // STUB(R.Chen): ItemDebugger hover-debug request → use MinecraftClient#crosshairTarget.
        });

        // Forge: RenderWorldLastEvent
        WorldRenderEvents.LAST.register(context -> {
            // STUB(R.Chen): DetachedRenderer.INSTANCE.renderWorldLastEvent(player, partialTicks);
        });

        // STUB(R.Chen): TextureStitchEvent → ModelLoadingPlugin / ClientSpriteRegistryCallback.
        // STUB(R.Chen): ModelBakeEvent → ModelLoadingPlugin#onInitializeModelLoader.
        // STUB(R.Chen): EventBuildCraftReload.FinishLoad → wire via custom ResourceReloader.

        BuildCraftFabric.LOGGER.info("[BCLib] Client initialization complete");
    }
}
