/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy;

import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.registry.RegistrationHelper;

public class BCEnergyItems {

    private static final RegistrationHelper HELPER = new RegistrationHelper();

    public static ItemBC_Neptune globOil;

    public static void preInit() {
        if (BCLib.DEV) {
            globOil = HELPER.addItem(new ItemBC_Neptune("item.glob.oil"));
        }
    }
}
