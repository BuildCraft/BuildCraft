package buildcraft.lib.misc.data;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.profiler.Profiler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** An extension for minecraft's {@link Profiler} class that returns {@link AutoCloseable} profiler sections. */
public class ProfilerBC {

    @Environment(EnvType.CLIENT)
    public static ProfilerBC getClient() {
        return new ProfilerBC(MinecraftClient.getInstance().getProfiler());
    }

    private final Profiler profiler;

    public ProfilerBC(Profiler profiler) {
        this.profiler = profiler;
    }

    public IProfilerSection start(String name) {
        profiler.push(name);
        return profiler::endSection;
    }

    public IProfilerSection start(String... names) {
        for (String s : names) {
            profiler.push(s);
        }
        return () -> {
            for (int i = 0; i < names.length; i++) {
                profiler.pop();
            }
        };
    }

    public interface IProfilerSection extends AutoCloseable {
        /** Ends the current profiler section. Will only throw if this was called incorrectly. */
        @Override
        void close();
    }
}
