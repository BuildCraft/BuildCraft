package buildcraft.transport;

import net.minecraft.item.EnumDyeColor;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.StatementManager;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.statements.ActionPipeColor;
import buildcraft.transport.statements.ActionPowerPulsar;
import buildcraft.transport.statements.TransportActionProvider;
import buildcraft.transport.statements.TransportTriggerProvider;

public class BCTransportStatements {

    public static final ActionPowerPulsar ACTION_PULSAR_CONSTANT;
    public static final ActionPowerPulsar ACTION_PULSAR_SINGLE;

    public static final ActionPipeColor[] ACTION_PIPE_COLOUR;

    public static final IStatement[] ACTION_PULSAR;

    static {
        ACTION_PULSAR_CONSTANT = new ActionPowerPulsar(true);
        ACTION_PULSAR_SINGLE = new ActionPowerPulsar(false);
        ACTION_PULSAR = new IStatement[] { ACTION_PULSAR_CONSTANT, ACTION_PULSAR_SINGLE };

        ACTION_PIPE_COLOUR = new ActionPipeColor[ColourUtil.COLOURS.length];
        for (EnumDyeColor colour : ColourUtil.COLOURS) {
            ACTION_PIPE_COLOUR[colour.ordinal()] = new ActionPipeColor(colour);
        }
    }

    public static void preInit() {
        StatementManager.registerTriggerProvider(TransportTriggerProvider.INSTANCE);
        StatementManager.registerActionProvider(TransportActionProvider.INSTANCE);
    }
}
