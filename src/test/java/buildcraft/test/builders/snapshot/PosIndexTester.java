/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.test.builders.snapshot;

import com.google.common.collect.ImmutableList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import net.minecraft.util.math.BlockPos;

import buildcraft.builders.snapshot.Snapshot;

@SuppressWarnings("DefaultAnnotationParam")
@RunWith(Parameterized.class)
public class PosIndexTester {
    private static final BlockPos SIZE = new BlockPos(6, 4, 8);
    @Parameterized.Parameter(0)
    public int x;
    @Parameterized.Parameter(1)
    public int y;
    @Parameterized.Parameter(2)
    public int z;

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        ImmutableList.Builder<Object[]> builder = new ImmutableList.Builder<>();
        for (int z = 0; z < SIZE.getZ(); z++) {
            for (int y = 0; y < SIZE.getY(); y++) {
                for (int x = 0; x < SIZE.getX(); x++) {
                    builder.add(new Object[] {x, y, z});
                }
            }
        }
        return builder.build();
    }

    @Test
    public void test() {
        BlockPos pos = new BlockPos(x, y, z);
        Assert.assertEquals(
            Integer.toString(Snapshot.posToIndex(SIZE, pos)),
            pos,
            Snapshot.indexToPos(SIZE, Snapshot.posToIndex(SIZE, pos))
        );
    }
}
