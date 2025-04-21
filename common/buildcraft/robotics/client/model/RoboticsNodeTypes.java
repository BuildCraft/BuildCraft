package buildcraft.robotics.client.model;

import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.robotics.plug.PluggableRobotStation;

public class RoboticsNodeTypes {
    public static final NodeType<PluggableRobotStation.EnumRobotStationState> ENUM_ROBOT_STATION_STATE;

    static {

        ENUM_ROBOT_STATION_STATE = new NodeType<>("RobotStationState", PluggableRobotStation.EnumRobotStationState.None);
        NodeTypes.addType("RobotStationState", ENUM_ROBOT_STATION_STATE);
        ENUM_ROBOT_STATION_STATE.put_t_o("(string)", String.class, PluggableRobotStation.EnumRobotStationState::getSerializedName);
        for (PluggableRobotStation.EnumRobotStationState state : PluggableRobotStation.EnumRobotStationState.values()) {
            ENUM_ROBOT_STATION_STATE.putConstant(state.getSerializedName(), state);
        }
    }

    public static synchronized void setup() {
        // Just to call the above static initializer
    }
}
