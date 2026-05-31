/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.config;

import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class OverridableConfigOption extends DetailedConfigOption {
    private final String assetName;
    private final RoamingConfigManager manager;

    public OverridableConfigOption(String assetLoc, String assetName, String defaultVal) {
        super(assetLoc + "|" + assetName, defaultVal);
        this.assetName = assetName;
        Identifier loc = new Identifier("buildcraftconfig:", assetLoc.replace(".", "/") + ".properties");
        this.manager = RoamingConfigManager.getOrCreateDefault(loc);
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected boolean refresh() {
        if (manager.exists()) {
            return manager.refresh(this, assetName);
        }
        return super.refresh();
    }
}
