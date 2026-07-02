/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

public class MathUtil {
    // ItemStacks are commonly found in small stacks -- precompute for smaller numbers
    private static final short HCF_SIZE = 64;
    private static final short[][] HCF_TABLE;

    static {
        HCF_TABLE = new short[HCF_SIZE][HCF_SIZE];
        // Prefill a,0 as its cheapest (and the algorithm can't handle 0,0)
        for (short a = 0; a < HCF_SIZE; a++) {
            HCF_TABLE[a][0] = a;
        }
        for (short a = 0; a < HCF_SIZE; a++) {
            for (short b = 1; b <= a; b++) {
                HCF_TABLE[a][b] = findHcfDirect(a, b);
            }
        }
    }

    private static short findHcfDirect(short a, short b) {
        while (b > 0) {
            short t = b;
            b = (short) (a % b);
            a = t;
        }
        return a;
    }

    public static double interp(double interp, double from, double to) {
        return from * (1 - interp) + to * interp;
    }

    public static int clamp(int toClamp, int min, int max) {
        return Math.max(Math.min(toClamp, max), min);
    }

    public static int clamp(double toClamp, int min, int max) {
        return clamp((int) toClamp, min, max);
    }

    public static double clamp(double toClamp, double min, double max) {
        return Math.max(Math.min(toClamp, max), min);
    }

    public static long clamp(long toClamp, long min, long max) {
        return Math.max(Math.min(toClamp, max), min);
    }

    public static int findHighestCommonFactor(int a, int b) {
        if (b > a) {
            int t = b;
            b = a;
            a = t;
        }
        if (a < HCF_SIZE) {
            return HCF_TABLE[a][b];
        }
        while (b > 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    public static int findLowestCommonMultiple(int a, int b) {
        return (a / findHighestCommonFactor(a, b)) * b;
    }
}
