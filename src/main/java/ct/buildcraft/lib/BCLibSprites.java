/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry;

public class BCLibSprites {
    public static final ISprite LOCK;
    public static final ISprite WARNING_MINOR;
    public static final ISprite WARNING_MAJOR;
    public static final ISprite LOADING;

    public static final ISprite LEDGER_LEFT;
    public static final ISprite LEDGER_RIGHT;
    public static final ISprite HELP;
    public static final ISprite HELP_SPLIT;
    public static final ISprite DEBUG;

    public static final ISprite ENGINE_INACTIVE;
    public static final ISprite ENGINE_ACTIVE;
    public static final ISprite ENGINE_WARM;
    public static final ISprite ENGINE_OVERHEAT;

    static {
        LOCK = getHolder("icons/lock");
        WARNING_MINOR = getHolder("icons/warning_minor");
        WARNING_MAJOR = getHolder("icons/warning_major");
        LOADING = getHolder("icons/loading");
        LEDGER_LEFT = getHolder("icons/ledger_left");
        LEDGER_RIGHT = getHolder("icons/ledger_right");
        HELP = getHolder("icons/help");
        HELP_SPLIT = getHolder("icons/help_split");
        DEBUG = getHolder("items/debugger");
        ENGINE_INACTIVE = getHolder("icons/engine_inactive");
        ENGINE_ACTIVE = getHolder("icons/engine_active");
        ENGINE_WARM = getHolder("icons/engine_warm");
        ENGINE_OVERHEAT = getHolder("icons/engine_overheat");
    }

    public static void fmlPreInitClient() {
        // Nothing, just to call the static method
    }

    private static ISprite getHolder(String suffix) {
        return SpriteHolderRegistry.getHolder("buildcraftlib:" + suffix);
    }
}
