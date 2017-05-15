package buildcraft.energy.generation;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;

public class BiomeInitializer {
    @SubscribeEvent
    public void initBiomeGens(WorldTypeEvent.InitBiomeGens event) {
        GenLayer[] newBiomeGens = event.getNewBiomeGens().clone();
        for (int i = 0; i < newBiomeGens.length; i++) {
            newBiomeGens[i] = new GenLayerAddOilOcean(event.getSeed(), 1500L, newBiomeGens[i]);
            newBiomeGens[i] = new GenLayerAddOilDesert(event.getSeed(), 1500L, newBiomeGens[i]);
        }
        event.setNewBiomeGens(newBiomeGens);
    }
}
