package buildcraft.lib.fluid;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public abstract class BCFluid extends Fluid {
    public BCFluid(String fluidName, ResourceLocation still, ResourceLocation flowing) {
        super(fluidName, still, flowing);
    }

    public abstract MapColor getMapColor();

    public boolean isFlammable() {
        return false;
    }

    public int getLightOpacity() {
        return 0;
    }
}
