/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import buildcraft.api.statements.StatementManager;
import buildcraft.builders.snapshot.pattern.*;
import buildcraft.builders.snapshot.pattern.parameter.*;

public class BCBuildersStatements {
    public static final PatternNone PATTERN_NONE = new PatternNone();
    public static final PatternClear PATTERN_CLEAR = new PatternClear();
    public static final PatternFill PATTERN_FILL = new PatternFill();
    public static final PatternBox PATTERN_BOX = new PatternBox();
    public static final PatternFrame PATTERN_FRAME = new PatternFrame();
    // public static final PatternHorizon PATTERN_HORIZON = new PatternHorizon(); // broken ATM
    public static final PatternPyramid PATTERN_PYRAMID = new PatternPyramid();
    public static final PatternStairs PATTERN_STAIRS = new PatternStairs();
    // public static final PatternFlatten PATTERN_FLATTEN = new PatternFlatten(); // broken ATM
    public static final PatternShape2dTriangle PATTERN_TRIANGLE = new PatternShape2dTriangle();
    public static final PatternShape2dSquare PATTERN_SQUARE = new PatternShape2dSquare();
    public static final PatternShape2dPentagon PATTERN_PENTAGON = new PatternShape2dPentagon();
    public static final PatternShape2dHexagon PATTERN_HEXAGON = new PatternShape2dHexagon();
    public static final PatternShape2dOctagon PATTERN_OCTAGON = new PatternShape2dOctagon();
    public static final PatternShape2dArc PATTERN_ARC = new PatternShape2dArc();
    public static final PatternShape2dSemiCircle PATTERN_SEMI_CIRCLE = new PatternShape2dSemiCircle();
    public static final PatternShape2dCircle PATTERN_CIRCLE = new PatternShape2dCircle();
    public static final PatternSphere PATTERN_SPHERE = new PatternSphere();
    public static final PatternSpherePart PATTERN_HEMI_SPHERE = new PatternSpherePart(PatternSpherePart.SpherePartType.HALF);
    public static final PatternSpherePart PATTERN_QUARTER_SPHERE = new PatternSpherePart(PatternSpherePart.SpherePartType.QUARTER);
    public static final PatternSpherePart PATTERN_EIGHTH_SPHERE = new PatternSpherePart(PatternSpherePart.SpherePartType.EIGHTH);

    public static final Pattern[] PATTERNS = { //
            PATTERN_NONE, PATTERN_CLEAR, PATTERN_FILL, PATTERN_BOX, PATTERN_FRAME, //
            /* PATTERN_HORIZON, PATTERN_FLATTEN, */ PATTERN_PYRAMID, PATTERN_STAIRS, //
            PATTERN_TRIANGLE, PATTERN_SQUARE, PATTERN_PENTAGON, PATTERN_HEXAGON, //
            PATTERN_OCTAGON, PATTERN_ARC, PATTERN_SEMI_CIRCLE, PATTERN_CIRCLE, //
            PATTERN_SPHERE, PATTERN_HEMI_SPHERE, PATTERN_QUARTER_SPHERE, //
            PATTERN_EIGHTH_SPHERE //
    };

    static {
        StatementManager.registerParameter(PatternParameterXZDir::readFromNbt);
        StatementManager.registerParameter(PatternParameterRotation::readFromNbt);
        StatementManager.registerParameter(PatternParameterFacing::readFromNbt);
        StatementManager.registerParameter(PatternParameterYDir::readFromNbt);
        StatementManager.registerParameter(PatternParameterCenter::readFromNbt);
        StatementManager.registerParameter(PatternParameterHollow::readFromNbt);
        StatementManager.registerParameter(PatternParameterAxis::readFromNbt);
    }

    public static void preInit() {
        StatementManager.registerActionProvider(BCBuildersActionProvider.INSTANCE);
    }
}
