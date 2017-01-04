package buildcraft.lib.client.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.model.json.JsonModel;
import buildcraft.lib.client.model.json.JsonVariableModel;

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
        } catch (FileNotFoundException fnfe) {
            throw new JsonSyntaxException("Did not find the file " + location, fnfe);
        } catch (IOException io) {
            throw new JsonSyntaxException(io);
        }
    }

    public void finishLoading() {
        loadingStack.pop();
    }
}
