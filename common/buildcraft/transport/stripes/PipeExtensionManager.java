/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.stripes;

// STUB(R.Chen): PipeExtensionManager — complex Forge world-interaction logic deferred to Phase 4E.
// Pipe extension/retraction logic depends on FakePlayer + BlockUtil.

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.pipe.IPipeExtensionManager;
import buildcraft.api.transport.pipe.PipeDefinition;

public class PipeExtensionManager implements IPipeExtensionManager {
    public static final PipeExtensionManager INSTANCE = new PipeExtensionManager();

    private final Set<PipeDefinition> retractionPipes = new HashSet<>();

    @Override
    public boolean requestPipeExtension(World world, BlockPos pos, Direction dir, IStripesActivator stripes,
        ItemStack stack) {
        return false; // STUB(R.Chen): FakePlayer + BlockUtil not yet migrated. Phase 4F.
    }

    @Override
    public void registerRetractionPipe(PipeDefinition pipeDefinition) {
        retractionPipes.add(pipeDefinition);
    }
}
