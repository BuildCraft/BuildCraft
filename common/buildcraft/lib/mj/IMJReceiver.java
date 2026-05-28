/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.mj;

/**
 * Anything that can receive MJ power from a connected emitter.
 * Mirrors the legacy {@code buildcraft.api.mj.IMjReceiver} surface.
 */
public interface IMJReceiver extends IMJConnector {

    /** Maximum micro-joules accepted per tick from a single side. */
    long getPowerRequested();

    /**
     * Push power into this receiver.
     *
     * @param microJoules amount offered (1 MJ = 1,000,000 µJ)
     * @param simulate    if {@code true}, do not actually accept the power
     * @return amount that could NOT be accepted (so the emitter can retry / overflow elsewhere)
     */
    long receivePower(long microJoules, boolean simulate);

    /**
     * Whether power is currently allowed to flow from {@code emitter} into this
     * receiver. Implementations typically check redstone signals, internal
     * state, or directional whitelists.
     */
    default boolean canConnect(IMJConnector other) {
        return true;
    }
}
