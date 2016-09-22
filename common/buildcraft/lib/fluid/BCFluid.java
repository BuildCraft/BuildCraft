package buildcraft.lib.fluid;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.Fluid;

public class BCFluid extends Fluid {
    private boolean isFlamable = false;
    private int lightOpacity = 0;
    private MapColor mapColour = null;

    public BCFluid(String fluidName, ResourceLocation still, ResourceLocation flowing) {
        super(fluidName, still, flowing);
    }

    public void setMapColour(MapColor mapColour) {
        this.mapColour = mapColour;
    }

    public final MapColor getMapColour() {
        return this.mapColour;
    }

    public void setFlamable(boolean isFlamable) {
        this.isFlamable = isFlamable;
    }

    public final boolean isFlammable() {
        return isFlamable;
    }

    public void setLightOpacity(int lightOpacity) {
        this.lightOpacity = lightOpacity;
    }

    public final int getLightOpacity() {
        return lightOpacity;
    }
}
