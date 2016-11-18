package buildcraft.transport;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.StatementManager;

import buildcraft.transport.statements.ActionPowerPulsar;
import buildcraft.transport.statements.TransportActionProvider;
import buildcraft.transport.statements.TransportTriggerProvider;

public class BCTransportStatements {

    public static final ActionPowerPulsar ACTION_PULSAR_CONSTANT;
    public static final ActionPowerPulsar ACTION_PULSAR_SINGLE;

    public static final IStatement[] ACTION_PULSAR;

    static {
        ACTION_PULSAR_CONSTANT = new ActionPowerPulsar(true);
        ACTION_PULSAR_SINGLE = new ActionPowerPulsar(false);
        ACTION_PULSAR = new IStatement[] { ACTION_PULSAR_CONSTANT, ACTION_PULSAR_SINGLE };
    }

    public static void preInit() {
        StatementManager.registerTriggerProvider(TransportTriggerProvider.INSTANCE);
        StatementManager.registerActionProvider(TransportActionProvider.INSTANCE);
    }
}
