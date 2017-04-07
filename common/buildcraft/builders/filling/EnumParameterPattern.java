package buildcraft.builders.filling;

public enum EnumParameterPattern implements IParameter {
    FRAME,
    SQUARE,
    SPHERE,
    CIRCLE;
    // TODO: TILT
    // TODO?: FLATTEN

    @Override
    public String getParameterName() {
        return "pattern";
    }
}
