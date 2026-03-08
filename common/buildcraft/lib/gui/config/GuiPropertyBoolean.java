package buildcraft.lib.gui.config;

import buildcraft.api.core.BCLog;
import buildcraft.lib.expression.api.IVariableNode.IVariableNodeBoolean;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class GuiPropertyBoolean extends GuiProperty implements IVariableNodeBoolean {

    private boolean value;

    public GuiPropertyBoolean(String name) {
        super(name);
    }

    // IVariableNodeBoolean

    @Override
    public void set(boolean value) {
        this.value = value;
        GuiConfigManager.markDirty();
    }

    @Override
    public boolean evaluate() {
        return value;
    }

    // GuiProperty

    @Override
    public JsonElement writeToJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void readFromJson(JsonElement json) {
        if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isBoolean()) {
            BCLog.logger.warn("[lib.gui.config] Tried to read " + json + " as a boolean, but it wasn't!");
        } else {
            value = json.getAsBoolean();
        }
    }
}
