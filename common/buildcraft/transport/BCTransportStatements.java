/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.api.statements.StatementManager;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;
import buildcraft.transport.pipe.behaviour.PipeBehaviourLimiter;
import buildcraft.transport.statements.*;
import buildcraft.transport.statements.ActionPowerLimit.ActionDiamondPowerLimit;
import buildcraft.transport.statements.ActionPowerLimit.ActionDiamondRfLimit;
import buildcraft.transport.statements.ActionPowerLimit.ActionIronPowerLimit;
import buildcraft.transport.statements.ActionPowerLimit.ActionIronRfLimit;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class BCTransportStatements {

    public static final TriggerPipeSignal[] TRIGGER_PIPE_SIGNAL;
    public static final TriggerPowerRequested TRIGGER_POWER_REQUESTED;
    public static final TriggerItemsTraversing TRIGGER_ITEMS_TRAVERSING;
    public static final TriggerFluidsTraversing TRIGGER_FLUIDS_TRAVERSING;
    public static final ActionPipeSignal[] ACTION_PIPE_SIGNAL;
    public static final ActionPipeColor[] ACTION_PIPE_COLOUR;
    public static final ActionExtractionPreset[] ACTION_EXTRACTION_PRESET;
    public static final ActionPipeDirection[] ACTION_PIPE_DIRECTION;
    public static final ActionIronPowerLimit[] ACTION_IRON_POWER_LIMIT;
    public static final ActionDiamondPowerLimit[] ACTION_DIAMOND_POWER_LIMIT;
    public static final ActionIronRfLimit[] ACTION_IRON_RF_LIMIT;
    public static final ActionDiamondRfLimit[] ACTION_DIAMOND_RF_LIMIT;

    static {
        TRIGGER_PIPE_SIGNAL = new TriggerPipeSignal[2 * ColourUtil.COLOURS.length];
        for (DyeColor colour : ColourUtil.COLOURS) {
            TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 0] = new TriggerPipeSignal(true, colour);
            TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 1] = new TriggerPipeSignal(false, colour);
        }

        ACTION_PIPE_SIGNAL = new ActionPipeSignal[ColourUtil.COLOURS.length];
        for (DyeColor colour : ColourUtil.COLOURS) {
            ACTION_PIPE_SIGNAL[colour.ordinal()] = new ActionPipeSignal(colour);
        }

        ACTION_PIPE_COLOUR = new ActionPipeColor[ColourUtil.COLOURS.length];
        for (DyeColor colour : ColourUtil.COLOURS) {
            ACTION_PIPE_COLOUR[colour.ordinal()] = new ActionPipeColor(colour);
        }

        ACTION_EXTRACTION_PRESET = new ActionExtractionPreset[SlotIndex.VALUES.length];
        for (SlotIndex index : SlotIndex.VALUES) {
            ACTION_EXTRACTION_PRESET[index.ordinal()] = new ActionExtractionPreset(index);
        }

        ACTION_PIPE_DIRECTION = new ActionPipeDirection[Direction.values().length];
        for (Direction face : Direction.values()) {
            ACTION_PIPE_DIRECTION[face.ordinal()] = new ActionPipeDirection(face);
        }

        TRIGGER_POWER_REQUESTED = new TriggerPowerRequested();
        TRIGGER_ITEMS_TRAVERSING = new TriggerItemsTraversing();
        TRIGGER_FLUIDS_TRAVERSING = new TriggerFluidsTraversing();

        ACTION_IRON_POWER_LIMIT = new ActionIronPowerLimit[PipeBehaviourLimiter.MAX_SHIFT + 1];
        ACTION_DIAMOND_POWER_LIMIT = new ActionDiamondPowerLimit[PipeBehaviourLimiter.MAX_SHIFT + 1];
        ACTION_IRON_RF_LIMIT = new ActionIronRfLimit[PipeBehaviourLimiter.MAX_SHIFT + 1];
        ACTION_DIAMOND_RF_LIMIT = new ActionDiamondRfLimit[PipeBehaviourLimiter.MAX_SHIFT + 1];
        int index = 0;
        for (int shift = PipeBehaviourLimiter.MAX_SHIFT; shift >= 0; shift--) {
            ACTION_IRON_POWER_LIMIT[index] = new ActionIronPowerLimit(shift);
            ACTION_DIAMOND_POWER_LIMIT[index] = new ActionDiamondPowerLimit(shift);
            if (!BCTransportConfig.disableRfPipe) {
                ACTION_IRON_RF_LIMIT[index] = new ActionIronRfLimit(shift);
                ACTION_DIAMOND_RF_LIMIT[index] = new ActionDiamondRfLimit(shift);
            }
            index++;
        }

        StatementManager.registerParameter(TriggerParameterSignal::readFromNbt, TriggerParameterSignal::readFromBuf);
        StatementManager.registerParameter(ActionParameterSignal::readFromNbt);
    }

    public static void preInit() {
        StatementManager.registerTriggerProvider(TriggerProviderPipes.INSTANCE);
        StatementManager.registerActionProvider(ActionProviderPipes.INSTANCE);
    }
}
