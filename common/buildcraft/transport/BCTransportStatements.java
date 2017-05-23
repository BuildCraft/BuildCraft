/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;

import buildcraft.api.statements.StatementManager;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;
import buildcraft.transport.statements.*;

public class BCTransportStatements {

    public static final TriggerLightSensor TRIGGER_LIGHT_LOW;
    public static final TriggerLightSensor TRIGGER_LIGHT_HIGH;
    public static final TriggerLightSensor[] TRIGGER_LIGHT;

    public static final ActionPowerPulsar ACTION_PULSAR_CONSTANT;
    public static final ActionPowerPulsar ACTION_PULSAR_SINGLE;
    public static final ActionPowerPulsar[] ACTION_PULSAR;

    public static final TriggerPipeSignal[] TRIGGER_PIPE_SIGNAL;
    public static final ActionPipeSignal[] ACTION_PIPE_SIGNAL;
    public static final ActionPipeColor[] ACTION_PIPE_COLOUR;
    public static final ActionExtractionPreset[] ACTION_EXTRACTION_PRESET;
    public static final ActionPipeDirection[] ACTION_PIPE_DIRECTION;

    static {
        TRIGGER_LIGHT_LOW = new TriggerLightSensor(false);
        TRIGGER_LIGHT_HIGH = new TriggerLightSensor(true);
        TRIGGER_LIGHT = new TriggerLightSensor[] { TRIGGER_LIGHT_LOW, TRIGGER_LIGHT_HIGH };

        ACTION_PULSAR_CONSTANT = new ActionPowerPulsar(true);
        ACTION_PULSAR_SINGLE = new ActionPowerPulsar(false);
        ACTION_PULSAR = new ActionPowerPulsar[] { ACTION_PULSAR_CONSTANT, ACTION_PULSAR_SINGLE };

        TRIGGER_PIPE_SIGNAL = new TriggerPipeSignal[2 * ColourUtil.COLOURS.length];
        for (EnumDyeColor colour : ColourUtil.COLOURS) {
            TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 0] = new TriggerPipeSignal(true, colour);
            TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 1] = new TriggerPipeSignal(false, colour);
        }

        ACTION_PIPE_SIGNAL = new ActionPipeSignal[ColourUtil.COLOURS.length];
        for (EnumDyeColor colour : ColourUtil.COLOURS) {
            ACTION_PIPE_SIGNAL[colour.ordinal()] = new ActionPipeSignal(colour);
        }

        ACTION_PIPE_COLOUR = new ActionPipeColor[ColourUtil.COLOURS.length];
        for (EnumDyeColor colour : ColourUtil.COLOURS) {
            ACTION_PIPE_COLOUR[colour.ordinal()] = new ActionPipeColor(colour);
        }

        ACTION_EXTRACTION_PRESET = new ActionExtractionPreset[SlotIndex.VALUES.length];
        for (SlotIndex index : SlotIndex.VALUES) {
            ACTION_EXTRACTION_PRESET[index.ordinal()] = new ActionExtractionPreset(index);
        }

        ACTION_PIPE_DIRECTION = new ActionPipeDirection[EnumFacing.VALUES.length];
        for (EnumFacing face : EnumFacing.VALUES) {
            ACTION_PIPE_DIRECTION[face.ordinal()] = new ActionPipeDirection(face);
        }

        StatementManager.registerParameterClass(TriggerParameterSignal.class);
        StatementManager.registerParameterClass(ActionParameterSignal.class);
    }

    public static void preInit() {
        StatementManager.registerTriggerProvider(TransportTriggerProvider.INSTANCE);
        StatementManager.registerActionProvider(TransportActionProvider.INSTANCE);
    }
}
