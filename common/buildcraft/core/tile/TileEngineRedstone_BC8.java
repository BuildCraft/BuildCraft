/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.tile;

import java.util.List;

import net.minecraft.util.EnumFacing;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.mj.IMjConnector;

import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.data.AverageInt;

public class TileEngineRedstone_BC8 extends TileEngineBase_BC8 {
    // TODO: Fix these numbers as they are probably completely wrong
    private static final long[] MICRO_JOULES = { 10_000, 20_000, 40_000, 80_000, 160_000, 0 };

    private EnumEnergyStage stage = EnumEnergyStage.BLUE;
    private AverageInt powerAvg = new AverageInt(10);
    private long lastChange = 0;

    public TileEngineRedstone_BC8() {}

    @Override
    public EnumEnergyStage getEnergyStage() {
        return stage;
    }

    @Override
    public int getMaxEngineCarryDist() {
        return 1;
    }

    @Override
    protected boolean canCarryOver(TileEngineBase_BC8 engine) {
        return engine instanceof TileEngineRedstone_BC8;
    }

    @Override
    public void update() {
        super.update();
        if (cannotUpdate()) return;
        if (world.isRemote) return;

        long target = MICRO_JOULES[getEnergyStage().ordinal()];

        if (isActive()) {
            addPower(target);
            // powerAvg.push((int) target);
        }

        // powerAvg.tick();
        // double average = powerAvg.getAverage();
        // if (average > 0.7 * target) {
        // if (worldObj.getTotalWorldTime() > lastChange + 100) {
        // if (stage != EnumEnergyStage.OVERHEAT) {
        // stage = EnumEnergyStage.VALUES[stage.ordinal() + 1];
        // lastChange = worldObj.getTotalWorldTime();
        // redrawBlock();
        // }
        // }
        // } else if (average < 0.3 * target) {
        // if (worldObj.getTotalWorldTime() > lastChange + 20) {
        // if (stage != EnumEnergyStage.BLUE) {
        // stage = EnumEnergyStage.VALUES[stage.ordinal() - 1];
        // lastChange = worldObj.getTotalWorldTime();
        // redrawBlock();
        // }
        // }
        // }
    }

    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(true);
    }

    @Override
    protected boolean hasFuelToBurn() {
        return true;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("");
        left.add("Average = " + powerAvg.getAverage());
    }
}
