/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.mj;

/**
 * Sub-interface for receivers that emit a redstone-strength signal proportional
 * to their internal buffer fill level — used by engines, redstone-engine carts,
 * and the legacy Gate "Energy Stored >= …" trigger.
 *
 * Mirrors legacy {@code buildcraft.api.mj.IMjRedstoneReceiver}.
 */
public interface IMJRedstoneReceiver extends IMJReceiver {

    /** 0–15, comparator-strength signal derived from current power buffer. */
    int getRedstoneLevel();
}
