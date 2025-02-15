/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import buildcraft.factory.item.ItemWaterGel;
import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemPropertiesCreator;
import buildcraft.lib.registry.RegistrationHelper;
import net.minecraftforge.registries.RegistryObject;

public class BCFactoryItems {

    private static final RegistrationHelper HELPER = new RegistrationHelper(BCFactory.MODID);

    public static RegistryObject<ItemBC_Neptune> plasticSheet;
    public static RegistryObject<ItemWaterGel> waterGel;
    public static RegistryObject<ItemBC_Neptune> gelledWater;

    public static void fmlPreInit() {
        if (BCLib.DEV) {
            plasticSheet = HELPER.addItem("item.plastic.sheet", ItemPropertiesCreator.common64(), ItemBC_Neptune::new);
        }
        waterGel = HELPER.addItem("item.water_gel_spawn", ItemPropertiesCreator.common16(), ItemWaterGel::new);
        gelledWater = HELPER.addItem("item.gel", ItemPropertiesCreator.common64(), ItemBC_Neptune::new);
    }
}
