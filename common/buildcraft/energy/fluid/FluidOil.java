package buildcraft.energy.fluid;

import buildcraft.lib.fluid.BCFluid;
import net.minecraft.block.material.MapColor;
import net.minecraft.util.ResourceLocation;

public class FluidOil extends BCFluid {
    public FluidOil(String fluidName, ResourceLocation still, ResourceLocation flowing) {
        super(fluidName, still, flowing);
    }

    @Override
    public MapColor getMapColor() {
        return MapColor.BLACK;
    }

    @Override
    public boolean isFlammable() {
        return true; // FIXME: not working because fluid is not full block
    }

    @Override
    public int getLightOpacity() {
        return 8;
    }
}
