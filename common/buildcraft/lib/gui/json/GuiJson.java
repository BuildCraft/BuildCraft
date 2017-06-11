package buildcraft.lib.gui.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.render.ISprite;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.button.GuiAbstractButton;

public abstract class GuiJson<C extends ContainerBC_Neptune> extends GuiBC8<C> {
    private final ResourceLocation guiDefinition;
    protected final Map<String, Supplier<String>> properties = new HashMap<>();
    protected final Map<String, ISprite> sprites = new HashMap<>();
    protected final Map<String, GuiAbstractButton<?>> buttons = new HashMap<>();

    public GuiJson(C container, ResourceLocation guiDefinition) {
        super(container);
        this.guiDefinition = guiDefinition;
    }

    @Override
    public void initGui() {
        load();
        super.initGui();
    }

    protected final void load() {
        try (IResource res = Minecraft.getMinecraft().getResourceManager().getResource(guiDefinition)) {
            JsonObject obj = new Gson().fromJson(new InputStreamReader(res.getInputStream()), JsonObject.class);

            preLoad();

            JsonGuiInfo info = new JsonGuiInfo(obj);
            xSize = info.sizeX;
            ySize = info.sizeY;

            for (JsonGuiElement elem : info.elements) {
                String typeName = elem.properties.get("type");
                ElementType type = JsonGuiDeserialiser.TYPES.get(typeName);
                if (type == null) {
                    BCLog.logger.warn("Unknown type " + typeName);
                } else {
                    type.addToGui(this, info, elem);
                }
            }
            postLoad();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    /** Add to {@link #sprites}, {@link #properties} */
    protected void preLoad() {}

    /** Setup {@link #buttons} */
    protected void postLoad() {}

    /** Helper method for setting up buttons in {@link #postLoad()}.
     * 
     * @param name The identifier for the button
     * @param ifNonNull Code to be run if the button was defined in the JSON file, and so is not null */
    protected final void setupButton(String name, Consumer<GuiAbstractButton<?>> ifNonNull) {
        GuiAbstractButton<?> button = buttons.get(name);
        if (button != null) {
            ifNonNull.accept(button);
        }
    }
}
