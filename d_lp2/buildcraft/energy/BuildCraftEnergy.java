package buildcraft.energy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import buildcraft.core.BCMisc;

@Mod(modid = "buildcraftenergy", name = "BuildCraft|Energy", version = BCMisc.VERSION)
public class BuildCraftEnergy {
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        // BCEnergyItems.preInit();
        BCEnergyBlocks.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {

    }
}
