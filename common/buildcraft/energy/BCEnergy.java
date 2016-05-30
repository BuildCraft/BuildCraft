package buildcraft.energy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import buildcraft.core.BCCore;
import buildcraft.lib.BCLib;
import buildcraft.lib.RegistryHelper;

@Mod(modid = BCEnergy.MODID, name = "BuildCraft Energy", dependencies = "required-after:buildcraftcore", version = BCLib.VERSION)
public class BCEnergy {
    public static final String MODID = "buildcraftenergy";

    @Mod.Instance(MODID)
    public static BCEnergy INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);
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
