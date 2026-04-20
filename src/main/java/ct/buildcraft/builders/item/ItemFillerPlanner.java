/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.item;

import ct.buildcraft.builders.addon.AddonFillerPlanner;
import ct.buildcraft.core.marker.volume.Addon;
import ct.buildcraft.core.marker.volume.ItemAddon;

public class ItemFillerPlanner extends ItemAddon {
    public ItemFillerPlanner(Properties prop) {
		super(prop);
	}

	@Override
    public Addon createAddon() {
        return new AddonFillerPlanner();
    }
}
