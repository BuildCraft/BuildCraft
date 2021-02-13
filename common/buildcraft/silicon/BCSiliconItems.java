/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import buildcraft.api.facades.FacadeAPI;

import buildcraft.lib.item.ItemPluggableSimple;
import buildcraft.lib.registry.RegistrationHelper;

import buildcraft.silicon.item.ItemGateCopier;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.item.ItemPluggableGate;
import buildcraft.silicon.item.ItemPluggableLens;
import buildcraft.silicon.item.ItemRedstoneChipset;
import buildcraft.silicon.plug.PluggablePulsar;

public class BCSiliconItems {
    private static final RegistrationHelper HELPER = new RegistrationHelper();

    public static ItemRedstoneChipset redstoneChipset;
    public static ItemGateCopier gateCopier;

    public static ItemPluggableGate plugGate;
    public static ItemPluggableLens plugLens;
    public static ItemPluggableSimple plugPulsar;
    public static ItemPluggableSimple plugLightSensor;
    public static ItemPluggableFacade plugFacade;

    public static void preInit() {
        redstoneChipset = HELPER.addItem(new ItemRedstoneChipset("item.redstone_chipset"));
        gateCopier = HELPER.addItem(new ItemGateCopier("item.gate_copier"));

        plugGate = HELPER.addItem(new ItemPluggableGate("item.plug.gate"));
        plugLens = HELPER.addItem(new ItemPluggableLens("item.plug.lens"));
        plugPulsar = HELPER.addItem(new ItemPluggableSimple("item.plug.pulsar", BCSiliconPlugs.pulsar,
            PluggablePulsar::new, ItemPluggableSimple.PIPE_BEHAVIOUR_ACCEPTS_RS_POWER));
        plugLightSensor = HELPER.addItem(new ItemPluggableSimple("item.plug.light_sensor", BCSiliconPlugs.lightSensor));
        plugFacade = HELPER.addItem(new ItemPluggableFacade("item.plug.facade"));
        FacadeAPI.facadeItem = plugFacade;
    }
}
