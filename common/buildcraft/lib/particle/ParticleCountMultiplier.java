package buildcraft.lib.particle;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

public enum ParticleCountMultiplier implements IParticlePositionPipe {
    MINIMAL(2),
    DECREASED(7),
    ALL(13);

    public static ParticleCountMultiplier getForOption() {
        GameSettings gs = Minecraft.getMinecraft().gameSettings;
        int count = gs.particleSetting % 3;
        if (count == 0) {
            return ALL;
        } else if (count == 1) {
            return DECREASED;
        }
        return MINIMAL;
    }

    public static IParticlePositionPipe getOptionProvider() {
        return (pos) -> {
            return getForOption().pipe(pos);
        };
    }

    private final int numExpanses;

    private ParticleCountMultiplier(int numExpanses) {
        this.numExpanses = numExpanses;
    }

    @Override
    public List<ParticlePosition> pipe(ParticlePosition pos) {
        List<ParticlePosition> list = new ArrayList<>();

        for (int i = 0; i < numExpanses; i++) {
            list.add(new ParticlePosition(pos.position, pos.motion));
        }

        return list;
    }

}
