package buildcraft.robotics.render;

import java.util.Arrays;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.robotics.RobotStationPluggable.EnumRobotStationState;

public class ModelKeyStation extends PluggableModelKey<ModelKeyStation> {
    public final EnumRobotStationState state;
    private final int hash;

    public ModelKeyStation(EnumFacing side, EnumRobotStationState state) {
        super(EnumWorldBlockLayer.CUTOUT, RobotStationModel.INSTANCE, side);
        this.state = state;
        this.hash = Arrays.hashCode(new int[] { super.hashCode(), state.hashCode() });
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        ModelKeyStation other = (ModelKeyStation) obj;
        if (state != other.state) return false;
        return true;
    }
}
