// STUB(R.Chen): Forge IPlantable — compile shim.
package net.minecraftforge.common;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface IPlantable {
    PlantType getPlantType(BlockView world, BlockPos pos);
    BlockState getPlant(BlockView world, BlockPos pos);

    enum PlantType {
        PLAINS, DESERT, BEACH, CAVE, WATER, NETHER, CROP
    }
}
