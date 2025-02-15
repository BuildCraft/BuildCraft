/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.builders.item.ItemFillerPlanner;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.lib.item.ItemPropertiesCreator;
import buildcraft.lib.registry.RegistrationHelper;
import net.minecraftforge.registries.RegistryObject;

public class BCBuildersItems {
    private static final RegistrationHelper HELPER = new RegistrationHelper(BCBuilders.MODID);

    public static RegistryObject<ItemSnapshot> snapshotBLUEPRINT;
    public static RegistryObject<ItemSnapshot> snapshotTEMPLATE;
    public static RegistryObject<ItemSchematicSingle> schematicSingle;
    public static RegistryObject<ItemFillerPlanner> addonFillerPlanner;

    public static void fmlPreInit() {
        snapshotBLUEPRINT = HELPER.addItem("item.snapshot.blueprint", ItemPropertiesCreator.common64(), (idBC, prop) -> new ItemSnapshot(idBC, prop, EnumSnapshotType.BLUEPRINT));
        snapshotTEMPLATE = HELPER.addItem("item.snapshot.template", ItemPropertiesCreator.common64(), (idBC, prop) -> new ItemSnapshot(idBC, prop, EnumSnapshotType.TEMPLATE));
        schematicSingle = HELPER.addItem("item.schematic.single", ItemPropertiesCreator.common1(), ItemSchematicSingle::new);
        addonFillerPlanner = HELPER.addItem("item.filler_planner", ItemPropertiesCreator.common64(), ItemFillerPlanner::new);
    }
}
