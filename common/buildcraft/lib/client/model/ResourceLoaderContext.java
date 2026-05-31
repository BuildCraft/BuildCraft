/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.client.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonSyntaxException;

import net.minecraft.client.MinecraftClientClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class ResourceLoaderContext {
    private final Set<Identifier> loaded = new HashSet<>();
    private final Deque<Identifier> loadingStack = new ArrayDeque<>();

    public InputStreamReader startLoading(Identifier location) throws IOException {
        if (!loaded.add(location)) {
            throw new JsonSyntaxException("Already loaded " + location + " from " + loadingStack.peek());
        }
        loadingStack.push(location);
        Optional<Resource> res = MinecraftClient.getInstance().getResourceManager().getResource(location);
        if (!res.isPresent()) {
            throw new IOException("Resource not found: " + location);
        }
        return new InputStreamReader(res.get().getInputStream(), StandardCharsets.UTF_8);
    }

    public void finishLoading() {
        loadingStack.pop();
    }
}
