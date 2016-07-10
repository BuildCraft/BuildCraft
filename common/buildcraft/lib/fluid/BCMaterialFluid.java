package buildcraft.lib.fluid;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;

public class BCMaterialFluid extends MaterialLiquid {
    public BCMaterialFluid(MapColor color, boolean canBurn) {
        super(color);
        if(canBurn) {
            setBurning();
        }
    }

    @Override
    public boolean blocksMovement() {
        return true;
    }
}
