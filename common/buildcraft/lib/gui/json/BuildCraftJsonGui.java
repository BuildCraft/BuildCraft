package buildcraft.lib.gui.json;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.IContainingElement;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.collect.TypedKeyMap;
import buildcraft.lib.misc.data.ModelVariableData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import java.io.IOException;
import java.io.InputStreamReader;

/** A GUI that is defined (mostly) in a json file. Note that callers generally have to add {@link Slot}'s,
 * {@link ISprite}'s and configure buttons in code - currently this only allows for completely defining simple elements
 * via json, more complex ones must be implemented in code. */
public class BuildCraftJsonGui extends BuildCraftGui {

    public final ResourceLocation jsonGuiDefinition;

    public final TypedKeyMap<String, Object> properties = TypedKeyMap.createHierachy();
    public final FunctionContext context = DefaultContexts.createWithAll();
    final ModelVariableData varData = new ModelVariableData();
    private final NodeVariableDouble time;
    private int timeOpen;

    private int sizeX, sizeY;

    {
        context.putConstant("gui.mouse", IGuiPosition.class, mouse);
        context.putConstant("gui.area", IGuiArea.class, rootElement);
        context.putConstant("gui.pos", IGuiPosition.class, rootElement);
        time = context.putVariableDouble("time");
    }

    public BuildCraftJsonGui(Screen gui, ResourceLocation jsonGuiDefinition) {
        super(gui);
        this.jsonGuiDefinition = jsonGuiDefinition;
    }

    public BuildCraftJsonGui(Screen gui, IGuiArea rootElement, ResourceLocation jsonGuiDefinition) {
        super(gui, rootElement);
        this.jsonGuiDefinition = jsonGuiDefinition;
    }

    public final void load() {
        ResourceLoaderContext loadHistory = new ResourceLoaderContext();
        try (InputStreamReader reader = loadHistory.startLoading(jsonGuiDefinition)) {
            JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
            JsonGuiInfo info = new JsonGuiInfo(obj, context, loadHistory);
            sizeX = (int) GenericExpressionCompiler.compileExpressionLong(info.sizeX, context).evaluate();
            sizeY = (int) GenericExpressionCompiler.compileExpressionLong(info.sizeY, context).evaluate();
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
        } catch (InvalidExpressionException iee) {
            throw new JsonSyntaxException("Failed to resolve the size of " + jsonGuiDefinition, iee);
        } catch (IOException e) {
            throw new Error(e);
        }
        loadHistory.finishLoading();
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    @Override
    public void tick() {
        super.tick();
        timeOpen++;
        varData.tick();
    }

    @Override
    public void drawBackgroundLayer(PoseStack poseStack, float partialTicks, int mouseX, int mouseY, Runnable backgroundRenderer) {
        time.value = timeOpen + partialTicks;
        varData.refresh();
        super.drawBackgroundLayer(poseStack, partialTicks, mouseX, mouseY, backgroundRenderer);
    }
}
