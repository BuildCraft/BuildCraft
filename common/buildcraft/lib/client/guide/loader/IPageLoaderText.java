/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.loader;

import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.nio.charset.StandardCharsets;

public interface IPageLoaderText extends IPageLoader {
    @Override
    default GuidePageFactory loadPage(InputStream in, ResourceLocation name, PageEntry<?> entry, IProfiler prof) throws IOException {
        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        return loadPage(new BufferedReader(reader), name, entry, prof);
    }

    GuidePageFactory loadPage(BufferedReader reader, ResourceLocation name, PageEntry<?> entry, IProfiler prof) throws IOException;
}
