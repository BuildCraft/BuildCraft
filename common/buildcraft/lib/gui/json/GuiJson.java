package buildcraft.lib.gui.json;

import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.GuiBC8;

public abstract class GuiJson<C extends ContainerBC_Neptune> extends GuiBC8<C> {

    public GuiJson(C container, ResourceLocation guiDefinition) {
        super(container);
        try (IResource res = Minecraft.getMinecraft().getResourceManager().getResource(guiDefinition)) {
            JsonObject obj = new Gson().fromJson(new InputStreamReader(res.getInputStream()), JsonObject.class);

            JsonElement customTypes = obj.get("types");

            JsonElement elements;

        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
