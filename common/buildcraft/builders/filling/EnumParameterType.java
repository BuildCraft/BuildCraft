package buildcraft.builders.filling;

public enum EnumParameterType implements IParameter {
    EMPTY,
    FILLED;

    @Override
    public String getParameterName() {
        return "type";
    }
}
