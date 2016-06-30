/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.tile;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.core.lib.utils.AverageDouble;
import buildcraft.lib.engine.TileEngineBase_BC8;

public class TileEngineRedstone_BC8 extends TileEngineBase_BC8 {
    // TODO: Fix these numbers as they are probably completely wrong
    public static final int[] MILLIWATTS_PROVIDED = { 35, 50, 75, 100, 0 };

    private EnumEnergyStage stage = EnumEnergyStage.BLUE;
    private AverageDouble powerAvg = new AverageDouble(10);
    private long lastChange = 0;

    public TileEngineRedstone_BC8() {}

    // @Override
    // public NBTTagCompound writeToNBT(int stage) {
    // NBTTagCompound nbt = super.writeToNBT(stage);
    // if (stage == 0) {
    // nbt.setTag("stage", NBTUtils.writeEnum(this.stage));
    // nbt.setTag("average", powerAvg.serializeNBT());
    // }
    // return nbt;
    // }
    //
    // @Override
    // public void readFromNBT(int stage, NBTTagCompound nbt) {
    // super.readFromNBT(stage, nbt);
    // if (stage == 0) {
    // this.stage = NBTUtils.readEnum(nbt.getTag("stage"), EnumEnergyStage.class);
    // powerAvg.deserializeNBT(nbt.getCompoundTag("average"));
    // }
    // }

    @Override
    protected void sendPower(int power) {

    }

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
        powerAvg.tick();
        double average = powerAvg.getAverage();
        if (average > 1) {
            if (worldObj.getTotalWorldTime() > lastChange + 100) {
                if (stage != EnumEnergyStage.OVERHEAT) {
                    stage = EnumEnergyStage.VALUES[stage.ordinal() + 1];
                    lastChange = worldObj.getTotalWorldTime();
                    redrawBlock();
                }
            }
        } else if (average < 0.5) {
            if (worldObj.getTotalWorldTime() > lastChange + 20) {
                if (stage != EnumEnergyStage.BLUE) {
                    stage = EnumEnergyStage.VALUES[stage.ordinal() - 1];
                    lastChange = worldObj.getTotalWorldTime();
                    redrawBlock();
                }
            }
        }
    }
}
