/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.lib.client.model.AdvModelCache;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.plug.PluggableGate;
import net.minecraft.client.renderer.VertexBuffer;

public enum PlugGateRenderer implements IPlugDynamicRenderer<PluggableGate> {
    INSTANCE;

    private static final AdvModelCache cache = new AdvModelCache(BCTransportModels.GATE_DYNAMIC, PluggableGate.MODEL_VAR_INFO);

    public static void onModelBake() {
        cache.reset();
    }

    @Override
    public void render(PluggableGate gate, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        vb.setTranslation(x, y, z);
        gate.setClientModelVariables();
        if (gate.clientModelData.hasNoNodes()) {
            gate.clientModelData.setNodes(BCTransportModels.GATE_DYNAMIC.createTickableNodes());
        }
        gate.clientModelData.refresh();
        MutableQuad copy = new MutableQuad();
        for (MutableQuad q : cache.getCutoutQuads()) {
            copy.copyFrom(q);
            copy.multShade();
            copy.render(vb);
        }
        vb.setTranslation(0, 0, 0);
    }
}
