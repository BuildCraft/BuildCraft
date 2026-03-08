package buildcraft.energy;

import buildcraft.energy.generation.structure.OilStructureRegistry;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public enum BCEnergyEventDist {
    INSTANCE;

    @SubscribeEvent
    public void tryToGenStructure(BiomeLoadingEvent e) {
        e.getGeneration().addStructureStart(OilStructureRegistry.REGISTERED_CONFIGURED_STRUCTURE_FEATURE);
    }
}
