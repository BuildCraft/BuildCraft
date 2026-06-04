/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.mj;

/**
 * Marker interface for any block / block entity that participates in the MJ
 * power network. Carries no methods of its own — the legacy BuildCraft API uses
 * this purely to signal "this thing speaks MJ" so a network builder can pick it
 * up without committing to receiver or emitter semantics.
 *
 * TODO(R.Chen): the original {@code buildcraft.api.mj.IMjConnector} lives in the
 * (still-Forge) API submodule. Once the API is ported, this lib-side marker can
 * either delegate to it or be removed entirely.
 */
public interface IMJConnector {
}
