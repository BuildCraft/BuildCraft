/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import buildcraft.lib.item.ItemManager;
import buildcraft.silicon.item.ItemRedstoneChipset;

public class BCSiliconItems {
    public static ItemRedstoneChipset redstoneChipset;

    public static void preInit() {
        redstoneChipset = ItemManager.register(new ItemRedstoneChipset("item.redstone_chipset"));
    }
}
