package buildcraft.energy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import buildcraft.core.BCMisc;
import buildcraft.core.BuildCraftCore;
import buildcraft.lib.RegistryHelper;

@Mod(modid = BuildCraftEnergy.MODID, name = "BuildCraft|Energy", dependencies = "required-after:buildcraftcore", version = BCMisc.VERSION)
public class BuildCraftEnergy {
    public static final String MODID = "buildcraftenergy";

    @Mod.Instance(MODID)
    public static BuildCraftEnergy INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BuildCraftCore.MODID);
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
