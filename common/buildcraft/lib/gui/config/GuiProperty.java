package buildcraft.lib.gui.config;

import buildcraft.lib.expression.api.IVariableNode;
import com.google.gson.JsonElement;

public abstract class GuiProperty implements IVariableNode {
    public final String name;
    /** If true then this property will be shown in an options ledger. This is set to true automatically if a json file
     * has a config setting for it. */
    public boolean isVisible;

    GuiProperty(String name) {
        this.name = name;
    }

    public abstract JsonElement writeToJson();

    public abstract void readFromJson(JsonElement json);

    @Override
    public void setConstant(boolean isConst) {
        throw new UnsupportedOperationException("Cannot make config options constant!");
    }

    @Override
    public boolean isConstant() {
        return false;
    }
}
