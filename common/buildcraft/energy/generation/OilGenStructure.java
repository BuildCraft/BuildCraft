/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): oil generation world-gen deferred
package buildcraft.energy.generation;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.misc.data.Box;

public abstract class OilGenStructure {

    public final Box box;

    public enum ReplaceType {
        ALWAYS {
            @Override
            public boolean canReplace(World world, BlockPos pos) { return true; }
        },
        IS_FOR_OIL {
            @Override
            public boolean canReplace(World world, BlockPos pos) { return false; }
        };
        public abstract boolean canReplace(World world, BlockPos pos);
    }

    public OilGenStructure(Box containingBox, ReplaceType replaceType) {
        this.box = containingBox;
    }

    public final void generate(World world, Box within) {
        // STUB
    }

    protected abstract void generateWithin(World world, Box intersect);

    protected abstract int countOilBlocks();

    public static class GenByPredicate extends OilGenStructure {
        public GenByPredicate(Box box, ReplaceType type) {
            super(box, type);
        }

        @Override
        protected void generateWithin(World world, Box intersect) {}

        @Override
        protected int countOilBlocks() { return 0; }
    }
}
