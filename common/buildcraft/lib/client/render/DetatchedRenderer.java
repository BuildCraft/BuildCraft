/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

/** Dispatches "detached renderer elements" - rendering that does not require a specific tile or entity in the world
 * (perhaps held item HUD elements) */
public enum DetatchedRenderer {
    INSTANCE;

    public enum RenderMatrixType implements IGlPre, IGLPost {
        FROM_PLAYER(null, null),
        FROM_WORLD_ORIGIN(DetatchedRenderer::fromWorldOriginPre, DetatchedRenderer::fromWorldOriginPost);

        public final IGlPre pre;
        public final IGLPost post;

        RenderMatrixType(IGlPre pre, IGLPost post) {
            this.pre = pre;
            this.post = post;
        }

        @Override
        public void glPre(EntityPlayer clientPlayer, float partialTicks) {
            if (pre != null) pre.glPre(clientPlayer, partialTicks);
        }

        @Override
        public void glPost() {
            if (post != null) post.glPost();
        }
    }

    @FunctionalInterface
    public interface IGlPre {
        void glPre(EntityPlayer clientPlayer, float partialTicks);
    }

    @FunctionalInterface
    public interface IGLPost {
        void glPost();
    }

    public interface IDetachedRenderer {
        void render(EntityPlayer player, float partialTicks);
    }

    private final Map<RenderMatrixType, List<IDetachedRenderer>> renders = new EnumMap<>(RenderMatrixType.class);

    DetatchedRenderer() {
        for (RenderMatrixType type : RenderMatrixType.values()) {
            renders.put(type, new ArrayList<>());
        }
    }

    public void addRenderer(RenderMatrixType type, IDetachedRenderer renderer) {
        renders.get(type).add(renderer);
    }

    public void renderWorldLastEvent(EntityPlayer player, float partialTicks) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().entityRenderer.enableLightmap();

        for (RenderMatrixType type : RenderMatrixType.values()) {
            List<IDetachedRenderer> rendersForType = this.renders.get(type);
            if (rendersForType.isEmpty()) continue;
            type.glPre(player, partialTicks);
            for (IDetachedRenderer render : rendersForType) {
                render.render(player, partialTicks);
            }
            type.glPost();
        }

        Minecraft.getMinecraft().entityRenderer.disableLightmap();
    }

    public static void fromWorldOriginPre(EntityPlayer player, float partialTicks) {
        GL11.glPushMatrix();

        Vec3d diff = new Vec3d(0, 0, 0);
        diff = diff.subtract(player.getPositionEyes(partialTicks));
        diff = diff.addVector(0, player.getEyeHeight(), 0);
        GL11.glTranslated(diff.xCoord, diff.yCoord, diff.zCoord);
    }

    public static void fromWorldOriginPost() {
        GL11.glPopMatrix();
    }
}
