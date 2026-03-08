package buildcraft.silicon.gui;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.json.BuildCraftJsonGui;
import buildcraft.lib.misc.collect.TypedKeyMap;
import buildcraft.silicon.container.ContainerGate;
import buildcraft.silicon.gate.GateLogic;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiGate extends GuiBC8<ContainerGate> {

    public static final ResourceLocation GUI_DEFINITION = new ResourceLocation("buildcraftsilicon:gui/gate.json");

    public GuiGate(ContainerGate container, PlayerInventory inventory, ITextComponent component) {
        super(container, GUI_DEFINITION, inventory, component);

        BuildCraftJsonGui jsonGui = (BuildCraftJsonGui) mainGui;
        preLoad(jsonGui);
        jsonGui.load();
//        xSize = jsonGui.getSizeX();
        imageWidth = jsonGui.getSizeX();
//        ySize = jsonGui.getSizeY();
        imageHeight = jsonGui.getSizeY();
    }

    protected void preLoad(BuildCraftJsonGui json) {
        GateLogic gate = container.gate;
        TypedKeyMap<String, Object> properties = json.properties;
        FunctionContext context = json.context;
        properties.put("statement.container", gate);
        context.putConstantBoolean("gate.two_columns", gate.isSplitInTwo());
        context.putConstantLong("gate.slots", gate.variant.numSlots);
        context.putConstantLong("gate.triggers.args", gate.variant.numTriggerArgs);
        context.putConstantLong("gate.actions.args", gate.variant.numActionArgs);
        context.put_b("gate.two_columns", () -> gate.isOn);
        context.putConstant("gate.material", String.class, gate.variant.material.tag);
        context.putConstant("gate.modifier", String.class, gate.variant.modifier.tag);
        context.putConstant("gate.logic", String.class, gate.variant.logic.tag);
        context.putConstant("gate.variant", String.class, gate.variant.getLocalizedName().getString());
        properties.put("gate.triggers.possible", container.possibleTriggersContext);
        properties.put("gate.actions.possible", container.possibleActionsContext);

        context.put_l_b("gate.is_connected", (i) ->
        {
            if (i < 0 || i >= gate.connections.length) {
                return false;
            }
            return gate.connections[(int) i];
        }).setNeverInline();

        context.put_l_b("gate.trigger.is_on", (i) ->
        {
            if (i < 0 || i >= gate.triggerOn.length) {
                return false;
            }
            return gate.triggerOn[(int) i];
        }).setNeverInline();

        context.put_l_b("gate.set.is_on", (i) ->
        {
            if (i < 0 || i >= gate.triggerOn.length) {
                return false;
            }
            return gate.actionOn[(int) i];
        }).setNeverInline();

        context.put_l_b("gate.action.is_on", (i) ->
        {
            if (i < 0 || i >= gate.actionOn.length) {
                return false;
            }
            return gate.actionOn[(int) i] && gate.statements[(int) i].action.get() != null;
        }).setNeverInline();

        for (int s = 0; s < gate.variant.numSlots; s++) {
            final int i = s;
            String tName = "gate.trigger/" + i;
            String aName = "gate.action/" + i;
            properties.put(tName, gate.statements[i].trigger);
            properties.put(aName, gate.statements[i].action);
            properties.put(tName, container.possibleTriggersContext);
            properties.put(aName, container.possibleActionsContext);
        }

        for (int c = 0; c < gate.connections.length; c++) {
            final int connection = c;
            String name = "gate.connection/" + c;
            properties.put(name, gate.connections[c]);
            properties.put(name, IButtonBehaviour.TOGGLE);
            properties.put(name, (IButtonClickEventListener) (b, k) ->
            {
                container.setConnected(connection, b.isButtonActive());
            });
        }
    }
}
