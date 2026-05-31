/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon;

import net.minecraft.util.DyeColor;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class BCSiliconSprites {

    public static final SpriteHolder TRIGGER_LIGHT_LOW;
    public static final SpriteHolder TRIGGER_LIGHT_HIGH;

    public static final SpriteHolder TRIGGER_TIMER_SHORT;
    public static final SpriteHolder TRIGGER_TIMER_MEDIUM;
    public static final SpriteHolder TRIGGER_TIMER_LONG;

    public static final SpriteHolder ACTION_PULSAR_CONSTANT;
    public static final SpriteHolder ACTION_PULSAR_SINGLE;
    public static final SpriteHolder[] ACTION_PIPE_COLOUR;

    static {
        TRIGGER_LIGHT_LOW = getHolder("triggers/trigger_light_dark");
        TRIGGER_LIGHT_HIGH = getHolder("triggers/trigger_light_bright");

        TRIGGER_TIMER_SHORT = getHolder("triggers/trigger_timer_short");
        TRIGGER_TIMER_MEDIUM = getHolder("triggers/trigger_timer_medium");
        TRIGGER_TIMER_LONG = getHolder("triggers/trigger_timer_long");

        ACTION_PULSAR_CONSTANT = getHolder("triggers/action_pulsar_on");
        ACTION_PULSAR_SINGLE = getHolder("triggers/action_pulsar_single");
        // DyeColor.values() replaces ColourUtil.COLOURS
        DyeColor[] colours = DyeColor.values();
        ACTION_PIPE_COLOUR = new SpriteHolder[colours.length];
        for (DyeColor colour : colours) {
            ACTION_PIPE_COLOUR[colour.ordinal()] = getHolder("core", "items/paintbrush/" + colour.getName());
        }
    }

    private static SpriteHolder getHolder(String loc) {
        return SpriteHolderRegistry.getHolder("buildcraftsilicon:" + loc);
    }

    private static SpriteHolder getHolder(String module, String loc) {
        return SpriteHolderRegistry.getHolder("buildcraft" + module + ":" + loc);
    }

    public static void fmlPreInit() {
        // STUB(R.Chen): MinecraftForge.EVENT_BUS.register(BCSiliconSprites.class) removed.
        // Fabric sprite registration is handled via ResourceReloadListener or Atlas events — deferred.
    }
}
