/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
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
