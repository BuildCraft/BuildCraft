package buildcraft.energy;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.tile.TileEngineStone_BC8;

public class BCEnergyBlocks {

    public static void preInit() {
        
        BCCoreBlocks.engine.registerEngine(EnumEngineType.STONE, TileEngineStone_BC8::new);
    }
}
