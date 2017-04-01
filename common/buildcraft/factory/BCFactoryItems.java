/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import buildcraft.factory.item.ItemWaterGel;
import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemManager;

public class BCFactoryItems {
    public static ItemBC_Neptune plasticSheet;
    public static ItemWaterGel waterGel;
    public static ItemBC_Neptune gelledWater;

    public static void preInit() {
        if (BCLib.DEV) {
            plasticSheet = ItemManager.register(new ItemBC_Neptune("item.plastic.sheet"));
        }
        waterGel = ItemManager.register(new ItemWaterGel("item.water_gel_spawn"));
        gelledWater = ItemManager.register(new ItemBC_Neptune("item.gel"));
    }
}
