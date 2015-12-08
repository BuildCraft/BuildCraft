package buildcraft.energy;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;

public class MaterialBuildCraftLiquid extends MaterialLiquid {

    public MaterialBuildCraftLiquid(MapColor color) {
        super(color);
    }

    @Override
    public boolean blocksMovement() {
        return true;
    }
}
