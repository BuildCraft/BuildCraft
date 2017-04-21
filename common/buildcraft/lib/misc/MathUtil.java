package buildcraft.lib.misc;

public class MathUtil {
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
}
