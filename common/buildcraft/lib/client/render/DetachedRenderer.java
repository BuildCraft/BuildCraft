/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.client.render;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

@Environment(EnvType.CLIENT)
public class DetachedRenderer {

    private static final List<IDetachedRenderer> renderers = new ArrayList<>();

    /**
     * Register a renderer to be called each frame at the end of world rendering
     * (equivalent to Forge's {@code RenderWorldLastEvent}).
     */
    public static void register(IDetachedRenderer renderer) {
        renderers.add(renderer);
    }

    /**
     * Wire this class into the Fabric event bus.  Call once from
     * {@code BCLibClientInitializer.onInitializeClient()}.
     */
    public static void registerEvents() {
        WorldRenderEvents.LAST.register(DetachedRenderer::onWorldRenderLast);
    }

    private static void onWorldRenderLast(WorldRenderContext context) {
        float partialTicks = context.tickDelta();
        for (IDetachedRenderer r : renderers) {
            r.render(partialTicks);
        }
    }

    @Environment(EnvType.CLIENT)
    public interface IDetachedRenderer {
        /** Called each frame at the end of world rendering. */
        void render(float partialTicks);
    }
}
