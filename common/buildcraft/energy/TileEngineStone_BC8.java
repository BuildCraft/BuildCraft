package buildcraft.energy;

import buildcraft.core.lib.engines.TileEngineBase_BC8;
import buildcraft.core.mj.api.EnumMjPowerType;
import buildcraft.core.mj.api.IMjMachineProducer;

public class TileEngineStone_BC8 extends TileEngineBase_BC8 {
    @Override
    protected IMjMachineProducer createProducer() {
        return new StoneProducer();
    }

    public class StoneProducer extends EngineProducer {
        public StoneProducer() {
            super(EnumMjPowerType.THINK_OF_NAME);
        }
    }
}
