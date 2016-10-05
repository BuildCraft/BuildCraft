/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import buildcraft.api.statements.StatementManager;
import buildcraft.core.statements.*;

public class BCCoreStatements {
    public static final TriggerTrue TRIGGER_TRUE;

    public static final TriggerMachine TRIGGER_MACHINE_ACTIVE;
    public static final TriggerMachine TRIGGER_MACHINE_INACTIVE;

    public static final TriggerRedstoneInput TRIGGER_REDSTONE_ACTIVE;
    public static final TriggerRedstoneInput TRIGGER_REDSTONE_INACTIVE;
    public static final ActionRedstoneOutput ACTION_REDSTONE;

    // TODO: All of these!

    // public static final TriggerEnergy TRIGGER_ENERGY_HIGH = new TriggerEnergy(true);
    // public static final TriggerEnergy TRIGGER_ENERGY_LOW = new TriggerEnergy(false);
    // public static final TriggerInventory triggerEmptyInventory = new TriggerInventory(TriggerInventory.State.Empty);
    // public static final TriggerInventory triggerContainsInventory = new
    // TriggerInventory(TriggerInventory.State.Contains);
    // public static final TriggerInventory triggerSpaceInventory = new TriggerInventory(TriggerInventory.State.Space);
    // public static final TriggerInventory triggerFullInventory = new TriggerInventory(TriggerInventory.State.Full);
    // public static final TriggerFluidContainer triggerEmptyFluid = new
    // TriggerFluidContainer(TriggerFluidContainer.State.Empty);
    // public static final TriggerFluidContainer triggerContainsFluid = new
    // TriggerFluidContainer(TriggerFluidContainer.State.Contains);
    // public static final TriggerFluidContainer triggerSpaceFluid = new
    // TriggerFluidContainer(TriggerFluidContainer.State.Space);
    // public static final TriggerFluidContainer triggerFullFluid = new
    // TriggerFluidContainer(TriggerFluidContainer.State.Full);
    // public static ITriggerExternal triggerInventoryBelow25 = new
    // TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW25);
    // public static ITriggerExternal triggerInventoryBelow50 = new
    // TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW50);
    // public static ITriggerExternal triggerInventoryBelow75 = new
    // TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW75);
    // public static final TriggerFluidContainerLevel triggerFluidContainerBelow25 = new
    // TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW25);
    // public static final TriggerFluidContainerLevel triggerFluidContainerBelow50 = new
    // TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW50);
    // public static final TriggerFluidContainerLevel triggerFluidContainerBelow75 = new
    // TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW75);
    // public static final ActionMachineControl[] ACTION_CONTROL;

    static {
        TRIGGER_TRUE = new TriggerTrue();

        TRIGGER_MACHINE_ACTIVE = new TriggerMachine(true);
        TRIGGER_MACHINE_INACTIVE = new TriggerMachine(false);

        TRIGGER_REDSTONE_ACTIVE = new TriggerRedstoneInput(true);
        TRIGGER_REDSTONE_INACTIVE = new TriggerRedstoneInput(false);
        ACTION_REDSTONE = new ActionRedstoneOutput();

        // ACTION_CONTROL = new ActionMachineControl[IControllable.Mode.values().length];
        // for (IControllable.Mode mode : IControllable.Mode.values()) {
        // if (mode != IControllable.Mode.Unknown && mode != IControllable.Mode.Mode) {
        // ACTION_CONTROL[mode.ordinal()] = new ActionMachineControl(mode);
        // }
        // }
    }

    public static void preInit() {
        StatementManager.registerTriggerProvider(CoreTriggerProvider.INSTANCE);
        StatementManager.registerActionProvider(CoreActionProvider.INSTANCE);
    }
}
