/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ct.buildcraft.api.core.BCDebugging;
import ct.buildcraft.api.core.BCLog;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;

public class ModelHolderRegistry {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.model.holder");

    static final List<ModelHolder> HOLDERS_JSONBAKE = new ArrayList<>();
    static final List<ModelHolder> HOLDERS_VANILLABAKE = new ArrayList<>();

    public static void onTextureStitchPre(Pre event) {
        Set<ResourceLocation> toStitch = new HashSet<>();
        for (ModelHolder holder : HOLDERS_JSONBAKE) {
            holder.onTextureStitch(toStitch);
        }

        for (ResourceLocation res : toStitch) {
        	event.addSprite(res);
        }
    }
	public static void preModelBake(RegisterAdditional event) {
        for (ModelHolder holder : HOLDERS_VANILLABAKE) {
            holder.onModelBakePre(event);
        }
	}
    
    public static void onModelBake(BakingCompleted event) {
        for (ModelHolder holder : HOLDERS_JSONBAKE) {
            holder.onModelBake(event);
        }
        for (ModelHolder holder : HOLDERS_VANILLABAKE) {
            holder.onModelBake(event);
        }
        if (DEBUG) {
            BCLog.logger.info("[lib.model.holder] List of registered Models:");
            List<ModelHolder> holders = new ArrayList<>();
            holders.addAll(HOLDERS_JSONBAKE);
            holders.sort(Comparator.comparing(a -> a.modelLocation.toString()));

            for (ModelHolder holder : holders) {
                String status = "  ";
                if (holder.failReason != null) {
                    status += "(" + holder.failReason + ")";
                } else if (!holder.hasBakedQuads()) {
                    status += "(Model was registered too late)";
                }

                BCLog.logger.info("  - " + holder.modelLocation + status);
            }
            BCLog.logger.info("[lib.model.holder] Total of " + HOLDERS_JSONBAKE.size() + " models");
        }
    }
}
