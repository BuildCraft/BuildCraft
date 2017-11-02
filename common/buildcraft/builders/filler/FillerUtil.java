/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filler;

import java.util.Optional;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.statement.FullStatement;

import buildcraft.builders.snapshot.Template;

public class FillerUtil {
    public static Template.BuildingInfo createBuildingInfo(BlockPos basePos,
                                                           BlockPos size,
                                                           FullStatement<IFillerPattern> patternStatement,
                                                           IFillerStatementContainer filler,
                                                           IStatementParameter[] params,
                                                           boolean inverted) {
        return Optional.ofNullable(patternStatement.get().createTemplate(filler, params))
            .map(filledTemplate -> {
                Template template = new Template();
                template.size = size;
                template.offset = BlockPos.ORIGIN;
                template.data = filledTemplate;
                if (inverted) {
                    template.data.invert();
                }
                return template.new BuildingInfo(basePos, Rotation.NONE);
            })
            .orElse(null);
    }
}
