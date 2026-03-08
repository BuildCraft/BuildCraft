/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import buildcraft.builders.snapshot.pattern.PatternSpherePart;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterCenter;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import net.minecraft.util.Direction;

import java.util.EnumMap;
import java.util.Map;

public class BCBuildersSprites {
    public static final SpriteHolder FILLER_PLANNER;
    public static final SpriteHolder ROBOT;

    public static final SpriteHolder FILLER_NONE;
    public static final SpriteHolder FILLER_CLEAR;
    public static final SpriteHolder FILLER_FILL;
    public static final SpriteHolder FILLER_BOX;
    public static final SpriteHolder FILLER_FRAME;
    public static final SpriteHolder FILLER_FLATTEN;
    public static final SpriteHolder FILLER_HORIZON;
    public static final SpriteHolder FILLER_CYLINDER;
    public static final SpriteHolder FILLER_PYRAMID;
    public static final SpriteHolder FILLER_STAIRS;
    public static final SpriteHolder FILLER_SPHERE;
    public static final SpriteHolder FILLER_2D_TRIANGLE;
    public static final SpriteHolder FILLER_2D_SQUARE;
    public static final SpriteHolder FILLER_2D_PENTAGON;
    public static final SpriteHolder FILLER_2D_HEXAGON;
    public static final SpriteHolder FILLER_2D_OCTAGON;
    public static final SpriteHolder FILLER_2D_CIRCLE;
    public static final SpriteHolder FILLER_2D_SEMI_CIRCLE;
    public static final SpriteHolder FILLER_2D_ARC;

    public static final SpriteHolder PARAM_HOLLOW;
    public static final SpriteHolder PARAM_FILLED_INNER;
    public static final SpriteHolder PARAM_FILLED_OUTER;

    public static final SpriteHolder PARAM_STAIRS_DOWN;
    public static final SpriteHolder PARAM_STAIRS_UP;

    public static final SpriteHolder[] PARAM_ROTATION;

    public static final Map<Direction, SpriteHolder> PARAM_XZ_DIR;
    public static final Map<PatternParameterCenter, SpriteHolder> PARAM_CENTER;
    public static final Map<Direction.Axis, SpriteHolder> PARAM_AXIS;
    public static final Map<Direction, SpriteHolder> PARAM_FACE;
    public static final Map<PatternSpherePart.SpherePartType, SpriteHolder> FILLER_SPHERE_PART;
    // Calen
    public static final SpriteHolder ARCHITECT_SCAN;

    static {
        FILLER_PLANNER = getHolder("addons/filler_planner");
        ROBOT = getHolder("robot");

        FILLER_NONE = getHolder("filler/patterns/none");
        FILLER_CLEAR = getHolder("filler/patterns/clear");
        FILLER_FILL = getHolder("filler/patterns/fill");
        FILLER_BOX = getHolder("filler/patterns/box");
        FILLER_FRAME = getHolder("filler/patterns/frame");
        FILLER_FLATTEN = getHolder("filler/patterns/flatten");
        FILLER_HORIZON = getHolder("filler/patterns/horizon");
        FILLER_CYLINDER = getHolder("filler/patterns/cylinder");
        FILLER_PYRAMID = getHolder("filler/patterns/pyramid");
        FILLER_STAIRS = getHolder("filler/patterns/stairs");
        FILLER_SPHERE = getHolder("filler/patterns/sphere");
        FILLER_2D_TRIANGLE = getHolder("filler/patterns/2d_triangle");
        FILLER_2D_SQUARE = getHolder("filler/patterns/2d_square");
        FILLER_2D_PENTAGON = getHolder("filler/patterns/2d_pentagon");
        FILLER_2D_HEXAGON = getHolder("filler/patterns/2d_hexagon");
        FILLER_2D_OCTAGON = getHolder("filler/patterns/2d_octagon");
        FILLER_2D_CIRCLE = getHolder("filler/patterns/2d_circle");
        FILLER_2D_SEMI_CIRCLE = getHolder("filler/patterns/2d_semi_circle");
        FILLER_2D_ARC = getHolder("filler/patterns/2d_arc");

        PARAM_HOLLOW = getHolder("filler/parameters/hollow");
        PARAM_FILLED_INNER = getHolder("filler/parameters/filled_inner");
        PARAM_FILLED_OUTER = getHolder("filler/parameters/filled_outer");

        PARAM_STAIRS_UP = getHolder("filler/parameters/stairs_ascend");
        PARAM_STAIRS_DOWN = getHolder("filler/parameters/stairs_descend");

        PARAM_ROTATION = new SpriteHolder[4];
        for (int r = 0; r < 4; r++) {
            PARAM_ROTATION[r] = getHolder("filler/parameters/rotation_" + r);
        }

        PARAM_XZ_DIR = new EnumMap<>(Direction.class);
        PARAM_XZ_DIR.put(Direction.WEST, getHolder("filler/parameters/arrow_left"));
        PARAM_XZ_DIR.put(Direction.EAST, getHolder("filler/parameters/arrow_right"));
        PARAM_XZ_DIR.put(Direction.NORTH, getHolder("filler/parameters/arrow_up"));
        PARAM_XZ_DIR.put(Direction.SOUTH, getHolder("filler/parameters/arrow_down"));

        PARAM_CENTER = new EnumMap<>(PatternParameterCenter.class);
        for (PatternParameterCenter param : PatternParameterCenter.values()) {
            PARAM_CENTER.put(param, getHolder("filler/parameters/center_" + param.ordinal()));
        }

        PARAM_AXIS = new EnumMap<>(Direction.Axis.class);
        for (Direction.Axis axis : Direction.Axis.values()) {
            PARAM_AXIS.put(axis, getHolder("filler/parameters/axis_" + axis.getName()));
        }

        PARAM_FACE = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            PARAM_FACE.put(face, getHolder("filler/parameters/face_" + face.getName()));
        }

        FILLER_SPHERE_PART = new EnumMap<>(PatternSpherePart.SpherePartType.class);
        for (PatternSpherePart.SpherePartType type : PatternSpherePart.SpherePartType.values()) {
            FILLER_SPHERE_PART.put(type, getHolder("filler/patterns/sphere_" + type.lowerCaseName));
        }

        // Calen
        ARCHITECT_SCAN = getHolder("blocks/scan");
    }

    private static SpriteHolder getHolder(String suffix) {
        return SpriteHolderRegistry.getHolder("buildcraftbuilders:" + suffix);
    }

    public static void fmlPreInit() {
        // Nothing, just to register the sprites
    }
}
