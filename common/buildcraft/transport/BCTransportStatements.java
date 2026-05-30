/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.api.statements.IActionProvider;

import buildcraft.transport.statements.ActionExtractionPreset;
import buildcraft.transport.statements.ActionParameterSignal;
import buildcraft.transport.statements.ActionPipeColor;
import buildcraft.transport.statements.ActionPipeDirection;
import buildcraft.transport.statements.ActionPipeSignal;
import buildcraft.transport.statements.ActionPowerLimit;
import buildcraft.transport.statements.ActionProviderPipes;
import buildcraft.transport.statements.TriggerFluidsTraversing;
import buildcraft.transport.statements.TriggerItemsTraversing;
import buildcraft.transport.statements.TriggerParameterSignal;
import buildcraft.transport.statements.TriggerPipeSignal;
import buildcraft.transport.statements.TriggerPowerRequested;
import buildcraft.transport.statements.TriggerProviderPipes;

import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;
import buildcraft.transport.pipe.behaviour.PipeBehaviourLimiter;

import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;

public class BCTransportStatements {

    // ---- Triggers ----

    public static TriggerItemsTraversing TRIGGER_ITEMS_TRAVERSING;
    public static TriggerFluidsTraversing TRIGGER_FLUIDS_TRAVERSING;
    public static TriggerPowerRequested TRIGGER_POWER_REQUESTED;
    public static TriggerPipeSignal[] TRIGGER_PIPE_SIGNAL;   // [colour.ordinal()*2+0]=inactive, [+1]=active

    // ---- Actions ----

    public static ActionPipeDirection[] ACTION_PIPE_DIRECTION;   // [Direction.ordinal()]
    public static ActionPipeColor[] ACTION_PIPE_COLOUR;          // [DyeColor.ordinal()]
    public static ActionPipeSignal[] ACTION_PIPE_SIGNAL;         // [DyeColor.ordinal()]
    public static ActionExtractionPreset[] ACTION_EXTRACTION_PRESET; // [SlotIndex.ordinal()]
    public static ActionPowerLimit[] ACTION_IRON_POWER_LIMIT;
    public static ActionPowerLimit[] ACTION_DIAMOND_POWER_LIMIT;
    public static ActionPowerLimit[] ACTION_IRON_RF_LIMIT;
    public static ActionPowerLimit[] ACTION_DIAMOND_RF_LIMIT;

    // ---- Parameter types (for createParameter lookups) ----

    public static ActionParameterSignal ACTION_PARAM_SIGNAL_EMPTY;
    public static TriggerParameterSignal TRIGGER_PARAM_SIGNAL_EMPTY;

    // ---- Providers ----

    public static final ITriggerProvider TRIGGER_PROVIDER = TriggerProviderPipes.INSTANCE;
    public static final IActionProvider ACTION_PROVIDER = ActionProviderPipes.INSTANCE;

    public static void preInit() {
        TRIGGER_ITEMS_TRAVERSING = new TriggerItemsTraversing();
        TRIGGER_FLUIDS_TRAVERSING = new TriggerFluidsTraversing();
        TRIGGER_POWER_REQUESTED = new TriggerPowerRequested();

        DyeColor[] colours = DyeColor.values();
        TRIGGER_PIPE_SIGNAL = new TriggerPipeSignal[colours.length * 2];
        for (DyeColor c : colours) {
            TRIGGER_PIPE_SIGNAL[c.ordinal() * 2 + 0] = new TriggerPipeSignal(false, c);
            TRIGGER_PIPE_SIGNAL[c.ordinal() * 2 + 1] = new TriggerPipeSignal(true, c);
        }

        ACTION_PIPE_DIRECTION = new ActionPipeDirection[Direction.values().length];
        for (Direction face : Direction.values()) {
            ACTION_PIPE_DIRECTION[face.ordinal()] = new ActionPipeDirection(face);
        }

        ACTION_PIPE_COLOUR = new ActionPipeColor[colours.length];
        for (DyeColor c : colours) {
            ACTION_PIPE_COLOUR[c.ordinal()] = new ActionPipeColor(c);
        }

        ACTION_PIPE_SIGNAL = new ActionPipeSignal[colours.length];
        for (DyeColor c : colours) {
            ACTION_PIPE_SIGNAL[c.ordinal()] = new ActionPipeSignal(c);
        }

        SlotIndex[] slots = SlotIndex.values();
        ACTION_EXTRACTION_PRESET = new ActionExtractionPreset[slots.length];
        for (SlotIndex s : slots) {
            ACTION_EXTRACTION_PRESET[s.ordinal()] = new ActionExtractionPreset(s);
        }

        int maxShift = PipeBehaviourLimiter.MAX_SHIFT + 1;
        ACTION_IRON_POWER_LIMIT = new ActionPowerLimit[maxShift];
        ACTION_DIAMOND_POWER_LIMIT = new ActionPowerLimit[maxShift];
        ACTION_IRON_RF_LIMIT = new ActionPowerLimit[maxShift];
        ACTION_DIAMOND_RF_LIMIT = new ActionPowerLimit[maxShift];
        for (int i = 0; i <= PipeBehaviourLimiter.MAX_SHIFT; i++) {
            ACTION_IRON_POWER_LIMIT[i] = new ActionPowerLimit.ActionIronPowerLimit(i);
            ACTION_DIAMOND_POWER_LIMIT[i] = new ActionPowerLimit.ActionDiamondPowerLimit(i);
            ACTION_IRON_RF_LIMIT[i] = new ActionPowerLimit.ActionIronRfLimit(i);
            ACTION_DIAMOND_RF_LIMIT[i] = new ActionPowerLimit.ActionDiamondRfLimit(i);
        }

        ACTION_PARAM_SIGNAL_EMPTY = ActionParameterSignal.EMPTY;
        TRIGGER_PARAM_SIGNAL_EMPTY = TriggerParameterSignal.EMPTY;
    }
}
