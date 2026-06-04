/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.pipe;

// STUB(R.Chen): SchematicBlockPipe — depends on buildcraft.api.schematics (builders module,
// not yet in libLeaf). The predicate static method is kept as a compile-safe stub;
// full pipe schematic support restores in Phase 4F when the schematics API lands.
public class SchematicBlockPipe {

    /** Predicate for SchematicBlockFactoryRegistry — always returns false until schematics API migrated. */
    public static boolean predicate(Object ctx) {
        return false; // STUB(R.Chen): ctx is SchematicBlockContext, not yet in libLeaf.
    }
}
