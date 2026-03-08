package buildcraft.energy;

import buildcraft.energy.item.ItemOilPlacer;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemPropertiesCreator;
import buildcraft.lib.registry.RegistrationHelper;
import net.minecraftforge.fml.RegistryObject;

public class BCEnergyItems {
    private static final RegistrationHelper HELPER = new RegistrationHelper(BCEnergy.MODID);

    public static RegistryObject<ItemBC_Neptune> globOil;
    public static RegistryObject<ItemOilPlacer> oilPlacer;

    public static void preInit() {
        globOil = HELPER.addItem("item.glob_oil", ItemPropertiesCreator.epic64(), ItemBC_Neptune::new);
        oilPlacer = HELPER.addItem("item.oil_placer", ItemPropertiesCreator.rare1(), ItemOilPlacer::new);
    }
}
