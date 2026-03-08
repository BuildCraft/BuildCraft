/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.render;

import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.lib.client.model.AdvModelCache;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.BCSiliconModels;
import buildcraft.silicon.plug.PluggableGate;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum PlugGateRenderer implements IPlugDynamicRenderer<PluggableGate> {
    INSTANCE;

    private static final AdvModelCache cache = new AdvModelCache(BCSiliconModels.GATE_DYNAMIC, PluggableGate.MODEL_VAR_INFO);

    public static void onModelBake() {
        cache.reset();
    }

    @Override
//    public void render(PluggableGate gate, double x, double y, double z, float partialTicks, BufferBuilder vb)
    public void render(PluggableGate gate, float partialTicks, MatrixStack poseStack, IVertexBuilder vb, int combinedLight, int combinedOverlay) {
//        vb.setTranslation(x, y, z);
        gate.setClientModelVariables();
        if (gate.clientModelData.hasNoNodes()) {
            gate.clientModelData.setNodes(BCSiliconModels.GATE_DYNAMIC.createTickableNodes());
        }
        gate.clientModelData.refresh();
        MutableQuad copy = new MutableQuad();
        for (MutableQuad q : cache.getCutoutQuads()) {
            copy.copyFrom(q);
            copy.multShade();

            copy.lighti(combinedLight);
            copy.overlay(combinedOverlay);

            copy.render(poseStack.last(), vb);
        }
//        vb.setTranslation(0, 0, 0);
    }
}
