/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.BCModules;

import buildcraft.lib.misc.StackUtil;

import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.item.ItemPluggableGate;

public enum GateMeshDefinition implements ItemMeshDefinition {
    INSTANCE;

    public static final ResourceLocation LOCATION_BASE = BCModules.SILICON.createLocation("gate_complex");

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        GateVariant var = ItemPluggableGate.getVariant(StackUtil.asNonNull(stack));
        ResourceLocation loc = LOCATION_BASE;
        String variant = var.getVariantName();
        return new ModelResourceLocation(loc, variant);
    }
}
