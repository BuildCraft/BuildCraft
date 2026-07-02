/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders;

import ct.buildcraft.builders.registry.FillerRegistry;
import ct.buildcraft.api.filler.FillerManager;
import ct.buildcraft.api.template.TemplateApi;
import ct.buildcraft.builders.addon.AddonFillerPlanner;
import ct.buildcraft.builders.snapshot.TemplateHandlerDefault;
import ct.buildcraft.builders.snapshot.TemplateRegistry;
import ct.buildcraft.core.marker.volume.AddonsRegistry;
import net.minecraft.resources.ResourceLocation;

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
