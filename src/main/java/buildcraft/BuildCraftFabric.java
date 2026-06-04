/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class BuildCraftFabric implements ModInitializer {

    public static final String MOD_ID = "buildcraft";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("""

                ========================================
                BuildCraft  |  Fabric 1.20.1
                Originally by SpaceToad & BuildCraft Team
                Ported by R.Chen
                https://github.com/MantraChen
                ========================================
                """);
    }
}
