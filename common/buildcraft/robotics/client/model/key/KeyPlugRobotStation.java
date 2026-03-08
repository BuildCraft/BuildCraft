package buildcraft.robotics.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.robotics.plug.PluggableRobotStation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;

import java.util.Objects;

public class KeyPlugRobotStation extends PluggableModelKey {
    public final PluggableRobotStation.EnumRobotStationState stationState;
    private final int hash;

    public KeyPlugRobotStation(Direction side, PluggableRobotStation.EnumRobotStationState stationState) {
        super(RenderType.cutout(), side);
        this.stationState = stationState;
        this.hash = Objects.hash(layer, side, stationState);
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
        KeyPlugRobotStation other = (KeyPlugRobotStation) obj;
        return other.stationState == stationState//
                && other.layer == layer//\
                && other.side == side;
    }
}
