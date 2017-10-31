/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filler;

import java.util.BitSet;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.statement.FullStatement;

import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Template;

public class Filling {
    public static Template.BuildingInfo createBuildingInfo(BlockPos basePos,
                                                           BlockPos size,
                                                           FullStatement<IFillerPattern> patternStatement,
                                                           IStatementParameter[] params,
                                                           boolean inverted) {
        Template template = new Template();
        template.size = size;
        template.offset = BlockPos.ORIGIN;
        template.data = new BitSet(Snapshot.getDataSize(size));
        if (!patternStatement.get().fillTemplate(template.getFilledTemplate(), params)) {
            return null;
        }
        if (inverted) {
            template.invert();
        }
        return template.new BuildingInfo(basePos, Rotation.NONE);
    }
}
