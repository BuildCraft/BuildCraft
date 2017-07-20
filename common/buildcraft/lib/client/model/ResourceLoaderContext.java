/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class ResourceLoaderContext {
    private final Set<ResourceLocation> loaded = new HashSet<>();
    private final Deque<ResourceLocation> loadingStack = new ArrayDeque<>();

    public InputStreamReader startLoading(ResourceLocation location) throws JsonSyntaxException {
        if (!loaded.add(location)) {
            throw new JsonSyntaxException("Already loaded " + location + " from " + loadingStack.peek());
        }
        loadingStack.push(location);
        try {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(location);
            return new InputStreamReader(res.getInputStream());
        } catch (FileNotFoundException e) {
            throw new JsonSyntaxException("Did not find the file " + location, e);
        } catch (IOException io) {
            throw new JsonSyntaxException(io);
        }
    }

    public void finishLoading() {
        loadingStack.pop();
    }
}
