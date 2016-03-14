package buildcraft.core.lib.client.model;

import java.util.EnumMap;

import com.google.common.collect.Maps;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import buildcraft.core.lib.utils.Utils;

public enum FacingRotationHelper {
    DOWN(EnumFacing.DOWN),
    UP(EnumFacing.UP),
    NORTH(EnumFacing.NORTH),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.WEST),
    EAST(EnumFacing.EAST);

    private static final EnumMap<EnumFacing, FacingRotationHelper> helperForFaceMap = Maps.newEnumMap(EnumFacing.class);

    static {
        for (FacingRotationHelper helper : values()) {
            helperForFaceMap.put(helper.face, helper);
        }
    }

    public static FacingRotationHelper helperForFace(EnumFacing face) {
        return helperForFaceMap.get(face);
    }

    public static EnumFacing rotateFace(EnumFacing model, EnumFacing to, EnumFacing face) {
        return helperForFace(model).rotateFace(to, face);
    }

    public final EnumFacing face;
    private final EnumMap<EnumFacing, EnumMap<EnumFacing, EnumFacing>> facingMap;

    FacingRotationHelper(EnumFacing face) {
        this.face = face;
        facingMap = Maps.newEnumMap(EnumFacing.class);
        for (EnumFacing key : EnumFacing.values()) {
            EnumMap<EnumFacing, EnumFacing> map = Maps.newEnumMap(EnumFacing.class);
            facingMap.put(key, map);
            Axis faceAxis = face.getAxis();
            Axis keyAxis = key.getAxis();
            Axis rotationAxis = Utils.other(faceAxis, keyAxis);
            int numRotations;
            if (face == key) {
                numRotations = 0;
            } else if (face.getOpposite() == key) {
                numRotations = 2;
            } else {
                EnumFacing rotated = key.rotateAround(rotationAxis);
                if (rotated == face) {
                    numRotations = 1;
                } else if (rotated == face.getOpposite()) {
                    numRotations = 3;
                } else {
                    throw new IllegalStateException("Dammit AlexIIL! You made a mistake (face = " + face + ", key = " + key + ", rotationAxis = "
                        + rotationAxis + ", rotated = " + rotated + ")");
                }
            }
            for (EnumFacing val : EnumFacing.values()) {
                EnumFacing rotated = val;
                if (val.getAxis() != rotationAxis) {
                    for (int i = 0; i < numRotations; i++) {
                        rotated = rotated.rotateAround(rotationAxis);
                    }
                }
                map.put(val, rotated);
            }
        }
    }

    public EnumFacing rotateFace(EnumFacing to, EnumFacing face) {
        return facingMap.get(to).get(face);
    }
}
