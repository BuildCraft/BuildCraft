package ct.buildcraft.robotics.client.model.key;

import ct.buildcraft.api.transport.pluggable.PluggableModelKey;
import ct.buildcraft.robotics.plug.RobotStationPluggable.RobotStationState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;

public final class KeyRobotStation extends PluggableModelKey {
    public final RobotStationState state;
    private final int hash;

    public KeyRobotStation(Direction side, RobotStationState state) {
        super(RenderType.cutout(), side);
        this.state = state == null ? RobotStationState.None : state;
        this.hash = 31 * super.hashCode() + this.state.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof KeyRobotStation other && state == other.state;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
