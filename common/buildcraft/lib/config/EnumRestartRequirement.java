/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.config;

@Deprecated
public enum EnumRestartRequirement {
    // NONE(false, false),
    // WORLD(true, false),
    // GAME(true, true);
    NONE(false),
    WORLD(true),
    ;

    // private final boolean restartWorld, restartGame;
    private final boolean restartWorld;

    // EnumRestartRequirement(boolean restartWorld, boolean restartGame)
    EnumRestartRequirement(boolean restartWorld) {
        this.restartWorld = restartWorld;
//        this.restartGame = restartGame;
    }

//    public void setTo(Property prop) {
//        prop.setRequiresWorldRestart(restartWorld);
//        prop.setRequiresMcRestart(restartGame);
//    }

    public boolean hasBeenRestarted(EnumRestartRequirement requirement) {
//        if (restartGame && !requirement.restartGame) return false;
        if (restartWorld && !requirement.restartWorld) return false;
        return true;
    }
}
