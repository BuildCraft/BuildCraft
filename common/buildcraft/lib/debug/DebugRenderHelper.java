/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.DetachedRenderer.IDetachedRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.LazyValue;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;

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

    private static final LazyValue<MutableQuad[]> smallCuboid = new LazyValue(() ->
    {
        MutableQuad[] smallCuboidInner = new MutableQuad[6];
        Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f radius = new Vector3f(0.25f, 0.25f, 0.25f);

        for (Direction face : Direction.values()) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, null);
            // Calen: "white" is missingno in 1.18.2
            // fixed by loading "white" in SpriteHolderRegistry
            quad.texFromSprite(ModelLoader.White.instance());
            quad.lightf(1, 1);
            quad.overlay(OverlayTexture.NO_OVERLAY); // Calen add
            smallCuboidInner[face.ordinal()] = quad;
        }
        return smallCuboidInner;
    });

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(PlayerEntity player, float partialTicks, MatrixStack poseStack) {
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

    public static void renderAABB(MatrixStack poseStack, IVertexBuilder bb, AxisAlignedBB aabb, int colour) {
//        bb.setTranslation(0, 0, 0);
        for (Direction face : Direction.values()) {
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
            quad.texFromSprite(ModelLoader.White.instance());
            quad.overlay(OverlayTexture.NO_OVERLAY);
            quad.lightf(1, 1);
//            quad.normalf(1, 1, 1);
            quad.render(poseStack.last(), bb);
        }
    }

    public static void renderSmallCuboid(MatrixStack poseStack, IVertexBuilder bb, BlockPos pos, int colour) {
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
