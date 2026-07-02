/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.render;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        public void glPre(PoseStack pose, Matrix4f matrix, float partialTicks) {
            if (pre != null) pre.glPre(pose, matrix, partialTicks);
        }

        @Override
        public void glPost(PoseStack pose, Matrix4f matrix) {
            if (post != null) post.glPost(pose, matrix);
        }
    }

    @FunctionalInterface
    public interface IGlPre {
        void glPre(PoseStack pose, Matrix4f matrix, float partialTicks);
    }

    @FunctionalInterface
    public interface IGLPost {
        void glPost(PoseStack pose, Matrix4f matrix);
    }

    @FunctionalInterface
    public interface IDetachedRenderer {
        void render(PoseStack pose, Matrix4f matrix, Player player, float partialTicks);
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

    public void renderWorldLastEvent(PoseStack pose, Matrix4f matrix, Player player, float partialTicks) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        for (RenderMatrixType type : RenderMatrixType.values()) {
            List<IDetachedRenderer> rendersForType = this.renders.get(type);
            if (rendersForType.isEmpty()) continue;
            type.glPre(pose, matrix, partialTicks);
            for (IDetachedRenderer render : rendersForType) {
                render.render(pose, matrix, player, partialTicks);
            }
            type.glPost(pose, matrix);
        }

    }

    public static void fromWorldOriginPre(PoseStack pose, Matrix4f matrix, float partialTicks) {
    	pose.pushPose();
    	Minecraft mc = Minecraft.getInstance();
    	Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 diff = Vec3.ZERO;
        diff = diff.subtract(camera.getPosition());
        pose.translate(diff.x, diff.y, diff.z);
    }

    public static void fromWorldOriginPost(PoseStack pose, Matrix4f matrix) {
        pose.popPose();
    }
}
