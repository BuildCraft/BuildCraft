package ct.buildcraft.silicon.gui;

import ct.buildcraft.lib.expression.FunctionContext;
import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.button.IButtonBehaviour;
import ct.buildcraft.lib.gui.button.IButtonClickEventListener;
import ct.buildcraft.lib.gui.json.BuildCraftJsonGui;
import ct.buildcraft.lib.misc.collect.TypedKeyMap;
import ct.buildcraft.silicon.container.ContainerGate;
import ct.buildcraft.silicon.gate.GateLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiGate extends GuiBC8<ContainerGate> {

    public static final ResourceLocation GUI_DEFINITION = new ResourceLocation("buildcraftsilicon:gui/gate.json");

    public GuiGate(ContainerGate container, Inventory inv, Component title) {
        super(container, GUI_DEFINITION, inv, title);

        BuildCraftJsonGui jsonGui = (BuildCraftJsonGui) mainGui;
        preLoad(jsonGui);
        jsonGui.load();
        imageWidth = jsonGui.getSizeX();
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

        context.put_l_b("gate.is_connected", (i) -> {
            if (i < 0 || i >= gate.connections.length) {
                return false;
            }
            return gate.connections[(int) i];
        }).setNeverInline();

        context.put_l_b("gate.trigger.is_on", (i) -> {
            if (i < 0 || i >= gate.triggerOn.length) {
                return false;
            }
            return gate.triggerOn[(int) i];
        }).setNeverInline();

        context.put_l_b("gate.set.is_on", (i) -> {
            if (i < 0 || i >= gate.triggerOn.length) {
                return false;
            }
            return gate.actionOn[(int) i];
        }).setNeverInline();

        context.put_l_b("gate.action.is_on", (i) -> {
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
            properties.put(name, (IButtonClickEventListener) (b, k) -> {
                container.setConnected(connection, b.isButtonActive());
            });
        }
    }
}
