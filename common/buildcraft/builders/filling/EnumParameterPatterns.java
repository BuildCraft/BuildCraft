package buildcraft.builders.filling;

public enum EnumParameterPatterns implements IParameter {
    FRAME,
    SQUARE,
    SPHERE,
    CIRCLE;
    // TODO: TILT
    // TODO?: FLATTEN

    @Override
    public String getParameterName() {
        return "axis";
    }
}
