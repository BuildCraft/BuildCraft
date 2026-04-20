/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import ct.buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import ct.buildcraft.lib.client.model.AdvModelCache;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.silicon.BCSiliconModels;
import ct.buildcraft.silicon.plug.PluggablePulsar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum PlugPulsarRenderer implements IPlugDynamicRenderer<PluggablePulsar> {
    INSTANCE;

    private static final AdvModelCache cache =
        new AdvModelCache(BCSiliconModels.PULSAR_DYNAMIC, PluggablePulsar.MODEL_VAR_INFO);

    public static void onModelBake() {
        cache.reset();
    }

    @Override
    public void render(PluggablePulsar pulsar, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,int combinedLight, int combinedOverlay) {
        if (pulsar.clientModelData.hasNoNodes()) {
            pulsar.clientModelData.setNodes(BCSiliconModels.PULSAR_DYNAMIC.createTickableNodes());
        }
        pulsar.setModelVariables(partialTicks);
        pulsar.clientModelData.refresh();
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer bb = buffer.getBuffer(RenderType.cutout());
        for (MutableQuad q : cache.getCutoutQuads()) {
            q.render(pose, normal, bb);
        }
    }
}
