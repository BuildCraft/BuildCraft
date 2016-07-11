package buildcraft.lib.particle;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ParticlePipeManager {
    private static final TIntObjectHashMap<Supplier<IParticlePositionPipe>> PIPE_TYPES = new TIntObjectHashMap<>();

    static {
        // Just use ints rather than a full registry - its simpler
        PIPE_TYPES.put(0, ParticleDirectionalSpread::getForOption);
    }

    public static IParticlePositionPipe construct(int[] types) {
        IParticlePositionPipe pipe = null;
        for (int type : types) {
            Supplier<IParticlePositionPipe> supplier = PIPE_TYPES.get(type);
            IParticlePositionPipe thisPipe = null;
            if (supplier != null) {
                thisPipe = supplier.get();
            }
            if (thisPipe == null) {
                thisPipe = ParticlePositionIdentity.INSTANCE;
            }
            if (pipe == null) {
                pipe = thisPipe;
            } else {
                pipe = pipe.andThen(thisPipe);
            }
        }
        return pipe;
    }

    public enum ParticlePositionIdentity implements IParticlePositionPipe {
        INSTANCE;

        @Override
        public List<ParticlePosition> pipe(ParticlePosition pos) {
            return ImmutableList.of(pos);
        }
    }
}
