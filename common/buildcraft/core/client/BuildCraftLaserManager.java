/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.core.client;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;

/**
 * STUB(R.Chen): client render — full implementation in Phase 5.
 *
 * The Forge original built each {@link LaserType} in a static initializer from {@code BCCoreSprites}
 * (which transitively pulls in {@code core.statements} → unmigrated {@code api.statements}). To keep
 * this on the green path without dragging in the statement system, the {@link LaserType} constants are
 * left null for now; they are only consumed by client-side laser rendering (also stubbed). Rebuild the
 * real sprite-backed laser types in Phase 5 once {@code BCCoreSprites} / {@code api.statements} land.
 */
public class BuildCraftLaserManager {

    // STUB(R.Chen): sprite-backed LaserType constants — populated in Phase 5 (were built from BCCoreSprites).
    public static final LaserType MARKER_VOLUME_CONNECTED = null;
    public static final LaserType MARKER_VOLUME_POSSIBLE = null;
    public static final LaserType MARKER_VOLUME_SIGNAL = null;

    public static final LaserType MARKER_PATH_CONNECTED = null;
    public static final LaserType MARKER_PATH_POSSIBLE = null;

    public static final LaserType MARKER_DEFAULT_POSSIBLE = null;

    public static final LaserType STRIPES_READ = null;
    public static final LaserType STRIPES_WRITE = null;
    public static final LaserType STRIPES_WRITE_DIRECTION = null;

    public static final LaserType POWER_LOW = null;// red
    public static final LaserType POWER_MED = null;// yellow
    public static final LaserType POWER_HIGH = null;// green
    public static final LaserType POWER_FULL = null;// blue
    public static final LaserType[] POWERS = new LaserType[] { POWER_LOW, POWER_MED, POWER_HIGH, POWER_FULL };
}
