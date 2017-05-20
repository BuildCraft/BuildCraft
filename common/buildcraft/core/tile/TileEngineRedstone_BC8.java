/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.tile;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;

import javax.annotation.Nonnull;

public class TileEngineRedstone_BC8 extends TileEngineBase_BC8 {
    public TileEngineRedstone_BC8() {}

    @Nonnull
    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(true);
    }

    @Override
    public boolean isBurning() {
        return isRedstonePowered;
    }

    @Override
    protected void engineUpdate() {
        super.engineUpdate();
        if (world.getTotalWorldTime() % 16 == 0) {
            this.addPower(MjAPI.MJ);
        }
    }

    @Override
    public double getPistonSpeed() {
        return super.getPistonSpeed() / 2;
    }

    @Override
    public long getMaxPower() {
        return MjAPI.MJ * 100;
    }

    @Override
    public long minPowerReceived() {
        return MjAPI.MJ / 10;
    }

    @Override
    public long maxPowerReceived() {
        return MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return MjAPI.MJ;
    }

    @Override
    public float explosionRange() {
        return 0;
    }

    @Override
    public long getCurrentOutput() {
        return MjAPI.MJ / 20;
    }
}
