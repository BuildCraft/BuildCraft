package buildcraft.robotics;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.core.statements.StatementParameterItemStackExact;
import buildcraft.robotics.statements.*;

public class BCRoboticsStatements {
    public static ITriggerInternal triggerRobotSleep = new TriggerRobotSleep();
    public static ITriggerInternal triggerRobotInStation = new TriggerRobotInStation();
    public static ITriggerInternal triggerRobotLinked = new TriggerRobotLinked(false);
    public static ITriggerInternal triggerRobotReserved = new TriggerRobotLinked(true);

    public static IActionInternal actionRobotGotoStation = new ActionRobotGotoStation();
    public static IActionInternal actionRobotWakeUp = new ActionRobotWakeUp();
    public static IActionInternal actionRobotWorkInArea = new ActionRobotWorkInArea(ActionRobotWorkInArea.AreaType.WORK);
    public static IActionInternal actionRobotLoadUnloadArea = new ActionRobotWorkInArea(ActionRobotWorkInArea.AreaType.LOAD_UNLOAD);
    public static IActionInternal actionRobotFilter = new ActionRobotFilter();
    public static IActionInternal actionRobotFilterTool = new ActionRobotFilterTool();
    public static IActionInternal actionStationRequestItems = new ActionStationRequestItems();
    public static IActionInternal actionStationProvideItems = new ActionStationProvideItems();
    public static IActionInternal actionStationAcceptFluids = new ActionStationAcceptFluids();
    public static IActionInternal actionStationProvideFluids = new ActionStationProvideFluids();
    public static IActionInternal actionStationForceRobot = new ActionStationForbidRobot(true);
    public static IActionInternal actionStationForbidRobot = new ActionStationForbidRobot(false);
    public static IActionInternal actionStationAcceptItems = new ActionStationAcceptItems();
    public static IActionInternal actionStationMachineRequestItems = new ActionStationRequestItemsMachine();

    static {
        StatementManager.registerParameter(StatementParameterRobot::new);
        StatementManager.registerParameter(StatementParameterMapLocation::new);
        StatementManager.registerParameter(StatementParameterItemStackExact::readFromNbt);
    }

    public static void preInit() {
        // NO-OP: just to call the above static block
    }
}
