/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon;

import buildcraft.silicon.item.ItemGateCopier;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.item.ItemPluggableGate;
import buildcraft.silicon.item.ItemPluggableLens;
import buildcraft.silicon.item.ItemPluggablePulsar;
import buildcraft.silicon.item.ItemRedstoneChipset;

/**
 * STUB(R.Chen): BCSiliconItems registration deferred until lib.item (ItemBC_Neptune,
 * RegistrationHelper) is migrated. Item instances are null placeholders.
 */
public class BCSiliconItems {

    // STUB(R.Chen): Item references are null until preInit() is wired.
    public static ItemRedstoneChipset redstoneChipset;
    public static ItemGateCopier gateCopier;
    public static ItemPluggableGate plugGate;
    public static ItemPluggableLens plugLens;
    public static ItemPluggablePulsar plugPulsar;
    // STUB(R.Chen): plugLightSensor and plugTimer items not yet stubbed as ItemPluggableSimple equivalent.
    public static ItemPluggableFacade plugFacade;

    public static void preInit() {
        // STUB(R.Chen): Registry.register(Registries.ITEM, ...) calls deferred.
    }
}
