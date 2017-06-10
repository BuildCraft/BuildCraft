/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import net.minecraft.client.renderer.VertexBuffer;

import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;

import buildcraft.lib.client.model.AdvModelCache;
import buildcraft.lib.client.model.MutableQuad;

import buildcraft.transport.BCTransportModels;
import buildcraft.transport.plug.PluggablePulsar;

public enum PlugPulsarRenderer implements IPlugDynamicRenderer<PluggablePulsar> {
    INSTANCE;

    private static final AdvModelCache cache = new AdvModelCache(BCTransportModels.PULSAR_DYNAMIC, PluggablePulsar.MODEL_VAR_INFO);

    public static void onModelBake() {
        cache.reset();
    }

    @Override
    public void render(PluggablePulsar pulsar, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        vb.setTranslation(x, y, z);
        if (pulsar.clientModelData.hasNoNodes()) {
            pulsar.clientModelData.setNodes(BCTransportModels.PULSAR_DYNAMIC.createTickableNodes());
        }
        pulsar.setModelVariables(partialTicks);
        pulsar.clientModelData.refresh();
        for (MutableQuad q : cache.getCutoutQuads()) {
            q.render(vb);
        }
        vb.setTranslation(0, 0, 0);
    }
}
