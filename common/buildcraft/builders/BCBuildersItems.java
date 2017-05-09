/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.builders.item.ItemFillingPlanner;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.lib.item.ItemManager;

import buildcraft.lib.BCLib;

public class BCBuildersItems {
    public static ItemSchematicSingle schematicSingle;
    public static ItemSnapshot snapshot;
    public static ItemFillingPlanner fillingPlanner;

    public static void preInit() {
        if (BCLib.DEV) {
            schematicSingle = ItemManager.register(new ItemSchematicSingle("item.schematic.single"));
        }
        snapshot = ItemManager.register(new ItemSnapshot("item.snapshot"));
        fillingPlanner = ItemManager.register(new ItemFillingPlanner("item.filling_planner"));
    }
}
