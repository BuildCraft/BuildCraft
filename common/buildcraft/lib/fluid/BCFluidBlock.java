package buildcraft.lib.fluid;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BCFluidBlock extends BlockFluidClassic {
    public BCFluidBlock(Fluid fluid, Material material) {
        super(fluid, material);
    }

    @Override
    public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos pos, IBlockState state, Entity entity, double yToTest, Material material, boolean testingHead) {
        if(material == Material.WATER) {
            return Boolean.TRUE;
        }
        return null;
    }
}
