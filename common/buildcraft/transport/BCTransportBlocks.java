/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport;

import net.minecraft.block.entity.BlockEntityType;

import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.tile.TilePipeHolder;

// STUB(R.Chen): block/item registration moves to BCTransportInitializer (Fabric Registry.register against
// Registries.BLOCK / BLOCK_ENTITY_TYPE). The Forge RegistrationHelper.addBlock/registerTile flow is dropped.
// Fields are left as null handles so already-migrated code (TilePipeHolder, SchematicBlockPipe) can reference
// BCTransportBlocks.pipeHolder; the initializer assigns them at mod-init time.
public class BCTransportBlocks {

    // STUB(R.Chen): filteredBuffer dropped here — BlockFilteredBuffer is not yet migrated. Restore the field
    // (typed BlockFilteredBuffer) once that block lands; nothing in the migrated leaf set references it.
    public static BlockPipeHolder pipeHolder;

    // STUB(R.Chen): the registered BlockEntityType for the pipe tile (was the Forge BlockEntity registration).
    // BlockPipeHolder.createBlockEntity / getTicker reference this; the Phase 4F transport initializer builds it
    // via FabricBlockEntityTypeBuilder.create(TilePipeHolder::new, pipeHolder) and assigns it here.
    public static BlockEntityType<TilePipeHolder> pipeHolderTile;

    public static void preInit() {
        // STUB(R.Chen): registered in BCTransportInitializer (Fabric registration).
    }
}
