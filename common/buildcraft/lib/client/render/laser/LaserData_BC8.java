/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.laser;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Objects;

/** Holds information on what a single laser in the world: its {@link LaserType}, is position, its size, and some other
 * misc rendering info. */
public class LaserData_BC8 {
    public final LaserType laserType;
    public final Vector3d startWorldPos, endWorldPos;
    public final double scale;
    public final boolean enableDiffuse, doubleFace;
    public final int minBlockLight;
    private final int hash;

    public LaserData_BC8(LaserType laserType, Vector3d startWorldPos, Vector3d endWorldPos, double scale) {
        this(laserType, startWorldPos, endWorldPos, scale, true, false, 0);
    }

    public LaserData_BC8(LaserType laserType, Vector3d startWorldPos, Vector3d endWorldPos, double scale, boolean enableDiffuse, boolean doubleFace, int minBlockLight) {
        this.laserType = laserType;
        this.startWorldPos = startWorldPos;
        this.endWorldPos = endWorldPos;
        this.scale = scale;
        this.enableDiffuse = enableDiffuse;
        this.doubleFace = doubleFace;
        this.minBlockLight = minBlockLight;
        hash = Objects.hash(laserType, this.startWorldPos, this.endWorldPos, Double.doubleToLongBits(scale), enableDiffuse, doubleFace, minBlockLight);
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
        if (!startWorldPos.equals(other.startWorldPos)) return false;
        if (!endWorldPos.equals(other.endWorldPos)) return false;
        if (Double.compare(scale, other.scale) != 0) return false;
        if (enableDiffuse != other.enableDiffuse) return false;
        if (doubleFace != other.doubleFace) return false;
        if (minBlockLight != other.minBlockLight) return false;
        return true;
    }

    /** Holds information about a specific type of laser: what textures should be used for different parts. */
    public static class LaserType {
        /** The caps of the laser. These will never be shrunk or */
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
        public final ISprite sprite;
        public final double uMin, vMin, uMax, vMax;
        public final int width, height;
        public final LaserSide[] validSides;

        public LaserRow(ISprite sprite, int uMin, int vMin, int uMax, int vMax, int textureSize, LaserSide... sides) {
            this.sprite = sprite;
            this.uMin = uMin / (double) textureSize;
            this.vMin = vMin / (double) textureSize;
            this.uMax = uMax / (double) textureSize;
            this.vMax = vMax / (double) textureSize;
            this.width = uMax - uMin;
            this.height = vMax - vMin;
            if (sides == null || sides.length == 0) {
                validSides = LaserSide.VALUES;
            } else {
                validSides = sides;
            }
        }

        public LaserRow(ISprite sprite, int uMin, int vMin, int uMax, int vMax, LaserSide... sides) {
            this(sprite, uMin, vMin, uMax, vMax, 16, sides);
        }

        public LaserRow(LaserRow from, ISprite sprite) {
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
