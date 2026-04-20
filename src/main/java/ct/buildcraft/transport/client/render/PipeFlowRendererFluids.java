/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.client.render;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.transport.pipe.IPipeFlowRenderer;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.lib.client.render.fluid.FluidRenderer;
import ct.buildcraft.lib.client.render.fluid.FluidSpriteType;
import ct.buildcraft.lib.misc.VecUtil;
import ct.buildcraft.transport.pipe.Pipe;
import ct.buildcraft.transport.pipe.flow.PipeFlowFluids;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public enum PipeFlowRendererFluids implements IPipeFlowRenderer<PipeFlowFluids> {
	INSTANCE;

	private static final boolean[] sides = { true, true, true, true, true, true };
	@Override
	public void render(PipeFlowFluids flow, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int lightc,
			int combinedOverlay) {
		FluidStack forRender = flow.getFluidStackForRender();
		if (forRender.isEmpty()) {
			return;
		}
		VertexConsumer fluidBuffer = buffer.getBuffer(RenderType.cutoutMipped());

		double[] amounts = flow.getAmountsForRender(partialTicks);
		Vec3[] offsets = flow.getOffsetsForRender(partialTicks);

		int blocklight = forRender.getFluid().getFluidType().getLightLevel();// to debug
		IPipeHolder holder = flow.pipe.getHolder();
		int combinedLight = holder.getPipeWorld().getBrightness(LightLayer.SKY, holder.getPipePos())<<20|blocklight<<4 ;

		FluidRenderer.vertex.lighti(combinedLight);

		boolean gas = forRender.getFluid().getFluidType().getDensity() <= 0;
		boolean horizontal = false;
		boolean vertical = flow.pipe.isConnected(gas ? Direction.DOWN : Direction.UP);

		for (Direction face : Direction.values()) {
			double size = ((Pipe) flow.pipe).getConnectedDist(face);
			if(size == 0)
				continue;
			double amount = amounts[face.get3DDataValue()];
			if (face.getAxis() != Axis.Y) {
				horizontal |= flow.pipe.isConnected(face) && amount > 0;
			}

			Vec3 center = VecUtil.offset(new Vec3(0.5, 0.5, 0.5), face, 0.25 + size / 2);
			Vec3 radius = new Vec3(0.24, 0.24, 0.24);
			radius = VecUtil.replaceValue(radius, face.getAxis(), 0.000 + size / 2);

			if (face.getAxis() == Axis.Y) {
				double perc = amount / flow.capacity;
				perc = Math.sqrt(perc);
				radius = new Vec3(perc * 0.24, radius.y, perc * 0.24);
			}

			Vec3 offset = offsets[face.get3DDataValue()];
			if (offset == null) offset = Vec3.ZERO;
			center = center.add(offset);
			matrix.translate(-offset.x, -offset.y, -offset.z);
			Vec3 min = center.subtract(radius);
			Vec3 max = center.add(radius);

			if(face.getAxis() == Axis.Y) 
				FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, 1, 1, min, max, fluidBuffer, matrix.last(), sides);
			else 
				FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, amount, flow.capacity, min, max, fluidBuffer, matrix.last(), sides);
			matrix.translate(offset.x, offset.y, offset.z);
		}

		double amount = amounts[EnumPipePart.CENTER.getIndex()];

		double horizPos = 0.26;

		Vec3 offset = offsets[EnumPipePart.CENTER.getIndex()];
		if (offset == null)
			offset = Vec3.ZERO;
		matrix.translate(-offset.x, -offset.y, -offset.z);

		if (horizontal | !vertical) {
			Vec3 min = new Vec3(0.26, 0.26, 0.26);
			Vec3 max = new Vec3(0.74, 0.74, 0.74);

			min = min.add(offset);
			max = max.add(offset);
			FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, amount, flow.capacity, min, max, fluidBuffer, matrix.last(), sides);
			horizPos += (max.y - min.y) * amount / flow.capacity;
		}

		if (vertical && horizPos < 0.74) {
			double perc = amount / flow.capacity;
			perc = Math.sqrt(perc);
			double minXZ = 0.5 - 0.24 * perc;
			double maxXZ = 0.5 + 0.24 * perc;

			double yMin = gas ? 0.26 : horizPos;
			double yMax = gas ? 1 - horizPos : 0.74;

			Vec3 min = new Vec3(minXZ, yMin, minXZ);
			Vec3 max = new Vec3(maxXZ, yMax, maxXZ);
			min = min.add(offset);
			max = max.add(offset);

			FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, 1, 1, min, max, fluidBuffer, matrix.last(), sides);
			
		}

	}
}
