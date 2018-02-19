/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import buildcraft.lib.item.ItemDebugger;
import buildcraft.lib.item.ItemGuide;
import buildcraft.lib.item.ItemGuideNote;
import buildcraft.lib.registry.RegistrationHelper;

public class BCLibItems {

    private static boolean enableGuide, enableDebugger;

    public static void enableGuide() {
        enableGuide = true;
    }

    public static void enableDebugger() {
        enableDebugger = true;
    }

    public static boolean isGuideEnabled() {
        return enableGuide;
    }

    public static boolean isDebuggerEnabled() {
        return enableDebugger;
    }

    public static void fmlPreInit() {
        if (isGuideEnabled()) {
            RegistrationHelper.addForcedItem(new ItemGuide("item.guide"));
            RegistrationHelper.addForcedItem(new ItemGuideNote("item.guide.note"));
        }
        if (isDebuggerEnabled()) {
            RegistrationHelper.addForcedItem(new ItemDebugger("item.debugger"));
        }
    }
}
