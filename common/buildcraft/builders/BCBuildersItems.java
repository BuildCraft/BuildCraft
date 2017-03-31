/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemManager;

public class BCBuildersItems {
    private static final boolean DEV = BCLib.DEVELOPER;

    public static ItemSchematicSingle schematicSingle;
    // FIXME: remove the old class so we don't have an import error
    public static buildcraft.builders.item.ItemBlueprint blueprint;

    public static void preInit() {
        if (!DEV) return;
        schematicSingle = ItemManager.register(new ItemSchematicSingle("item.schematic.single"));
        // FIXME: remove the old class so we don't have an import error
        blueprint = ItemManager.register(new buildcraft.builders.item.ItemBlueprint("item.blueprint"));
    }
}
