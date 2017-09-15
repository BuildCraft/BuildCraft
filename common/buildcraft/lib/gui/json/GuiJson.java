package buildcraft.lib.gui.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.render.ISprite;

import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.IContainingElement;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.collect.TypedKeyMap;
import buildcraft.lib.misc.data.ModelVariableData;

/** A GUI that is defined (mostly) in a json file. Note that implementors generally have to add {@link Slot}'s,
 * {@link ISprite}'s and configure buttons in code - currently this only allows for completely defining simple elements
 * via json, more complex ones must be implemented in code. */
public abstract class GuiJson<C extends ContainerBC_Neptune> extends GuiBC8<C> {
    public final ResourceLocation guiDefinition;
    protected final TypedKeyMap<String, Object> properties = TypedKeyMap.createHierachy();
    protected final FunctionContext context = DefaultContexts.createWithAll();
    ModelVariableData varData = new ModelVariableData();
    private final NodeVariableDouble time;
    private int timeOpen;

    public GuiJson(C container, ResourceLocation guiDefinition) {
        super(container);
        this.guiDefinition = guiDefinition;
        time = context.putVariableDouble("time");
        load();
    }

    @Override
    protected boolean shouldAddHelpLedger() {
        return true;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    protected final void load() {
        context.putConstant("gui.mouse", IGuiPosition.class, mouse);
        context.putConstant("gui.area", IGuiArea.class, rootElement);
        context.putConstant("gui.pos", IGuiPosition.class, rootElement);
        preLoad();

        ResourceLoaderContext loadHistory = new ResourceLoaderContext();
        try (InputStreamReader reader = loadHistory.startLoading(guiDefinition)) {
            JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
            JsonGuiInfo info = new JsonGuiInfo(obj, context, loadHistory);
            xSize = (int) GenericExpressionCompiler.compileExpressionLong(info.sizeX, context).evaluate();
            ySize = (int) GenericExpressionCompiler.compileExpressionLong(info.sizeY, context).evaluate();
            varData.setNodes(info.createTickableNodes());

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
                        p.calculateSizes();
                    }
                }
            }
            postLoad();
        } catch (InvalidExpressionException iee) {
            throw new JsonSyntaxException("Failed to resolve the size of " + guiDefinition, iee);
        } catch (IOException e) {
            throw new Error(e);
        }
        loadHistory.finishLoading();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        timeOpen++;
        varData.tick();
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        time.value = timeOpen + partialTicks;
        varData.refresh();
        super.drawBackgroundLayer(partialTicks);
    }

    @Override
    protected void drawForegroundLayer() {
        super.drawForegroundLayer();
        if (isDebuggingEnabled.evaluate()) {
            List<String> debug = new ArrayList<>();
            varData.addDebugInfo(debug);

            int x = 6;
            int y = 18;
            for (String d : debug) {
                // drawString(getFontRenderer(), d, x, y, -1);
                y += getFontRenderer().FONT_HEIGHT + 2;
            }
        }
    }

    /** Fill up {@link #properties} and {@link #context} */
    protected void preLoad() {
        properties.put("player.inventory", new InventorySlotHolder(container, container.player.inventory));
    }

    /** Called after the whole gui has been loaded. */
    protected void postLoad() {}
}
