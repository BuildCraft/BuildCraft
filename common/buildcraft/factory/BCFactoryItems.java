/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemManager;

public class BCFactoryItems {
    public static ItemBC_Neptune plasticSheet;

    public static void preInit() {
        plasticSheet = ItemManager.register(new ItemBC_Neptune("item.plastic.sheet"));
    }
}
