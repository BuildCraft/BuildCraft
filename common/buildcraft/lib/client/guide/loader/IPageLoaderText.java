/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.parts.GuidePageFactory;

public interface IPageLoaderText extends IPageLoader {
    @Override
    default GuidePageFactory loadPage(InputStream in, ResourceLocation name, PageEntry<?> entry, Profiler prof) throws IOException {
        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        return loadPage(new BufferedReader(reader), name, entry, prof);
    }

    GuidePageFactory loadPage(BufferedReader reader, ResourceLocation name, PageEntry<?> entry, Profiler prof) throws IOException;
}
