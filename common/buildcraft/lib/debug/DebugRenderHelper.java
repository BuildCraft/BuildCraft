/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.DetachedRenderer.IDetachedRenderer;
import buildcraft.lib.client.sprite.White;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public enum DebugRenderHelper implements IDetachedRenderer {
    INSTANCE;

//    private static final MutableQuad[] smallCuboid;

//    static {
//        smallCuboid = new MutableQuad[6];
//        Tuple3f center = new Point3f(0.5f, 0.5f, 0.5f);
//        Tuple3f radius = new Point3f(0.25f, 0.25f, 0.25f);
//
//        for (EnumFacing face : EnumFacing.VALUES) {
//            MutableQuad quad = ModelUtil.createFace(face, center, radius, null);
//            quad.lightf(1, 1);
//            smallCuboid[face.ordinal()] = quad;
//        }
//    }

    private static final LazyLoadedValue<MutableQuad[]> smallCuboid = new LazyLoadedValue(() ->
    {
        MutableQuad[] smallCuboidInner = new MutableQuad[6];
        Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f radius = new Vector3f(0.25f, 0.25f, 0.25f);

        for (Direction face : Direction.VALUES) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, null);
            // Calen: "white" is missingno in 1.18.2
            // fixed by loading "white" in SpriteHolderRegistry
            quad.texFromSprite(White.instance());
            quad.lightf(1, 1);
            quad.overlay(OverlayTexture.NO_OVERLAY); // Calen add
            smallCuboidInner[face.ordinal()] = quad;
        }
        return smallCuboidInner;
    });

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(Player player, float partialTicks, PoseStack poseStack) {
        IAdvDebugTarget target = BCAdvDebugging.INSTANCE.targetClient;
        if (target == null) {
            return;
        } else if (!target.doesExistInWorld()) {
            // targetClient = null;
            // return;
        }
        IDetachedRenderer renderer = target.getDebugRenderer();
        if (renderer != null) {
            renderer.render(player, partialTicks, poseStack);
        }
    }

    public static void renderAABB(PoseStack poseStack, VertexConsumer bb, AABB aabb, int colour) {
//        bb.setTranslation(0, 0, 0);
        for (Direction face : Direction.VALUES) {
            MutableQuad quad = ModelUtil.createFace(
                    face,
                    new Vector3f(
                            (float) aabb.getCenter().x,
                            (float) aabb.getCenter().y,
                            (float) aabb.getCenter().z
                    ),
//                    new Point3f(
////                            (float) aabb.getCenter().x,
////                            (float) aabb.getCenter().y,
////                            (float) aabb.getCenter().z
//                            0, 0, 0
//                    ),
                    new Vector3f(
                            (float) (aabb.maxX - aabb.minX) / 2,
                            (float) (aabb.maxY - aabb.minY) / 2,
                            (float) (aabb.maxZ - aabb.minZ) / 2
                    ),
                    null
            );
            quad.colouri(colour);
//            quad.texFromSprite(IModelLoader.White.INSTANCE);
            quad.texFromSprite(White.instance());
            quad.overlay(OverlayTexture.NO_OVERLAY);
            quad.lightf(1, 1);
//            quad.normalf(1, 1, 1);
            quad.render(poseStack.last(), bb);
        }
    }

    public static void renderSmallCuboid(PoseStack poseStack, VertexConsumer bb, BlockPos pos, int colour) {
//        bb.setTranslation(pos.getX(), pos.getY(), pos.getZ());
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
//        for (MutableQuad q : smallCuboid)
        for (MutableQuad q : smallCuboid.get()) {
            // Calen: don't texFromSprite here! or the tex_u/v will be changed based on last time!
//            q.texFromSprite(ModelLoader.White.INSTANCE);
            q.colouri(colour);
            q.render(poseStack.last(), bb);
        }
//        vertexConsumer.normal(0, 0, 0);
        poseStack.popPose();
    }
}
