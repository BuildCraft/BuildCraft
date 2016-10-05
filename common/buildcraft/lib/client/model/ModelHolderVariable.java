package buildcraft.lib.client.model;

import java.util.List;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.expression.FunctionContext;

public class ModelHolderVariable extends ModelHolder {
    private final FunctionContext context;

    public ModelHolderVariable(String location, FunctionContext context) {
        super(location);
        this.context = context;
    }

    @Override
    protected void onTextureStitchPre(List<ResourceLocation> toRegisterSprites) {

    }

    @Override
    protected void onModelBake() {

    }

    @Override
    public boolean hasBakedQuads() {
        return false;
    }
}
