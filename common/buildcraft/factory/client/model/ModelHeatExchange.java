package buildcraft.factory.client.model;

import buildcraft.factory.BCFactoryModels;
import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.factory.block.BlockHeatExchange.EnumExchangePart;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.misc.SpriteUtil;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModelHeatExchange extends ModelItemSimple {

    public static final NodeType<EnumExchangePart> TYPE_EXCHANGE_PART;

    public static final FunctionContext FUNCTION_CONTEXT;

    public static final NodeVariableBoolean VAR_CONNECTED_LEFT;
    public static final NodeVariableBoolean VAR_CONNECTED_RIGHT;
    public static final NodeVariableBoolean VAR_CONNECTED_UP;
    public static final NodeVariableBoolean VAR_CONNECTED_DOWN;
    public static final NodeVariableObject<EnumExchangePart> VAR_PART;
    public static final NodeVariableObject<Direction> VAR_DIRECTION;

    static {
        TYPE_EXCHANGE_PART = new NodeType<>("HeatExchangePart", EnumExchangePart.MIDDLE);
        NodeTypes.addType(TYPE_EXCHANGE_PART);
        for (EnumExchangePart part : EnumExchangePart.values()) {
            TYPE_EXCHANGE_PART.putConstant(part.getSerializedName(), part);
        }
        FUNCTION_CONTEXT = new FunctionContext("heat_exchange", DefaultContexts.createWithAll(), TYPE_EXCHANGE_PART);
        VAR_CONNECTED_LEFT = FUNCTION_CONTEXT.putVariableBoolean("connected_left");
        VAR_CONNECTED_RIGHT = FUNCTION_CONTEXT.putVariableBoolean("connected_right");
        VAR_CONNECTED_UP = FUNCTION_CONTEXT.putVariableBoolean("connected_up");
        VAR_CONNECTED_DOWN = FUNCTION_CONTEXT.putVariableBoolean("connected_down");
        VAR_PART = FUNCTION_CONTEXT.putVariableObject("part", EnumExchangePart.class);
        VAR_DIRECTION = FUNCTION_CONTEXT.putVariableObject("direction", Direction.class);
    }

    private final TextureAtlasSprite particle;
    private final List<List<BakedQuad>> cache = new ArrayList<>();

    public ModelHeatExchange() {
        super(ImmutableList.of(), TRANSFORM_BLOCK, false);

        VAR_CONNECTED_DOWN.value = false;
        VAR_CONNECTED_UP.value = false;
        VAR_CONNECTED_LEFT.value = false;
        VAR_CONNECTED_RIGHT.value = false;
        VAR_PART.value = EnumExchangePart.MIDDLE;
        VAR_DIRECTION.value = Direction.NORTH;

        if (BCFactoryModels.HEAT_EXCHANGE_STATIC.getCutoutQuads().length == 0) {
//            particle = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
            particle = SpriteUtil.missingSprite();
        } else {
            particle = BCFactoryModels.HEAT_EXCHANGE_STATIC.getCutoutQuads()[0].toBakedItem().getSprite();
        }

        for (int i = 0; i < 4 * 8 * 3; i++) {
            boolean connectedUpDown = (i & 4) == 4;
            EnumExchangePart part = EnumExchangePart.values()[i / (8 * 4)];
            VAR_CONNECTED_LEFT.value = (i & 1) == 1;
            VAR_CONNECTED_RIGHT.value = (i & 2) == 2;
            VAR_CONNECTED_UP.value = connectedUpDown && part == EnumExchangePart.END;
            VAR_CONNECTED_DOWN.value = connectedUpDown && part == EnumExchangePart.START;
            VAR_PART.value = part;
            VAR_DIRECTION.value = Direction.from2DDataValue((i / 8) & 3);
            List<BakedQuad> quads = new ArrayList<>();

            for (MutableQuad quad : BCFactoryModels.HEAT_EXCHANGE_STATIC.getCutoutQuads()) {
                quad.multShade();
                quads.add(quad.toBakedBlock());
            }

            cache.add(quads);
        }
    }

    @Override
//    public TextureAtlasSprite getParticleTexture()
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
//    public List<BakedQuad> getQuads(BlockState state, Direction side, long rand)
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand) {
        // Calen: state == null
        // NullPointerException: Cannot invoke "net.minecraft.block.BlockState.m_61143_(net.minecraft.state.Property)" because "state" is null
        // at buildcraft.factory.client.model.ModelHeatExchange.getIndexOf(ModelHeatExchange.java:124)
        // at buildcraft.factory.client.model.ModelHeatExchange.m_6840_(ModelHeatExchange.java:119)
        // at net.minecraft.client.renderer.entity.ItemRenderer.m_115189_(ItemRenderer.java:105)
        // ...
        // at mezz.jei.common.render.ItemStackRenderer.render(ItemStackRenderer.java:41)
//        if (side != null)
        if (side != null || state == null) {
            return ImmutableList.of();
        }
        return cache.get(getIndexOf(state));
    }

    private static int getIndexOf(BlockState state) {
        return (state.getValue(BlockHeatExchange.PROP_CONNECTED_LEFT) ? 1 : 0)//
                | (state.getValue(BlockHeatExchange.PROP_CONNECTED_RIGHT) ? 2 : 0)//
                | (state.getValue(BlockHeatExchange.PROP_CONNECTED_Y) ? 4 : 0)//
                | (state.getValue(BlockBCBase_Neptune.PROP_FACING).get2DDataValue() * 8)//
                | (state.getValue(BlockHeatExchange.PROP_PART).ordinal() * 8 * 4)//
                ;
    }
}
