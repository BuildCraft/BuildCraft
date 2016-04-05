package buildcraft.energy;

import net.minecraft.block.material.Material;

import buildcraft.energy.block.BlockEngineStone;
import buildcraft.lib.block.BlockBuildCraftBase_BC8;

public class BCEnergyBlocks {
    public static BlockEngineStone engineStirling;
    

    public static void preInit() {
        engineStirling = BlockBuildCraftBase_BC8.register(new BlockEngineStone(Material.rock, "block.engine.stone"));
    }
}
