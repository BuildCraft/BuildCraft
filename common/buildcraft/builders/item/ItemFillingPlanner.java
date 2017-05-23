/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.item;

import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ItemAddon;

public class ItemFillingPlanner extends ItemAddon {
    public ItemFillingPlanner(String id) {
        super(id);
    }

    @Override
    public Addon createAddon() {
        return new AddonFillingPlanner();
    }
}
