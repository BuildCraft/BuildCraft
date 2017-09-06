package buildcraft.transport.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.expression.node.value.NodeConstantObject;
import buildcraft.lib.gui.json.GuiJson;

import buildcraft.transport.container.ContainerGate2;
import buildcraft.transport.gate.GateLogic;

public class GuiGate2 extends GuiJson<ContainerGate2> {

    public static final ResourceLocation GUI_DEFINITION = new ResourceLocation("buildcrafttransport:gui/gate.json");

    public GuiGate2(ContainerGate2 container) {
        super(container, GUI_DEFINITION);
    }

    @Override
    protected void preLoad() {
        super.preLoad();
        GateLogic gate = container.gate;
        properties.put("statement.container", gate);
        properties.put("gate.two_columns", NodeConstantBoolean.of(gate.isSplitInTwo()));
        properties.put("gate.slots", NodeConstantLong.of(gate.variant.numSlots));
        properties.put("gate.triggers.args", NodeConstantLong.of( gate.variant.numTriggerArgs));
        properties.put("gate.actions.args", NodeConstantLong.of(gate.variant.numActionArgs));
        properties.put("gate.is_on", (INodeBoolean) () -> gate.isOn);
        properties.put("gate.material",new NodeConstantObject<>(String.class,gate.variant.material.tag));
        properties.put("gate.modifier", new NodeConstantObject<>(String.class,gate.variant.modifier.tag));
        properties.put("gate.logic", new NodeConstantObject<>(String.class,gate.variant.logic.tag));
        properties.put("gate.variant", new NodeConstantObject<>(String.class, gate.variant.getLocalizedName()));

        for (int s = 0; s < gate.variant.numSlots; s++) {
            final int i = s;
            String tName = "gate.trigger[" + i + "]";
            String aName = "gate.action[" + i + "]";
            properties.put(tName, gate.statements[i].trigger);
            properties.put(aName, gate.statements[i].trigger);

            properties.put(tName + ".is_on", (INodeBoolean) () -> gate.triggerOn[i]);
            properties.put(aName + ".is_on", (INodeBoolean) () -> gate.actionOn[i]);

            if (s > 0) {
                final int j = i - 1;
                properties.put("gate.is_connected[" + j + "]", (INodeBoolean) () -> gate.connections[j]);
            }
        }
    }

    @Override
    protected void postLoad() {
        super.postLoad();
    }

}
