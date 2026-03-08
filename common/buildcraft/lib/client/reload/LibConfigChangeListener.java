/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.reload;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.BCLibConfig.RenderRotation;

import java.util.ArrayList;
import java.util.List;

public enum LibConfigChangeListener implements Runnable {
    INSTANCE;

    private boolean lastColourBlind = false;
    private RenderRotation lastRotateTravelItems = null;

    @Override
    public void run() {
        List<ReloadSource> changed = new ArrayList<>();
        if (BCLibConfig.colourBlindMode != lastColourBlind) {
            lastColourBlind = BCLibConfig.colourBlindMode;
            changed.add(ReloadManager.CONFIG_COLOUR_BLIND);
        }
        if (BCLibConfig.rotateTravelingItems != lastRotateTravelItems) {
            lastRotateTravelItems = BCLibConfig.rotateTravelingItems;
            changed.add(ReloadManager.CONFIG_ROTATE_TRAVEL_ITEMS);
        }
        ReloadManager.INSTANCE.postReload(changed);
    }
}
