/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.IPlayerOwned;
import buildcraft.api.mj.MjBattery;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ITileForSnapshotBuilder extends IPlayerOwned {
    Level getWorldBC();

    MjBattery getBattery();

    BlockPos getBuilderPos();

    boolean canExcavate();

    SnapshotBuilder<?> getBuilder();
}
