/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.mj;

import javax.annotation.Nonnull;

import ct.buildcraft.api.mj.IMjConnector;
import ct.buildcraft.api.mj.IMjReadable;
import ct.buildcraft.api.mj.IMjReceiver;
import ct.buildcraft.api.mj.MjBattery;

import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class MjBatteryReceiver implements IMjReceiver, IMjReadable {
    private final MjBattery battery;

    public MjBatteryReceiver(MjBattery battery) {
        this.battery = battery;
    }

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return battery.getCapacity() - battery.getStored();
    }

    @Override
    public long receivePower(long microJoules, FluidAction simulate) {
        return battery.addPowerChecking(microJoules, simulate);
    }

    @Override
    public long getStored() {
        return battery.getStored();
    }

    @Override
    public long getCapacity() {
        return battery.getCapacity();
    }
}
