/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class ResourceLoaderContext {
    private final Set<ResourceLocation> loaded = new HashSet<>();
    private final Deque<ResourceLocation> loadingStack = new ArrayDeque<>();

    public InputStreamReader startLoading(ResourceLocation location) throws IOException {
        if (!loaded.add(location)) {
            throw new JsonSyntaxException("Already loaded " + location + " from " + loadingStack.peek());
        }
        loadingStack.push(location);
        Resource res = Minecraft.getInstance().getResourceManager().getResource(location).get();

        return new InputStreamReader(res.open(), StandardCharsets.UTF_8);
    }

    // Calen 1.20.1
    public InputStreamReader datagenStartLoading(ResourceLocation location, ExistingFileHelper fileHelper) throws IOException {
        if (!loaded.add(location)) {
            throw new JsonSyntaxException("Already loaded " + location + " from " + loadingStack.peek());
        }
        loadingStack.push(location);
        Resource res = fileHelper.getResource(location, PackType.CLIENT_RESOURCES);

        return new InputStreamReader(res.open(), StandardCharsets.UTF_8);
    }

    public void finishLoading() {
        loadingStack.pop();
    }
}
