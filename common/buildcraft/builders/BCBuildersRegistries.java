/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import buildcraft.api.template.TemplateApi;
import buildcraft.builders.snapshot.TemplateHandlerDefault;
import buildcraft.builders.snapshot.TemplateRegistry;

public class BCBuildersRegistries {
    public static void preInit() {
        TemplateApi.templateRegistry = TemplateRegistry.INSTANCE;
    }

    public static void init() {
        TemplateApi.templateRegistry.addHandler(TemplateHandlerDefault.INSTANCE);
    }
}
