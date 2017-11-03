/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filler;

import java.util.BitSet;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.statement.FullStatement;

import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Template;

public class Filling {
    public static Template.BuildingInfo createBuildingInfo(BlockPos basePos,
                                                           IFillerStatementContainer filler,
                                                           FullStatement<IFillerPattern> patternStatement,
                                                           IStatementParameter[] params,
                                                           boolean inverted) {
        Template.FilledTemplate filledTemplate = (Template.FilledTemplate) patternStatement.get().createTemplate(
            filler,
            (pos, size) -> {
                Template template = new Template();
                template.size = size;
                template.offset = pos;
                template.data = new BitSet(Snapshot.getDataSize(size));
                return template.getFilledTemplate();
            },
            params
        );
        if (filledTemplate == null) {
            return null;
        }
        if (inverted) {
            filledTemplate.getTemplate().invert();
        }
        return filledTemplate.getTemplate().new BuildingInfo(BlockPos.ORIGIN, Rotation.NONE);
    }
}
