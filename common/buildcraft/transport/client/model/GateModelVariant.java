package buildcraft.transport.client.model;

import buildcraft.lib.client.model.ModelHolderStatic;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelVariable;
import buildcraft.lib.client.model.ModelVariable.ModelVariantBuilder;
import buildcraft.lib.client.model.MutableQuad;

public class GateModelVariant {
    public static final ModelVariable VARIANT;
    public static final int INDEX_MATERIAL, INDEX_MODIFIER;
    public static final ModelHolderStatic MODEL;

    static {
        ModelVariantBuilder builder = new ModelVariantBuilder();
        INDEX_MATERIAL = builder.addVariableString("material");
        INDEX_MODIFIER = builder.addVariableString("modifier");
        VARIANT = builder.build();

        MODEL = new ModelHolderVariable("buildcrafttransport:models/plugs/gate", VARIANT);
    }

    public static MutableQuad[] getQuads(String material, String modifier) {
        VARIANT.strings[INDEX_MATERIAL] = material;
        VARIANT.strings[INDEX_MODIFIER] = modifier;
        return MODEL.getCutoutQuads();
    }
}
