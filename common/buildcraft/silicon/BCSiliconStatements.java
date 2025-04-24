package buildcraft.silicon;

import buildcraft.silicon.statement.ActionPowerPulsar;
import buildcraft.silicon.statement.TriggerLightSensor;
import buildcraft.silicon.statement.TriggerTimer;

public class BCSiliconStatements {

    public static final TriggerLightSensor TRIGGER_LIGHT_LOW;
    public static final TriggerLightSensor TRIGGER_LIGHT_HIGH;
    public static final TriggerLightSensor[] TRIGGER_LIGHT;

    public static final TriggerTimer TRIGGER_TIMER_SHORT;
    public static final TriggerTimer TRIGGER_TIMER_MEDIUM;
    public static final TriggerTimer TRIGGER_TIMER_LONG;
    public static final TriggerTimer[] TRIGGER_TIMER;

    public static final ActionPowerPulsar ACTION_PULSAR_CONSTANT;
    public static final ActionPowerPulsar ACTION_PULSAR_SINGLE;
    public static final ActionPowerPulsar[] ACTION_PULSAR;

    static {
        TRIGGER_LIGHT_LOW = new TriggerLightSensor(false);
        TRIGGER_LIGHT_HIGH = new TriggerLightSensor(true);
        TRIGGER_LIGHT = new TriggerLightSensor[] { TRIGGER_LIGHT_LOW, TRIGGER_LIGHT_HIGH };

        TRIGGER_TIMER_SHORT = new TriggerTimer(TriggerTimer.Duration.SHORT);
        TRIGGER_TIMER_MEDIUM = new TriggerTimer(TriggerTimer.Duration.MEDIUM);
        TRIGGER_TIMER_LONG = new TriggerTimer(TriggerTimer.Duration.LONG);
        TRIGGER_TIMER = new TriggerTimer[] { TRIGGER_TIMER_SHORT, TRIGGER_TIMER_MEDIUM, TRIGGER_TIMER_LONG };

        ACTION_PULSAR_CONSTANT = new ActionPowerPulsar(true);
        ACTION_PULSAR_SINGLE = new ActionPowerPulsar(false);
        ACTION_PULSAR = new ActionPowerPulsar[] { ACTION_PULSAR_CONSTANT, ACTION_PULSAR_SINGLE };
    }

    public static void preInit() {
        // NO-OP: just to call the above static block
    }
}
