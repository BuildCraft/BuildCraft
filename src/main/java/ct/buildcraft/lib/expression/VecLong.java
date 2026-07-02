/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression;

/** An immutable class the represents a 4 part vector of longs. It is *highly* recommended that you have a way to
 * convert this to a more specific implementation (for example another library provided Vector3i class) */
public class VecLong {
    public static final VecLong ZERO = new VecLong(0, 0, 0, 0);

    public final long a, b, c, d;

    public VecLong(long a) {
        this(a, 0, 0, 0);
    }

    public VecLong(long a, long b) {
        this(a, b, 0, 0);
    }

    public VecLong(long a, long b, long c) {
        this(a, b, c, 0);
    }

    public VecLong(long a, long b, long c, long d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public VecLong add(long a_, long b_, long c_, long d_) {
        return new VecLong(a + a_, b + b_, c + c_, d + d_);
    }

    public VecLong sub(long a_, long b_, long c_, long d_) {
        return new VecLong(a - a_, b - b_, c - c_, d - d_);
    }

    public VecLong scale(long a_, long b_, long c_, long d_) {
        return new VecLong(a * a_, b * b_, c * c_, d * d_);
    }

    public VecLong div(long a_, long b_, long c_, long d_) {
        return new VecLong(a / a_, b / b_, c / c_, d / d_);
    }

    public VecLong add(VecLong w) {
        return new VecLong(a + w.a, b + w.b, c + w.c, d + w.d);
    }

    public VecLong sub(VecLong neg) {
        return new VecLong(a - neg.a, b - neg.b, c - neg.c, d - neg.d);
    }

    public VecLong scale(VecLong s) {
        return new VecLong(a * s.a, b * s.b, c * s.c, d * s.d);
    }

    public VecLong div(VecLong s) {
        return new VecLong(a / s.a, b / s.b, c / s.c, d / s.d);
    }

    public long dotProduct2(VecLong w) {
        return a * w.a + b * w.b;
    }

    public long dotProduct3(VecLong w) {
        return a * w.a + b * w.b + c * w.c;
    }

    public long dotProduct4(VecLong w) {
        return a * w.a + b * w.b + c * w.c + d * w.d;
    }

    public double length() {
        return Math.sqrt(a * a + b * b + c * c + d * d);
    }

    public VecLong crossProduct(VecLong w) {
        long x = b * w.c - c * w.b;
        long y = c * w.b - a * w.c;
        long z = a * w.b - b * w.a;
        return new VecLong(x, y, z, 1);
    }

    public double distance(VecLong to) {
        long da = a - to.a;
        long db = b - to.b;
        long dc = c - to.c;
        long dd = d - to.d;
        return Math.sqrt(da * da + db * db + dc * dc + dd * dd);
    }

    public VecDouble castToDouble() {
        return new VecDouble(a, b, c, d);
    }

    @Override
    public String toString() {
        return "{ " + a + ", " + b + ", " + c + ", " + d + " }";
    }
}
