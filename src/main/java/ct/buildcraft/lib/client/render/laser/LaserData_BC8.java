/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.render.laser;

import java.util.Arrays;
import java.util.Objects;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import net.minecraft.world.phys.Vec3;

/** Holds information on what a single laser in the world: its {@link LaserType}, is position, its size, and some other
 * misc rendering info. */
public class LaserData_BC8 {
	
	private static int id = 0;
	private static int lenth = 16;
	
	private static LaserData_BC8[] objCache = new LaserData_BC8[lenth];
	private static int[] hashCache = new int[lenth];

	
    public final LaserType laserType;
    public final Vec3 start, end;
    public final double scale;
    public final boolean enableDiffuse, doubleFace;
    public final int minBlockLight;
	public final boolean isOrgin;
    private final int hash;

    public LaserData_BC8(LaserType laserType, Vec3 start, Vec3 end, double scale) {
        this(laserType, start, end, scale, true, false, 0, false);
    }
    
    public LaserData_BC8(LaserType laserType, Vec3 start, Vec3 end, double scale, boolean isOrgin) {
        this(laserType, start, end, scale, true, false, 0, isOrgin);
    }

    public LaserData_BC8(LaserType laserType, Vec3 start, Vec3 end, double scale, boolean enableDiffuse, boolean doubleFace, int minBlockLight, boolean isOrgin) {
        this(laserType, start, end, scale, enableDiffuse, doubleFace, minBlockLight, isOrgin, Objects.hash(laserType, start, end, Double.doubleToLongBits(scale), enableDiffuse, doubleFace, minBlockLight, isOrgin));
    }
    
    protected LaserData_BC8(LaserType laserType, Vec3 start, Vec3 end, double scale, boolean enableDiffuse, boolean doubleFace, int minBlockLight, boolean isOrgin, int hash) {
    	this.laserType = laserType;
        this.start = start;
        this.end = end;
        this.scale = scale;
        this.enableDiffuse = enableDiffuse;
        this.doubleFace = doubleFace;
        this.minBlockLight = minBlockLight;
        this.isOrgin = isOrgin;
        this.hash = hash;
        
    }
    
    //Cache
    public static LaserData_BC8 of(LaserType laserType, Vec3 start, Vec3 end, double scale, boolean enableDiffuse, boolean doubleFace, int minBlockLight, int id) {
    	int hash = Objects.hash(laserType, start, end, Double.doubleToLongBits(scale), enableDiffuse, doubleFace, minBlockLight);
    	if(hash != hashCache[id]) {
    		objCache[id] = new LaserData_BC8(laserType, start, end, scale, enableDiffuse, doubleFace, minBlockLight, false, id);
    		hashCache[id] = hash;
    	}
    	return objCache[id];
    }
    
    public static int AllocateId(int amount) {
    	int k = id;
    	id+=amount;
    	if(id>lenth){
    		lenth +=16;
    		objCache = Arrays.copyOf(objCache, lenth);
    		hashCache = Arrays.copyOf(hashCache, lenth);
    	}
    	return k;
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

        public LaserType(LaserType from, SpriteHolder markerDefaultPossible) {
            this.capStart = new LaserRow(from.capStart, markerDefaultPossible);
            this.capEnd = new LaserRow(from.capEnd, markerDefaultPossible);
            this.start = new LaserRow(from.start, markerDefaultPossible);
            this.end = new LaserRow(from.end, markerDefaultPossible);
            this.variations = new LaserRow[from.variations.length];
            for (int i = 0; i < variations.length; i++) {
                this.variations[i] = new LaserRow(from.variations[i], markerDefaultPossible);
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
