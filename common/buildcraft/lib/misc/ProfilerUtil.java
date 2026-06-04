/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

// Yarn 1.20.1: Profiler is at net.minecraft.util.profiler.Profiler (interface).
// API changed: push(String)/pop() replace startSection/endSection; Profiler.Result
// and getProfilingData() were removed — the writeProfilerResults* methods below
// are stubbed until a new analysis approach is found.
import net.minecraft.util.profiler.Profiler;

/** Provides a few methods for writing the results from a vanilla {@link Profiler} to a file or something else. */
public class ProfilerUtil {

    // TODO(R.Chen): Profiler.getProfilingData() and Profiler.Result were removed in 1.20.1.
    // The write/print/save/log result methods below cannot be implemented without a new
    // profiling data API. Stub them until a replacement strategy is decided.

    public static void printProfilerResults(Profiler profiler, String rootName) {
        throw new UnsupportedOperationException("TODO(R.Chen): profiler result output not yet ported to 1.20.1");
    }

    public static void printProfilerResults(Profiler profiler, String rootName, long totalNanoseconds) {
        throw new UnsupportedOperationException("TODO(R.Chen): profiler result output not yet ported to 1.20.1");
    }

    public static void logProfilerResults(Profiler profiler, String rootName) {
        throw new UnsupportedOperationException("TODO(R.Chen): profiler result output not yet ported to 1.20.1");
    }

    public static void logProfilerResults(Profiler profiler, String rootName, long totalNanoseconds) {
        throw new UnsupportedOperationException("TODO(R.Chen): profiler result output not yet ported to 1.20.1");
    }

    public static void saveProfilerResults(Profiler profiler, String rootName, Path dest) throws IOException {
        throw new UnsupportedOperationException("TODO(R.Chen): profiler result output not yet ported to 1.20.1");
    }

    public static void saveProfilerResults(Profiler profiler, String rootName, File dest) throws IOException {
        throw new UnsupportedOperationException("TODO(R.Chen): profiler result output not yet ported to 1.20.1");
    }

    public static void saveProfilerResults(Profiler profiler, String rootName, long totalNanoseconds, Path dest)
        throws IOException {
        throw new UnsupportedOperationException("TODO(R.Chen): profiler result output not yet ported to 1.20.1");
    }

    public static <E extends Throwable> void writeProfilerResults(Profiler profiler, String rootName,
        ILogAcceptor<E> dest) throws E {
        throw new UnsupportedOperationException("TODO(R.Chen): profiler result output not yet ported to 1.20.1");
    }

    public static <E extends Throwable> void writeProfilerResults(Profiler profiler, String rootName,
        long totalNanoseconds, ILogAcceptor<E> dest) throws E {
        throw new UnsupportedOperationException("TODO(R.Chen): profiler result output not yet ported to 1.20.1");
    }

    /** @param <E> The base exception type that {@link #write(String)} might throw. Used to allow writing to files to
     *            throw a (checked) exception, but {@link System#out} to never throw. */
    public interface ILogAcceptor<E extends Throwable> {
        void write(String line) throws E;
    }

    public interface ProfilerEntry {
        // Yarn 1.20.1: Profiler.push()/pop() replace startSection()/endSection().
        void startSection(String name);

        void endSection();

        default void endStartSection(String name) {
            endSection();
            startSection(name);
        }
    }

    // TODO(R.Chen): Profiler.profilingEnabled was a public field in 1.12.2 used to
    // skip profiling overhead. In 1.20.1 Profiler is an interface with no such flag;
    // always route to the wrapped profiler(s). Consider using DummyProfiler detection
    // in a later pass if the overhead matters.
    public static ProfilerEntry createEntry(Profiler p1, Profiler p2) {
        return new ProfilerEntry2(p1, p2);
    }

    public static ProfilerEntry createEntry(Profiler p) {
        return new ProfilerEntry1(p);
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
        final Profiler p;

        ProfilerEntry1(Profiler p) {
            this.p = p;
        }

        @Override
        public void startSection(String name) {
            p.push(name);
        }

        @Override
        public void endSection() {
            p.pop();
        }
    }

    static final class ProfilerEntry2 implements ProfilerEntry {
        final Profiler p1, p2;

        ProfilerEntry2(Profiler p1, Profiler p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        public void startSection(String name) {
            p1.push(name);
            p2.push(name);
        }

        @Override
        public void endSection() {
            p1.pop();
            p2.pop();
        }
    }

}
