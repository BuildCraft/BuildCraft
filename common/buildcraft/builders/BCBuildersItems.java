/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.builders.item.ItemBlueprint;
import buildcraft.builders.item.ItemFillingPlanner;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.lib.item.ItemManager;

import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemManager;

public class BCBuildersItems {
    private static final boolean DEV = BCLib.DEVELOPER;

    public static ItemSchematicSingle schematicSingle;
    public static ItemBlueprint blueprint;
    public static ItemSnapshot snapshot;
    public static ItemFillingPlanner fillingPlanner;

    public static void preInit() {
        if (!DEV) return;
        schematicSingle = ItemManager.register(new ItemSchematicSingle("item.schematic.single"));
        blueprint = ItemManager.register(new ItemBlueprint("item.blueprint"));
        snapshot = ItemManager.register(new ItemSnapshot("item.snapshot"));
        fillingPlanner = ItemManager.register(new ItemFillingPlanner("item.filling_planner"));
    }
}
