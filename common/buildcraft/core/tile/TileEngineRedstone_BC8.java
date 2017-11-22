/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.tile;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.AdvancementUtil;

public class TileEngineRedstone_BC8 extends TileEngineBase_BC8 {
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftcore:free_power");
    private boolean givenAdvancement = false;

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
            if (isPumping && !givenAdvancement) {
                givenAdvancement = AdvancementUtil.unlockAdvancement(this.getOwner().getId(), ADVANCEMENT);
            }
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
        return 4 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 4 * MjAPI.MJ;
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
