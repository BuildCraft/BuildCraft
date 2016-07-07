package buildcraft.lib.client.render.laser;

import java.util.Objects;

import net.minecraft.util.math.Vec3d;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

/** Holds information on what a laser is, its width, length, texture, etc. */
public class LaserData_BC8 {
    public final LaserType laserType;
    public final Vec3d start, end;
    public final double scale;
    private final int hash;

    public LaserData_BC8(LaserType laserType, Vec3d start, Vec3d end, double scale) {
        this.laserType = laserType;
        this.start = start;
        this.end = end;
        this.scale = scale;
        hash = Objects.hash(laserType, start, end, Double.doubleToLongBits(scale));
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        LaserData_BC8 other = (LaserData_BC8) obj;
        if (laserType != other.laserType) return false;
        if (!start.equals(other.start)) return false;
        if (!end.equals(other.end)) return false;
        if (Double.compare(scale, other.scale) != 0) return false;
        return true;
    }

    public static class LaserType {
        public final LaserRow capStart, capEnd;
        public final LaserRow start, end;
        public final LaserRow[] variations;

        public LaserType(LaserRow capStart, LaserRow start, LaserRow[] middle, LaserRow end, LaserRow capEnd) {
            this.capStart = capStart;
            this.start = start;
            this.variations = middle;
            this.end = end;
            this.capEnd = capEnd;
        }

        public LaserType(LaserType from, SpriteHolder replacementSprite) {
            this.capStart = new LaserRow(from.capStart, replacementSprite);
            this.capEnd = new LaserRow(from.capEnd, replacementSprite);
            this.start = new LaserRow(from.start, replacementSprite);
            this.end = new LaserRow(from.end, replacementSprite);
            this.variations = new LaserRow[from.variations.length];
            for (int i = 0; i < variations.length; i++) {
                this.variations[i] = new LaserRow(from.variations[i], replacementSprite);
            }
        }
    }

    public static class LaserRow {
        public final SpriteHolder sprite;
        public final int uMin, vMin, uMax, vMax;
        public final int width, height;
        public final LaserSide[] validSides;

        public LaserRow(SpriteHolder sprite, int uMin, int vMin, int uMax, int vMax, LaserSide... sides) {
            this.sprite = sprite;
            this.uMin = uMin;
            this.vMin = vMin;
            this.uMax = uMax;
            this.vMax = vMax;
            this.width = uMax - uMin;
            this.height = vMax - vMin;
            if (sides == null || sides.length == 0) {
                validSides = LaserSide.VALUES;
            } else {
                validSides = sides;
            }
        }

        public LaserRow(LaserRow from, SpriteHolder sprite) {
            this.sprite = sprite;
            this.uMin = from.uMin;
            this.vMin = from.vMin;
            this.uMax = from.uMax;
            this.vMax = from.vMax;
            this.width = from.width;
            this.height = from.height;
            this.validSides = from.validSides;
        }
    }

    public enum LaserSide {
        TOP,
        BOTTOM,
        /** +Z */
        LEFT,
        /** -Z */
        RIGHT;

        public static final LaserSide[] VALUES = values();
    }
}
