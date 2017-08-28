package buildcraft.lib.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.IVariableNode.IVariableNodeBoolean;
import buildcraft.lib.expression.api.NodeType2;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

/** Stores configuration values about GUI elements. Primarily which ledger is open, however json based gui's may add
 * config options per-gui. */
public class GuiConfigManager {
    private final Map<ResourceLocation, GuiPropertySet> properties = new HashMap<>();

    public IVariableNode getOrAddProperty(ResourceLocation gui, String name, IExpressionNode value) {
        GuiPropertySet props = properties.computeIfAbsent(gui, r -> new GuiPropertySet());
        return props.getOrAddProperty(name, value);
    }

    public IVariableNodeBoolean getOrAddBoolean(ResourceLocation gui, String name, boolean defaultValue) {
        return (IVariableNodeBoolean) getOrAddProperty(gui, name, NodeConstantBoolean.of(defaultValue));
    }

    static class GuiPropertySet {
        public final Map<String, GuiProperty> properties = new HashMap<>();

        public IVariableNode getOrAddProperty(String name, IExpressionNode value) {
            GuiProperty prop = properties.computeIfAbsent(name, GuiProperty::new);
            return prop.getOrAdd(value);
        }
    }

    static class GuiProperty {
        public final String name;
        /** Map of NodeType to IVariable node (of that type). As NodeType<long> is impossible, this stores the class
         * instead. */
        public final Map<Class<?>, IVariableNode> values = new HashMap<>();

        public GuiProperty(String name) {
            this.name = name;
        }

        public IVariableNode getOrAdd(IExpressionNode value) {
            Class<?> clazz = NodeTypes.getType(value);
            IVariableNode existing = values.get(clazz);
            if (existing == null) {
                existing = NodeTypes.makeVariableNode(clazz, name);
                existing.set(value);
                values.put(clazz, existing);
            }
            return existing;
        }

        void writeToJson(JsonObject obj) {
            for (Entry<Class<?>, IVariableNode> entry : values.entrySet()) {
                Class<?> clazz = entry.getKey();
                IVariableNode node = entry.getValue();
                NodeTypes.getName(clazz);
                // TODO: Writing out + reading!
            }
        }
    }
}
