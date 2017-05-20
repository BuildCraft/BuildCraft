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
