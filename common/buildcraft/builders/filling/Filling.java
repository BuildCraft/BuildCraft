package buildcraft.builders.filling;

import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Function;

public enum Filling {
    INSTANCE;

    public Class<? extends IParameter> getNextParameterClass(List<IParameter> parameters) {
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

    public boolean[][][] generateFillingPlanByFunction(BlockPos size, Function<BlockPos, Boolean> function) {
        boolean[][][] fillingPlan = new boolean[size.getX()][size.getY()][size.getZ()];
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    fillingPlan[x][y][z] = function.apply(new BlockPos(x, y, z));
                }
            }
        }
        return fillingPlan;
    }

    public boolean[][][] getFillingPlan(BlockPos size, List<IParameter> parameters) {
        EnumParameterPattern parameterPattern = (EnumParameterPattern) parameters.get(0);
        BlockPos sizeS = size.add(-1, -1, -1);
        if (parameterPattern == EnumParameterPattern.FRAME) {
            EnumParameterType parameterType = (EnumParameterType) parameters.get(1);
            return generateFillingPlanByFunction(size, pos ->
                    parameterType == EnumParameterType.FILLED ?
                            pos.getX() == 0 || pos.getX() == sizeS.getX() ||
                                    pos.getY() == 0 || pos.getY() == sizeS.getY() ||
                                    pos.getZ() == 0 || pos.getZ() == sizeS.getZ() :
                            ((pos.getX() == 0 || pos.getX() == sizeS.getX()) && (pos.getY() == 0 || pos.getY() == sizeS.getY())) ||
                                    ((pos.getY() == 0 || pos.getY() == sizeS.getY()) && (pos.getZ() == 0 || pos.getZ() == sizeS.getZ())) ||
                                    ((pos.getZ() == 0 || pos.getZ() == sizeS.getZ()) && (pos.getX() == 0 || pos.getX() == sizeS.getX()))
            );
        }
        return generateFillingPlanByFunction(size, pos -> false);
    }
}
