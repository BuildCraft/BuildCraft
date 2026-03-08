/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package buildcraft.energy.generation.structure;

import buildcraft.lib.misc.data.Box;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;

import java.util.List;

public final class OilPlacer {
    private final IWorld level;
    private final List<OilGenStructurePart> structurePieces;
    private final Box box;

    public OilPlacer(ISeedReader level, List<OilGenStructurePart> structurePieces, MutableBoundingBox bounds) {
        this.level = level;
        this.structurePieces = structurePieces;
        this.box = new Box(new MutableBoundingBox(bounds));
    }

    public void place() {
        IWorld world = this.level;
        OilGenStructurePart.Spring spring = null;
        for (OilGenStructurePart struct : structurePieces) {
            struct.generate(world, box);
            if (struct instanceof OilGenStructurePart.Spring) {
                spring = (OilGenStructurePart.Spring) struct;
            }
        }
        if (spring != null && box.contains(spring.pos)) {
            int count = 0;
            for (OilGenStructurePart struct : structurePieces) {
                count += struct.countOilBlocks();
            }
            spring.generate(world, count);
        }
    }
}
