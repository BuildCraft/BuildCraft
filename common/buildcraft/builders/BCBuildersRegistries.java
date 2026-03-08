/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.template.TemplateApi;
import buildcraft.builders.addon.AddonFillerPlanner;
import buildcraft.builders.registry.FillerRegistry;
import buildcraft.builders.snapshot.TemplateHandlerDefault;
import buildcraft.builders.snapshot.TemplateRegistry;
import buildcraft.core.marker.volume.AddonsRegistry;
import net.minecraft.util.ResourceLocation;

public class BCBuildersRegistries {
    public static void preInit() {
        TemplateApi.templateRegistry = TemplateRegistry.INSTANCE;
        FillerManager.registry = FillerRegistry.INSTANCE;

        AddonsRegistry.INSTANCE.register(new ResourceLocation("buildcraftbuilders:filler_planner"),
                AddonFillerPlanner.class);
    }

    public static void init() {
        TemplateApi.templateRegistry.addHandler(TemplateHandlerDefault.INSTANCE);
    }
}
