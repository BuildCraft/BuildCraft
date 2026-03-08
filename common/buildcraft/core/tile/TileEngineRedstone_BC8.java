/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.tile;


import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCoreBlocks;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.AdvancementUtil;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class TileEngineRedstone_BC8 extends TileEngineBase_BC8 {
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftcore:free_power");
    private boolean givenAdvancement = false;

    public TileEngineRedstone_BC8() {
        super(BCCoreBlocks.engineWoodTile.get());
    }

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
        if (isRedstonePowered) {
            power = getMaxPower();
            if (level.getGameTime() % 16 == 0) {
                if (getHeatLevel() < 0.8) {
                    heat += 4;
                }
                if (isPumping && !givenAdvancement) {
                    givenAdvancement = AdvancementUtil.unlockAdvancement(this.getOwner().getId(), ADVANCEMENT);
                }
            }
        } else {
            power = 0;
        }
    }

    @Override
    public double getPistonSpeed() {
        return super.getPistonSpeed() / 2;
    }

    @Override
    public void updateHeatLevel() {
        if (heat > MIN_HEAT) {
            heat -= 0.2f;
            if (heat < MIN_HEAT) {
                heat = MIN_HEAT;
            }
        }
    }

    @Override
    protected int getMaxChainLength() {
        return 0;
    }

    @Override
    public long getMaxPower() {
        return MjAPI.MJ * 1;
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
