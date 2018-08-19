package buildcraft.lib.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import net.minecraft.profiler.Profiler;

/** Provides a few methods for writing the results from a vanilla {@link Profiler} to a file or something else. */
public class ProfilerUtil {

    /** Calls {@link #writeProfilerResults(Profiler, String, ILogAcceptor)} with {@link System#out} as the
     * {@link ILogAcceptor}. */
    public static void printProfilerResults(Profiler profiler, String rootName) {
        writeProfilerResults(profiler, rootName, System.out::println);
    }

    /** Calls {@link #writeProfilerResults(Profiler, String, ILogAcceptor)} but saves the output to a file.
     * 
     * @throws IOException if the file exists but is a directory rather than a regular file, does not exist but cannot
     *             be created, or cannot be opened for any other reason, or if an I/O exception occurred while wrting
     *             the profiler results. */
    public static void saveProfilerResults(Profiler profiler, String rootName, File dest) throws IOException {
        dest = dest.getAbsoluteFile();
        dest.getParentFile().mkdirs();
        saveProfilerResults(profiler, rootName, dest.toPath());
    }

    /** Calls {@link #writeProfilerResults(Profiler, String, ILogAcceptor)} but saves the output to a file.
     * 
     * @throws IOException if the file exists but is a directory rather than a regular file, does not exist but cannot
     *             be created, or cannot be opened for any other reason, or if an I/O exception occurred while writing
     *             the profiler results. */
    public static void saveProfilerResults(Profiler profiler, String rootName, Path dest) throws IOException {
        try (BufferedWriter br = Files.newBufferedWriter(dest, StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            writeProfilerResults(profiler, rootName, str -> {
                br.write(str);
                br.newLine();
            });
            br.flush();
        }
    }

    /** @param profiler
     * @param rootName The base name to use. Most of the time you just want to use "root".
     * @param dest The method to call with the finished lines.
     * @throws E if {@link ILogAcceptor#write(String)} throws an exception. */
    public static <E extends Throwable> void writeProfilerResults(Profiler profiler, String rootName,
        ILogAcceptor<E> dest) throws E {
        writeProfilerResults_Internal(profiler, rootName, 0, dest);
    }

    private static <E extends Throwable> void writeProfilerResults_Internal(Profiler profiler, String sectionName,
        int indent, ILogAcceptor<E> dest) throws E {

        List<Profiler.Result> list = profiler.getProfilingData(sectionName);

        if (list != null && list.size() >= 3) {
            for (int i = 1; i < list.size(); ++i) {
                Profiler.Result profiler$result = list.get(i);
                StringBuilder builder = new StringBuilder();
                builder.append(String.format("[%02d] ", indent));

                for (int j = 0; j < indent; ++j) {
                    builder.append("|   ");
                }

                builder.append(profiler$result.profilerName);
                builder.append(" - ");
                builder.append(String.format("%.2f", profiler$result.usePercentage));
                builder.append("%/");
                builder.append(String.format("%.2f", profiler$result.totalUsePercentage));
                dest.write(builder.toString());

                if (!"unspecified".equals(profiler$result.profilerName)) {
                    if (indent > 20) {
                        // Something probably went wrong
                        dest.write("[[ Too deep! ]]");
                        continue;
                    }
                    writeProfilerResults_Internal(profiler, sectionName + "." + profiler$result.profilerName,
                        indent + 1, dest);
                }
            }
        }
    }

    /** @param <E> The base exception type that {@link #write(String)} might throw. Used to allow writing to files to
     *            throw a (checked) exception, but {@link System#out} to never throw. */
    public interface ILogAcceptor<E extends Throwable> {
        void write(String line) throws E;
    }
}
