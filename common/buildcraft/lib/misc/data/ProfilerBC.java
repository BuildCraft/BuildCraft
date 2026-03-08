package buildcraft.lib.misc.data;

import net.minecraft.client.Minecraft;
import net.minecraft.profiler.IProfiler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** An extension for minecraft's {@link IProfiler} class that returns {@link AutoCloseable} profiler sections. */
public class ProfilerBC {

    @OnlyIn(Dist.CLIENT)
    public static ProfilerBC getClient() {
        return new ProfilerBC(Minecraft.getInstance().getProfiler());
    }

    private final IProfiler profiler;

    public ProfilerBC(IProfiler profiler) {
        this.profiler = profiler;
    }

    public IProfilerSection start(String name) {
        profiler.push(name);
        return profiler::pop;
    }

    public IProfilerSection start(String... names) {
        for (String s : names) {
            profiler.push(s);
        }
        return () ->
        {
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
