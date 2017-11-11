/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.lib.registry.RegistrationHelper;

import buildcraft.builders.item.ItemFillerPlanner;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;

public class BCBuildersItems {

    private static final RegistrationHelper HELPER = new RegistrationHelper();

    public static ItemSnapshot snapshot;
    public static ItemSchematicSingle schematicSingle;
    public static ItemFillerPlanner addonFillerPlanner;

    public static void fmlPreInit() {
        snapshot = HELPER.addItem(new ItemSnapshot("item.snapshot"));
        schematicSingle = HELPER.addItem(new ItemSchematicSingle("item.schematic.single"));
        addonFillerPlanner = HELPER.addItem(new ItemFillerPlanner("item.filler_planner"));
    }
}
