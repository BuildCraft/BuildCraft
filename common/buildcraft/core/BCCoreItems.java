/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import buildcraft.api.items.FluidItemDrops;
import buildcraft.core.item.*;
import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.registry.RegistrationHelper;

public class BCCoreItems {

    public static void preInit() {
        RegistrationHelper.addItem(new ItemWrench_Neptune("item.wrench"));
        RegistrationHelper.addItem(new ItemBC_Neptune("item.gear.wood"));
        RegistrationHelper.addItem(new ItemBC_Neptune("item.gear.stone"));
        RegistrationHelper.addItem(new ItemBC_Neptune("item.gear.iron"));
        RegistrationHelper.addItem(new ItemBC_Neptune("item.gear.gold"));
        RegistrationHelper.addItem(new ItemBC_Neptune("item.gear.diamond"));
        RegistrationHelper.addItem(new ItemPaintbrush_BC8("item.paintbrush"));
        RegistrationHelper.addItem(new ItemList_BC8("item.list"));
        RegistrationHelper.addItem(new ItemMapLocation("item.map_location"));
        RegistrationHelper.addItem(new ItemMarkerConnector("item.marker_connector"));
        RegistrationHelper.addItem(new ItemVolumeBox("item.volume_box"));
        FluidItemDrops.item = RegistrationHelper.addItem(new ItemFragileFluidContainer("item.fragile_fluid_shard"));
        if (BCLib.DEV) {
            RegistrationHelper.addItem(new ItemGoggles("item.goggles"));
        }
    }
}
