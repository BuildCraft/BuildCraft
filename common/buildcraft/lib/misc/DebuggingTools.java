/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.lib.world.WorldEventListenerAdapter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

public class DebuggingTools {
    public static final boolean ENABLE = BCDebugging.shouldDebugComplex("lib.debug.world");

    public static void fmlInit() {
        if (ENABLE) {
            MinecraftForge.EVENT_BUS.register(new EventHook());
        }
    }

    private static class EventHook {
        @SubscribeEvent
        public void worldLoadEvent(WorldEvent.Load load) {
            load.getWorld().addEventListener(new WorldListener());
        }
    }

    private static class WorldListener extends WorldEventListenerAdapter {
        @Override
        public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
            StackTraceElement[] elements = new Throwable().getStackTrace();
            String[] bc = new String[elements.length];
            int bcIndex = 0;
            for (int i = 1; i < elements.length; i++) {
                StackTraceElement ste = elements[i];
                if (!ste.getClassName().startsWith("buildcraft")) continue;
                bc[bcIndex++] = ste.getClassName() + " # " + ste.getMethodName() + " : " + ste.getLineNumber();
            }
            if (bcIndex > 0) {
                BCLog.logger.info("[lib.debug.world] markBlockRangeForRenderUpdate(" + x1 + ", " + y1 + ", " + z1 + ", " + x2 + ", " + y2 + ", " + z2 + ")");
                for (int i = 0; i < bcIndex; i++) {
                    BCLog.logger.info("[lib.debug.world]   at " + bc[i]);
                }
            }
        }

    }
}
