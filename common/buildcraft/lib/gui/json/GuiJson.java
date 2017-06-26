package buildcraft.lib.gui.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

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
import buildcraft.lib.gui.IContainingElement;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.button.GuiAbstractButton;
import buildcraft.lib.misc.collect.TypedKeyMap;

/** A GUI that is defined (mostly) in a json file. Note that implementors generally have to add {@link Slot}'s,
 * {@link ISprite}'s and configure buttons in code - currently this only allows for completely defining simple elements
 * via json, more complex ones must be implemented in code. */
public abstract class GuiJson<C extends ContainerBC_Neptune> extends GuiBC8<C> {
    private final ResourceLocation guiDefinition;
    protected final TypedKeyMap<String, Object> properties = TypedKeyMap.createHierachy();

    public GuiJson(C container, ResourceLocation guiDefinition) {
        super(container);
        this.guiDefinition = guiDefinition;
        load();
    }

    @Override
    protected boolean shouldAddHelpLedger() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    protected final void load() {
        try (IResource res = Minecraft.getMinecraft().getResourceManager().getResource(guiDefinition)) {
            JsonObject obj = new Gson().fromJson(new InputStreamReader(res.getInputStream()), JsonObject.class);

            properties.put("gui.root", rootElement);
            properties.put("mouse", mouse);
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
                    IGuiElement e = type.deserialize(this, rootElement, info, elem);
                    String parent = elem.properties.get("parent");
                    IContainingElement p = properties.get("custom." + parent, IContainingElement.class);
                    properties.put("custom." + elem.name, e);
                    if (p == null) {
                        shownElements.add(e);
                    } else {
                        p.getChildElements().add(e);
                    }
                }
            }
            postLoad();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    /** Fill up {@link #properties} */
    protected void preLoad() {}

    /** Setup objects contained in {@link #properties}. Usually via {@link #setup(String, Class, Consumer)}. */
    protected void postLoad() {}

    protected final <T> void setup(String name, Class<T> clazz, Consumer<T> ifNonNull) {
        T value = properties.get(name, clazz);
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
