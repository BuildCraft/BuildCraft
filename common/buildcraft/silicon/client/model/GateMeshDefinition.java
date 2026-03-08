/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model;

import buildcraft.api.BCModules;
import buildcraft.lib.misc.StackUtil;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.item.ItemPluggableGate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ItemModelMesherForge;

@Deprecated()
// Calen: created items with different ids in 1.18.2, and this is not still useful
//public enum GateMeshDefinition implements ItemMeshDefinition
public class GateMeshDefinition extends ItemModelMesherForge {
//    INSTANCE;

    public static GateMeshDefinition INSTANCE = new GateMeshDefinition();

    public static final ResourceLocation LOCATION_BASE = BCModules.SILICON.createLocation("gate_complex");

    public GateMeshDefinition() {
        super(Minecraft.getInstance().getModelManager());
    }

    @Override
//    public ModelResourceLocation getModelLocation(ItemStack stack)
    public ModelResourceLocation getLocation(ItemStack stack) {
        GateVariant var = ItemPluggableGate.getVariant(StackUtil.asNonNull(stack));
        ResourceLocation loc = LOCATION_BASE;
        String variant = var.getVariantName();
        return new ModelResourceLocation(loc, variant);
    }
}
