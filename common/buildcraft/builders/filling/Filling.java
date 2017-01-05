package buildcraft.builders.filling;

import java.util.List;

public enum Filling {
    INSTANCE;

    public Class<?extends IParameter> getNextParameterClass(List<IParameter> parameters) {
        if (parameters.size() == 0) {
            return EnumParameterPattern.class;
        }
        EnumParameterPattern parameterPattern = (EnumParameterPattern) parameters.get(0);
        if (parameterPattern == EnumParameterPattern.FRAME) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
            }
        }
        if (parameterPattern == EnumParameterPattern.SQUARE) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
            }
            if (parameters.size() == 2) {
                EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
                if (parameterType == EnumParameterType.EMPTY) {
                    return EnumParameterAxis.class;
                }
            }
        }
        if (parameterPattern == EnumParameterPattern.SPHERE) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
            }
        }
        if (parameterPattern == EnumParameterPattern.CIRCLE) {
            if (parameters.size() == 1) {
                return EnumParameterType.class;
            }
            if (parameters.size() == 2) {
                return EnumParameterAxis.class;
            }
        }
        return null;
    }
}
