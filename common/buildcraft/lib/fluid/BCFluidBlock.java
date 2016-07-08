package buildcraft.lib.fluid;

import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BCFluidBlock extends BlockFluidClassic {
    public BCFluidBlock(Fluid fluid, Material material) {
        super(fluid, material);
    }
}
