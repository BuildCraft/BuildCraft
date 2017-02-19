/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import buildcraft.lib.item.ItemDebugger;
import buildcraft.lib.item.ItemGuide;
import buildcraft.lib.item.ItemManager;

public class BCLibItems {
    public static ItemGuide guide;
    public static ItemDebugger debugger;

    private static boolean enableGuide, enableDebugger;

    public static void enableGuide() {
        enableGuide = true;
    }

    public static void enableDebugger() {
        enableDebugger = true;
    }

    public static void fmlPreInit() {
        if (enableGuide) {
            guide = ItemManager.register(new ItemGuide("item.guide"), true);
        }
        if (enableDebugger) {
            debugger = ItemManager.register(new ItemDebugger("item.debugger"), true);
        }
    }
}
