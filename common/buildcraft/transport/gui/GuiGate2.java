package buildcraft.transport.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.expression.node.value.NodeConstantObject;
import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.json.GuiJson;
import buildcraft.lib.misc.MessageUtil;

import buildcraft.transport.container.ContainerGate2;
import buildcraft.transport.gate.GateLogic;

public class GuiGate2 extends GuiJson<ContainerGate2> {

    public static final ResourceLocation GUI_DEFINITION = new ResourceLocation("buildcrafttransport:gui/gate.json");

    public GuiGate2(ContainerGate2 container) {
        super(container, GUI_DEFINITION);

        MessageUtil.doDelayed(() -> {
            container.sendMessage(ContainerGate2.ID_VALID_STATEMENTS);
        });
    }

    @Override
    protected void preLoad() {
        super.preLoad();
        GateLogic gate = container.gate;
        properties.put("statement.container", gate);
        context.putConstantBoolean("gate.two_columns", gate.isSplitInTwo());
        context.putConstantLong("gate.slots", gate.variant.numSlots);
        context.putConstantLong("gate.triggers.args", gate.variant.numTriggerArgs);
        context.putConstantLong("gate.actions.args", gate.variant.numActionArgs);
        context.put_b("gate.two_columns", () -> gate.isOn);
        context.putConstant("gate.material", String.class, gate.variant.material.tag);
        context.putConstant("gate.modifier", String.class, gate.variant.modifier.tag);
        context.putConstant("gate.logic", String.class, gate.variant.logic.tag);
        context.putConstant("gate.variant", String.class, gate.variant.getLocalizedName());
        properties.put("gate.triggers.possible", container.possibleTriggersContext);
        properties.put("gate.actions.possible", container.possibleActionsContext);

        context.put_l_b("gate.is_connected", (i) -> {
            return gate.connections[(int) i];
        });

        context.put_l_b("gate.trigger.is_on", (i) -> {
            return gate.triggerOn[(int) i];
        });

        context.put_l_b("gate.action.is_on", (i) -> {
            return gate.actionOn[(int) i];
        });

        for (int s = 0; s < gate.variant.numSlots; s++) {
            final int i = s;
            String tName = "gate.trigger[" + i + "]";
            String aName = "gate.action[" + i + "]";
            properties.put(tName, gate.statements[i].trigger);
            properties.put(aName, gate.statements[i].trigger);
        }
    }

    @Override
    protected void postLoad() {
        super.postLoad();
        GateLogic gate = container.gate;
        for (int s = 0; s < gate.variant.numSlots; s++) {
            final int i = s;
            setupButton("gate_connection[" + s + "]", (button) -> {
                button.setBehaviour(IButtonBehaviour.TOGGLE);
                button.setActive(gate.connections[i]);
                button.registerListener((b2, k) -> {
                    container.setConnected(i, button.isActive());
                });
            });
        }
    }
}
