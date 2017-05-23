/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.particle;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface IParticlePositionPipe {
    List<ParticlePosition> pipe(ParticlePosition pos);

    default List<ParticlePosition> pipe(List<ParticlePosition> positions) {
        List<ParticlePosition> nPositions = new ArrayList<>();
        for (ParticlePosition p : positions) {
            nPositions.addAll(pipe(p));
        }
        return nPositions;
    }

    default IParticlePositionPipe andThen(IParticlePositionPipe after) {
        return (pos) -> after.pipe(pipe(pos));
    }
}
