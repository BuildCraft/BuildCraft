/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.ItemRenderUtil;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.TravellingItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public enum PipeFlowRendererItems implements IPipeFlowRenderer<PipeFlowItems> {
    INSTANCE;

    private static final MutableQuad[] COLOURED_QUADS = new MutableQuad[6];

    public static void onModelBake() {
        Vector3f center = new Vector3f();
        Vector3f radius = new Vector3f(0.2f, 0.2f, 0.2f);

        ISprite sprite = BCTransportSprites.COLOUR_ITEM_BOX;
        UvFaceData uvs = new UvFaceData();
        uvs.minU = (float) sprite.getInterpU(0);
        uvs.maxU = (float) sprite.getInterpU(1);
        uvs.minV = (float) sprite.getInterpV(0);
        uvs.maxV = (float) sprite.getInterpV(1);

        for (Direction face : Direction.VALUES) {
            MutableQuad q = ModelUtil.createFace(face, center, radius, uvs);
            q.setCalculatedDiffuse();
            COLOURED_QUADS[face.ordinal()] = q;
        }
    }

    @Override
//    public void render(PipeFlowItems flow, double x, double y, double z, float partialTicks, BufferBuilder bb)
    public void render(PipeFlowItems flow, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int lightc, int combinedOverlay) {
        VertexConsumer bb = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
        Level world = flow.pipe.getHolder().getPipeWorld();
//        long now = world.getTotalWorldTime();
        long now = world.getGameTime();
//        int lightc = world.getCombinedLight(flow.pipe.getHolder().getPipePos(), 0);

        List<TravellingItem> toRender = flow.getAllItemsForRender();

        for (TravellingItem item : toRender) {
            Vec3 pos = item.getRenderPosition(BlockPos.ZERO, now, partialTicks, flow);

            poseStack.pushPose();
            poseStack.translate(pos.x, pos.y, pos.z);

            ItemStack stack = item.clientItemLink.get();
            if (stack != null && !stack.isEmpty()) {
//                ItemRenderUtil.renderItemStack(x + pos.x, y + pos.y, z + pos.z, stack, item.stackSize, lightc, item.getRenderDirection(now, partialTicks), bb);
                ItemRenderUtil.renderItemStack(stack, item.stackSize, lightc, item.getRenderDirection(now, partialTicks), poseStack, bb);
            }
            if (item.colour != null) {
//                bb.setTranslation(x + pos.x, y + pos.y, z + pos.z);
                int col = ColourUtil.getLightHex(item.colour);
                int r = (col >> 16) & 0xFF;
                int g = (col >> 8) & 0xFF;
                int b = col & 0xFF;
                for (MutableQuad q : COLOURED_QUADS) {
                    MutableQuad q2 = new MutableQuad(q);
                    q2.lighti(lightc);
                    q2.multColouri(r, g, b, 255);
                    q2.render(poseStack.last(), bb);
                }
//                bb.setTranslation(0, 0, 0);
            }
            poseStack.popPose();
        }

//        ItemRenderUtil.endItemBatch();
    }
}
