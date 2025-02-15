/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render;

import buildcraft.lib.misc.SpriteUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Dispatches "detached renderer elements" - rendering that does not require a specific tile or entity in the world
 * (perhaps held item HUD elements) */
@OnlyIn(Dist.CLIENT)
public enum DetachedRenderer {
    INSTANCE;

    public enum RenderMatrixType implements IGlPre, IGLPost {
        FROM_PLAYER(null, null),
        FROM_WORLD_ORIGIN(DetachedRenderer::fromWorldOriginPre, DetachedRenderer::fromWorldOriginPost);

        public final IGlPre pre;
        public final IGLPost post;

        RenderMatrixType(IGlPre pre, IGLPost post) {
            this.pre = pre;
            this.post = post;
        }

        @Override
        public void glPre(Player clientPlayer, float partialTicks, PoseStack poseStack, Camera camera) {
            if (pre != null) pre.glPre(clientPlayer, partialTicks, poseStack, camera);
        }

        @Override
        public void glPost(PoseStack poseStack) {
            if (post != null) post.glPost(poseStack);
        }
    }

    @FunctionalInterface
    public interface IGlPre {
        void glPre(Player clientPlayer, float partialTicks, PoseStack poseStack, Camera camera);
    }

    @FunctionalInterface
    public interface IGLPost {
        void glPost(PoseStack poseStack);
    }

    @FunctionalInterface
    public interface IDetachedRenderer {
        void render(Player player, float partialTicks, PoseStack poseStack);
    }

    private final Map<RenderMatrixType, List<IDetachedRenderer>> renders = new EnumMap<>(RenderMatrixType.class);

    DetachedRenderer() {
        for (RenderMatrixType type : RenderMatrixType.values()) {
            renders.put(type, new ArrayList<>());
        }
    }

    public void addRenderer(RenderMatrixType type, IDetachedRenderer renderer) {
        renders.get(type).add(renderer);
    }

    public void renderWorldLastEvent(Player player, float partialTicks, PoseStack poseStack, Camera camera) {
//        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//        Minecraft.getInstance().entityRenderer.enableLightmap();

        for (RenderMatrixType type : RenderMatrixType.values()) {
            List<IDetachedRenderer> rendersForType = this.renders.get(type);
            if (rendersForType.isEmpty()) continue;
            // Calen: push
            type.glPre(player, partialTicks, poseStack, camera);
            for (IDetachedRenderer render : rendersForType) {
                render.render(player, partialTicks, poseStack);
            }
            // Calen: pop
            type.glPost(poseStack);
        }

//        Minecraft.getInstance().entityRenderer.disableLightmap();
    }

    public static void fromWorldOriginPre(Player player, float partialTicks, PoseStack poseStack, Camera camera) {
//        GL11.glPushMatrix();
        poseStack.pushPose();
//        Vec3d diff = new Vec3d(0, 0, 0);
//        diff = diff.subtract(player.getPositionEyes(partialTicks));
//        diff = diff.addVector(0, player.getEyeHeight(), 0);
//        GL11.glTranslated(diff.x, diff.y, diff.z);
        Vec3 vec3 = camera.getPosition();
        double d0 = vec3.x();
        double d1 = vec3.y();
        double d2 = vec3.z();
        poseStack.translate(-d0, -d1, -d2);
    }

    public static void fromWorldOriginPost(PoseStack poseStack) {
//        GL11.glPopMatrix();
        poseStack.popPose();
    }
}
