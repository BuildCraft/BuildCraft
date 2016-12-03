package buildcraft.transport.client.model;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.misc.StackUtil;
import buildcraft.transport.gate.GateVariant;
import buildcraft.transport.item.ItemPluggableGate;

public enum GateMeshDefinition implements ItemMeshDefinition {
    INSTANCE;

    public static final ResourceLocation LOCATION_BASE = new ResourceLocation("buildcrafttransport", "gate_complex");

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        GateVariant var = ItemPluggableGate.getVariant(StackUtil.asNonNull(stack));
        ResourceLocation loc = LOCATION_BASE;
        String variant = var.getVariantName();
        return new ModelResourceLocation(loc, variant);
    }
}
