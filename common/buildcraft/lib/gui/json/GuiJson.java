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
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.render.ISprite;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.button.GuiAbstractButton;
import buildcraft.lib.misc.collect.TypedKeyMap;

/** A GUI that is defined (mostly) in a json file. Note that implementors generally have to add {@link Slot}'s,
 * {@link ISprite}'s and configure buttons in code - currently this only allows for completely defining simple elements
 * via json, more complex ones must be implemented in code. */
public abstract class GuiJson<C extends ContainerBC_Neptune> extends GuiBC8<C> {
    private final ResourceLocation guiDefinition;
    protected final Map<String, Supplier<String>> properties = new HashMap<>();
    protected final TypedKeyMap<String, Object> miscProperties = TypedKeyMap.createHierachy();

    public GuiJson(C container, ResourceLocation guiDefinition) {
        super(container);
        this.guiDefinition = guiDefinition;
        load();
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    protected final void load() {
        try (IResource res = Minecraft.getMinecraft().getResourceManager().getResource(guiDefinition)) {
            JsonObject obj = new Gson().fromJson(new InputStreamReader(res.getInputStream()), JsonObject.class);

            miscProperties.put("gui.root", rootElement);
            miscProperties.put("mouse", mouse);
            preLoad();

            JsonGuiInfo info = new JsonGuiInfo(obj);
            xSize = info.sizeX;
            ySize = info.sizeY;

            for (JsonGuiElement elem : info.elements) {
                String typeName = elem.properties.get("type");
                ElementType type = JsonGuiTypeRegistry.TYPES.get(typeName);
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

    /** Add strings to {@link #properties}, and put {@link ISprite}'s into {@link #miscProperties} */
    protected void preLoad() {}

    /** Setup objects contained in {@link #miscProperties}. Usually via {@link #setup(String, Class, Consumer)}. */
    protected void postLoad() {}

    protected final <T> void setup(String name, Class<T> clazz, Consumer<T> ifNonNull) {
        T value = miscProperties.get(name, clazz);
        if (value != null) {
            ifNonNull.accept(value);
        }
    }

    /** Helper method for setting up buttons in {@link #postLoad()}.
     * 
     * @param name The identifier for the button
     * @param ifNonNull Code to be run if the button was defined in the JSON file, and so is not null */
    protected final void setupButton(String name, Consumer<GuiAbstractButton> ifNonNull) {
        setup(name, GuiAbstractButton.class, ifNonNull);
    }
}
