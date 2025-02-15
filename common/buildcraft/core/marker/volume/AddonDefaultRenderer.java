/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import buildcraft.lib.client.sprite.White;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class AddonDefaultRenderer<T extends Addon> implements IFastAddonRenderer<T> {
    //    private final TextureAtlasSprite s;
    private final LazyOptional<TextureAtlasSprite> s;

    public AddonDefaultRenderer() {
//        s = ModelLoader.White.INSTANCE;
        s = LazyOptional.of(White::instance);
    }

    public AddonDefaultRenderer(TextureAtlasSprite s) {
//        this.s = s;
        this.s = LazyOptional.of(() -> s);
    }

    @Override
//    public void renderAddonFast(T addon, Player player, float partialTicks, BufferBuilder builder)
    public void renderAddonFast(T addon, Player player, PoseStack.Pose pose, float partialTicks, VertexConsumer builder) {
        AABB bb = addon.getBoundingBox();

        Matrix4f posePose = pose.pose();
        Matrix3f normal = pose.normal();
        TextureAtlasSprite s = this.s.resolve().get();
        builder.vertex(posePose, (float) bb.minX, (float) bb.maxY, (float) bb.minZ).color(204, 204, 204, 255).uv(s.getU0(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.maxX, (float) bb.maxY, (float) bb.minZ).color(204, 204, 204, 255).uv(s.getU0(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.maxX, (float) bb.minY, (float) bb.minZ).color(204, 204, 204, 255).uv(s.getU1(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.minX, (float) bb.minY, (float) bb.minZ).color(204, 204, 204, 255).uv(s.getU1(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();

        builder.vertex(posePose, (float) bb.minX, (float) bb.minY, (float) bb.maxZ).color(204, 204, 204, 255).uv(s.getU0(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.maxX, (float) bb.minY, (float) bb.maxZ).color(204, 204, 204, 255).uv(s.getU0(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ).color(204, 204, 204, 255).uv(s.getU1(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.minX, (float) bb.maxY, (float) bb.maxZ).color(204, 204, 204, 255).uv(s.getU1(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();

        builder.vertex(posePose, (float) bb.minX, (float) bb.minY, (float) bb.minZ).color(127, 127, 127, 255).uv(s.getU0(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.maxX, (float) bb.minY, (float) bb.minZ).color(127, 127, 127, 255).uv(s.getU0(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.maxX, (float) bb.minY, (float) bb.maxZ).color(127, 127, 127, 255).uv(s.getU1(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.minX, (float) bb.minY, (float) bb.maxZ).color(127, 127, 127, 255).uv(s.getU1(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();

        builder.vertex(posePose, (float) bb.minX, (float) bb.maxY, (float) bb.maxZ).color(255, 255, 255, 255).uv(s.getU0(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ).color(255, 255, 255, 255).uv(s.getU0(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.maxX, (float) bb.maxY, (float) bb.minZ).color(255, 255, 255, 255).uv(s.getU1(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.minX, (float) bb.maxY, (float) bb.minZ).color(255, 255, 255, 255).uv(s.getU1(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();

        builder.vertex(posePose, (float) bb.minX, (float) bb.minY, (float) bb.maxZ).color(153, 153, 153, 255).uv(s.getU0(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.minX, (float) bb.maxY, (float) bb.maxZ).color(153, 153, 153, 255).uv(s.getU0(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.minX, (float) bb.maxY, (float) bb.minZ).color(153, 153, 153, 255).uv(s.getU1(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.minX, (float) bb.minY, (float) bb.minZ).color(153, 153, 153, 255).uv(s.getU1(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();

        builder.vertex(posePose, (float) bb.maxX, (float) bb.minY, (float) bb.minZ).color(153, 153, 153, 255).uv(s.getU0(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.maxX, (float) bb.maxY, (float) bb.minZ).color(153, 153, 153, 255).uv(s.getU0(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ).color(153, 153, 153, 255).uv(s.getU1(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        builder.vertex(posePose, (float) bb.maxX, (float) bb.minY, (float) bb.maxZ).color(153, 153, 153, 255).uv(s.getU1(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
    }
}
