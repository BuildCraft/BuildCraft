/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.client.render;


import java.util.function.Function;

import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.transport.pipe.IPipeFlowRenderer;
import ct.buildcraft.lib.client.model.ModelUtil;
import ct.buildcraft.lib.client.model.ModelUtil.UvFaceData;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.lib.misc.MathUtil;
import ct.buildcraft.lib.misc.VecUtil;
import ct.buildcraft.transport.pipe.flow.PipeFlowPower;
import ct.buildcraft.transport.pipe.flow.PipeFlowPower.Section;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum PipeFlowRendererPower implements IPipeFlowRenderer<PipeFlowPower> {
    INSTANCE;

	public static TextureAtlasSprite POWER_FLOW_OVERLOAD;
	public static TextureAtlasSprite POWER_FLOW;
	
    @Override
	public void render(PipeFlowPower flow, float partialTicks, PoseStack matrix, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
    	if(POWER_FLOW_OVERLOAD == null || POWER_FLOW == null) {
    		Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
    		POWER_FLOW_OVERLOAD = atlas.apply(new ResourceLocation("buildcrafttransport:pipes/power_flow_overload"));
    		POWER_FLOW = atlas.apply(new ResourceLocation("buildcrafttransport:pipes/power_flow"));
    	}
    	Pose p = matrix.last();
    	Matrix4f pose = p.pose();
    	Matrix3f normal = p.normal();
    	VertexConsumer bb = buffer.getBuffer(RenderType.cutout());
        double centrePower = 0;
        double[] power = new double[6];
        for (Direction side : Direction.values()) {
            Section s = flow.getSection(side);
            int i = side.ordinal();
            power[i] = s.displayPower / (double) MjAPI.MJ;
            centrePower = Math.max(centrePower, power[i]);
        }

        if (centrePower > 0) {
            for (Direction side : Direction.values()) {
                if (!flow.pipe.isConnected(side)) {
                    continue;
                }
                int i = side.ordinal();
                Section s = flow.getSection(side);
                double offset = MathUtil.interp(partialTicks, s.clientDisplayFlowLast, s.clientDisplayFlow);
                renderSidePower(side, power[i], centrePower, offset, pose, normal, bb);
            }

            renderCentrePower(centrePower, flow.clientDisplayFlowCentre, pose, normal, bb);
        }

	}

    private static void renderSidePower(Direction side, double power, double centrePower, double offset, Matrix4f pose, Matrix3f normal,
        VertexConsumer bb) {
        if (power < 0) {
            return;
        }
        boolean overload = false;
        double radius = 0.248 * power;
        if (radius >= 0.248) {
            // overload = true;
            radius = 0.248;
        }

        TextureAtlasSprite sprite = (overload ? POWER_FLOW_OVERLOAD : POWER_FLOW);

        double centreRadius = 0.252 - (0.248 * centrePower);

        Vec3 centre = VecUtil.offset(VecUtil.VEC_HALF, side, 0.25 + 0.125 - centreRadius / 2);
        Vec3 radiusV = new Vec3(radius, radius, radius);
        radiusV = VecUtil.replaceValue(radiusV, side.getAxis(), 0.125 + centreRadius / 2);

        Vector3f centreF = new Vector3f((float) centre.x, (float) centre.y, (float) centre.z);
        Vector3f radiusF = new Vector3f((float) radiusV.x, (float) radiusV.y, (float) radiusV.z);

        UvFaceData uvs = new UvFaceData();
        for (Direction face : Direction.values()) {
            if (face == side.getOpposite()) {
                continue;
            }

            AABB box = new AABB(centre.subtract(radiusV).scale(0.5), centre.add(radiusV).scale(0.5));
            box = box.move(VecUtil.offset(Vec3.ZERO, side, offset * side.getAxisDirection().getStep() / 32));
            ModelUtil.mapBoxToUvs(box, face, uvs);

            MutableQuad quad = ModelUtil.createFace(face, centreF, radiusF, uvs);
            quad.texFromSprite(sprite);
            quad.lighti(15, 15);
            quad.render(pose, normal, bb);
        }
    }

    private static void renderCentrePower(double power, Vec3 offset, Matrix4f pose, Matrix3f normal, VertexConsumer bb) {
        boolean overload = false;
        float radius = 0.248f * (float) power;
        if (radius > 0.248f) {
            // overload = true;
            radius = 0.248f;
        }
        TextureAtlasSprite sprite = (overload ? POWER_FLOW_OVERLOAD : POWER_FLOW);
        
        Vector3f centre = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f radiusP = new Vector3f(radius, radius, radius);

        UvFaceData uvs = new UvFaceData();

        for (Direction face : Direction.values()) {

            AABB box = new AABB(
                new Vec3(0.5 - radius, 0.5 - radius, 0.5 - radius).scale(0.5), //
                new Vec3(0.5 + radius, 0.5 + radius, 0.5 + radius).scale(0.5)//
            );
            box = box.move(offset.scale(1 / 32.0));
            ModelUtil.mapBoxToUvs(box, face, uvs);

            MutableQuad quad = ModelUtil.createFace(face, centre, radiusP, uvs);
            quad.texFromSprite(sprite);
            quad.lighti(15, 15);
            quad.render(pose, normal, bb);
        }
    }
}
