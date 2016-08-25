package buildcraft.lib.particle;

public class ParticlePipes {
    public static final IParticlePositionPipe DUPLICATE_SPREAD;

    static {
        DUPLICATE_SPREAD = ParticleCountMultiplier.getOptionProvider().andThen(ParticleDirectionalSpread.MEDIUM);
    }
}
