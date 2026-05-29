/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.cap;

import java.util.ArrayList;
import java.util.List;

// STUB(R.Chen): the Forge capability model (Capability<T> / ICapabilityProvider / hasCapability /
// getCapability, keyed per EnumPipePart face) has no direct Fabric equivalent. Capability exposure now
// happens via Transfer-API lookups (ItemStorage.SIDED / FluidStorage.SIDED / EnergyStorage.SIDED)
// registered against the owning BlockEntity type in the ModInitializer. Only addProvider — used by
// TileBC_Neptune's constructor to register itemManager — is retained as a registry until that wiring
// lands; the per-face cap getters are deferred. See LegacyCapabilityStubs.
public class CapabilityHelper {

    private final List<Object> additional = new ArrayList<>();

    public CapabilityHelper() {}

    public Object addProvider(Object provider) {
        if (provider != null) {
            additional.add(provider);
        }
        return provider;
    }
}
