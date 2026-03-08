package buildcraft.lib.misc;


import buildcraft.api.core.BCLog;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.Util;
import net.minecraft.util.profiling.*;
import org.apache.commons.lang3.ObjectUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;

/** Provides a few methods for writing the results from a vanilla {@link ProfilerFiller} to a file or something else. */
public class ProfilerUtil {

    /** Calls {@link #writeProfilerResults(ActiveProfiler, String)} with {@link System#out} as the
     * {@link ILogAcceptor}. */
//    public static void printProfilerResults(Profiler profiler, String rootName)
    public static void printProfilerResults(ActiveProfiler profiler, String rootName) {
//        printProfilerResults(profiler, rootName, -1);
        printProfilerResults(profiler, rootName);
    }

//    /** Calls {@link #writeProfilerResults(Profiler, String, ILogAcceptor)} with {@link System#out} as the
//     * {@link ILogAcceptor}. */
//    public static void printProfilerResults(Profiler profiler, String rootName, long totalNanoseconds) {
//        writeProfilerResults(profiler, rootName, totalNanoseconds, System.out::println);
//    }

    /** Calls {@link #writeProfilerResults(ActiveProfiler, String)} with {@link BCLog#logger
     * BCLog.logger}::{@link org.apache.logging.log4j.Logger#info(CharSequence) info} as the {@link ILogAcceptor}. */
//    public static void logProfilerResults(Profiler profiler, String rootName)
    public static void logProfilerResults(ActiveProfiler profiler, String rootName) {
        logProfilerResults(profiler, rootName, -1);
    }

    /** Calls {@link #writeProfilerResults(ActiveProfiler, String)} with {@link BCLog#logger
     * BCLog.logger}::{@link org.apache.logging.log4j.Logger#info(CharSequence) info} as the {@link ILogAcceptor}. */
//    public static void logProfilerResults(Profiler profiler, String rootName, long totalNanoseconds)
    public static void logProfilerResults(ActiveProfiler profiler, String rootName, long totalNanoseconds) {
//        writeProfilerResults(profiler, rootName, totalNanoseconds, BCLog.logger::info);
        writeProfilerResults(profiler, rootName);
    }

//    /** Calls {@link #writeProfilerResults(Profiler, String, ILogAcceptor)} but saves the output to a file.
//     *
//     * @throws IOException if the file exists but is a directory rather than a regular file, does not exist but cannot
//     *             be created, or cannot be opened for any other reason, or if an I/O exception occurred while writing
//     *             the profiler results. */
//    public static void saveProfilerResults(Profiler profiler, String rootName, Path dest) throws IOException {
//        saveProfilerResults(profiler, rootName, -1, dest);
//    }

//    /** Calls {@link #writeProfilerResults(Profiler, String, ILogAcceptor)} but saves the output to a file.
//     *
//     * @throws IOException if the file exists but is a directory rather than a regular file, does not exist but cannot
//     *             be created, or cannot be opened for any other reason, or if an I/O exception occurred while wrting
//     *             the profiler results. */
//    public static void saveProfilerResults(Profiler profiler, String rootName, File dest) throws IOException {
//        dest = dest.getAbsoluteFile();
//        dest.getParentFile().mkdirs();
//        saveProfilerResults(profiler, rootName, -1, dest.toPath());
//    }

    /** Calls {@link #writeProfilerResults(ActiveProfiler, String)} but saves the output to a file.
     *
     * @throws IOException if the file exists but is a directory rather than a regular file, does not exist but cannot
     *             be created, or cannot be opened for any other reason, or if an I/O exception occurred while writing
     *             the profiler results. */
//    public static void saveProfilerResults(Profiler profiler, String rootName, long totalNanoseconds, Path dest)
    public static void saveProfilerResults(ActiveProfiler profiler, String rootName, Path dest) throws IOException {
        try (BufferedWriter br = Files.newBufferedWriter(dest, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE))
        {
//            writeProfilerResults(profiler, rootName, str -> {
//                br.write(str);
//                br.newLine();
//            });
            writeProfilerResults(profiler, rootName);
            br.flush();
        }
    }

//    /** @param rootName The base name to use. Most of the time you just want to use "root".
//     * @param dest The method to call with the finished lines.
//     * @throws E if {@link ILogAcceptor#write(String)} throws an exception. */
//    public static <E extends Throwable> void writeProfilerResults(Profiler profiler, String rootName, ILogAcceptor<E> dest) throws E {
//        writeProfilerResults(profiler, rootName, -1, dest);
//    }

    /** @param rootName The base name to use. Most of the time you just want to use "root".
     * @throws E if {@link ILogAcceptor#write(String)} throws an exception. */
//    public static <E extends Throwable> void writeProfilerResults(Profiler profiler, String rootName, long totalNanoseconds, ILogAcceptor<E> dest) throws E
    public static <E extends Throwable> void writeProfilerResults(ActiveProfiler profiler, String rootName) throws E {
        StringBuilder builder = new StringBuilder();
        FilledProfileResults result = (FilledProfileResults) profiler.getResults();
        writeProfilerResults_Internal(result, rootName, 0, builder);
    }

    // private static <E extends Throwable> void writeProfilerResults_Internal(Profiler profiler, String sectionName, long totalNanoseconds, int indent, ILogAcceptor<E> dest) throws E
    private static <E extends Throwable> void writeProfilerResults_Internal(FilledProfileResults result, String sectionName, int indent, StringBuilder builder) throws E {
        List<ResultField> list = result.getTimes(sectionName);
        Object2LongMap<String> object2longmap = ObjectUtils.firstNonNull(result.entries.get(sectionName), FilledProfileResults.EMPTY).getCounters();
        object2longmap.forEach((p_230100_3_, p_230100_4_) ->
        {
            FilledProfileResults.indentLine(builder, indent).append('#').append(p_230100_3_).append(' ').append((Object) p_230100_4_).append('/').append(p_230100_4_ / (long) result.getTickDuration()).append('\n');
        });
        if (list.size() >= 3) {
            for (int i = 1; i < list.size(); ++i) {
                ResultField datapoint = list.get(i);
                FilledProfileResults.indentLine(builder, indent).append(datapoint.name).append('(').append(datapoint.count).append('/').append(String.format(Locale.ROOT, "%.0f", (float) datapoint.count / (float) result.getTickDuration())).append(')').append(" - ").append(String.format(Locale.ROOT, "%.2f", datapoint.percentage)).append("%/").append(String.format(Locale.ROOT, "%.2f", datapoint.globalPercentage)).append("%\n");
                if (!"unspecified".equals(datapoint.name)) {
                    try {
                        writeProfilerResults_Internal(result, sectionName + '\u001e' + datapoint.name, indent + 1, builder);
                    } catch (Exception exception) {
                        builder.append("[[ EXCEPTION ").append((Object) exception).append(" ]]");
                    }
                }
            }
        }
    }

    /** @param <E> The base exception type that {@link #write(String)} might throw. Used to allow writing to files to
     *            throw a (checked) exception, but {@link System#out} to never throw. */
    public interface ILogAcceptor<E extends Throwable> {
        void write(String line) throws E;
    }

    public interface ProfilerEntry {
        void startSection(String name);

        void endSection();

        default void endStartSection(String name) {
            endSection();
            startSection(name);
        }
    }

    // public static ProfilerEntry createEntry(Profiler p1, Profiler p2)
    public static ProfilerEntry createEntry(ProfilerFiller p1, ProfilerFiller p2) {
//        if (p1.profilingEnabled)
        if (p1 instanceof ActiveProfiler) {
//            if (p2.profilingEnabled)
            if (p2 instanceof ActiveProfiler) {
                return new ProfilerEntry2(p1, p2);
            } else {
                return new ProfilerEntry1(p1);
            }
        } else {
//            if (p2.profilingEnabled)
            if (p2 instanceof ActiveProfiler) {
                return new ProfilerEntry1(p2);
            } else {
                return ProfilerEntry0.INSTANCE;
            }
        }
    }

    static enum ProfilerEntry0 implements ProfilerEntry {
        INSTANCE;

        @Override
        public void startSection(String name) {
            // NO-OP
        }

        @Override
        public void endSection() {
            // NO-OP
        }
    }

    static final class ProfilerEntry1 implements ProfilerEntry {
        final ProfilerFiller p;

        ProfilerEntry1(ProfilerFiller p) {
            this.p = p;
        }

        @Override
        public void startSection(String name) {
//            p.startSection(name);
            p.push(name);
        }

        @Override
        public void endSection() {
//            p.endSection();
            p.pop();
        }
    }

    static final class ProfilerEntry2 implements ProfilerEntry {
        // final Profiler p1, p2;
        final ProfilerFiller p1, p2;

        // ProfilerEntry2(Profiler p1, Profiler p2)
        ProfilerEntry2(ProfilerFiller p1, ProfilerFiller p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        public void startSection(String name) {
//            p1.startSection(name);
//            p2.startSection(name);
            p1.push(name);
            p2.push(name);
        }

        @Override
        public void endSection() {
//            p1.endSection();
//            p2.endSection();
            p1.pop();
            p2.pop();
        }
    }

    // Calen
    public static ActiveProfiler newProfiler() {
        ActiveProfiler ret = new ActiveProfiler(Util.timeSource, () ->
        {
            return 0;
        }, false);
        ret.startTick();
        return ret;
    }

    public static ProfilerFiller newProfiler(boolean enable) {
        if (enable) {
            ActiveProfiler ret = new ActiveProfiler(Util.timeSource, () ->
            {
                return 0;
            }, false);
            ret.startTick();
            return ret;
        } else {
            return InactiveProfiler.INSTANCE;
        }
    }
}
