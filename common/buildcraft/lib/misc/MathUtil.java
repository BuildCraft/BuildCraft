package buildcraft.lib.misc;

public class MathUtil {
    public static double interp(double interp, double min, double max) {
        return min * (1 - interp) + max * interp;
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
}
