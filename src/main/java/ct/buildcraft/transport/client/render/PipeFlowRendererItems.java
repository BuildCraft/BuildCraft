/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.client.render;

import java.util.List;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.transport.pipe.IPipeFlowRenderer;
import ct.buildcraft.lib.client.model.ModelUtil;
import ct.buildcraft.lib.client.model.ModelUtil.UvFaceData;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.lib.client.render.ItemRenderUtil;
import ct.buildcraft.lib.misc.ColourUtil;
import ct.buildcraft.transport.BCTransportSprites;
import ct.buildcraft.transport.pipe.flow.PipeFlowItems;
import ct.buildcraft.transport.pipe.flow.TravellingItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum PipeFlowRendererItems implements IPipeFlowRenderer<PipeFlowItems> {
	INSTANCE;
	
	private static ItemRenderer itemRender;
	
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

        for (Direction face : Direction.values()) {
            MutableQuad q = ModelUtil.createFace(face, center, radius, uvs);
            q.setCalculatedDiffuse();
            COLOURED_QUADS[face.ordinal()] = q;
        }
    }

    @Override
    public void render(PipeFlowItems flow, float partialTicks, PoseStack matrix, MultiBufferSource buffer,
			int lightc, int combinedOverlay) {
    	if(itemRender == null) itemRender = Minecraft.getInstance().getItemRenderer();
        Level world = flow.pipe.getHolder().getPipeWorld();
        long now = world.getGameTime();
        List<TravellingItem> toRender = flow.getAllItemsForRender();
        VertexConsumer buffer2 = buffer.getBuffer(RenderType.cutout());
        
        matrix.translate(0.5f, 0.5f, 0.5f);
//        BCLog.d("" + toRender.size());
        for (TravellingItem item : toRender) {
            Vec3 pos = item.getRenderPosition(BlockPos.ZERO, now, partialTicks, flow);
            
            ItemStack stack = item.clientItemLink.get();
            
            if (!stack.isEmpty()) {
//            	BCLog.d(stack.getItem() == Items.APPLE);
            	matrix.translate(pos.x, -0.2f+pos.y, pos.z);
            	itemRender.renderStatic(stack, TransformType.GROUND, lightc, combinedOverlay, matrix, buffer, 0);
//                itemRender.getModel(stack, world, null, combinedOverlay);
                matrix.translate(-pos.x, 0.2f-pos.y, -pos.z);
  //              ItemRenderUtil.renderItemStack(pos.x, pos.y, pos.z, //
  //                      stack, item.stackSize, lightc, item.getRenderDirection(now, partialTicks), buffer2);
            }
            if (item.colour != null) {
                matrix.translate(pos.x, pos.y, pos.z);
                Matrix4f pose = matrix.last().pose();
                Matrix3f normal = matrix.last().normal();
                VertexConsumer builder = buffer.getBuffer(RenderType.cutoutMipped());
                int col = ColourUtil.getLightHex(item.colour);
                int r = (col >> 16) & 0xFF;
                int g = (col >> 8) & 0xFF;
                int b = col & 0xFF;
                for (MutableQuad q : COLOURED_QUADS) {
                    MutableQuad q2 = new MutableQuad(q);
                    q2.lighti(lightc);
                    q2.multColouri(r, g, b, 255);
                    q2.render(pose, normal, builder);
                }
                matrix.translate(-pos.x, -pos.y, -pos.z);
            }
        }

    }
}
