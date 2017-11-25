package buildcraft.factory.client.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableObject;

import buildcraft.factory.BCFactoryModels;
import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.factory.block.BlockHeatExchange.EnumExchangePart;

public class ModelHeatExchange extends ModelItemSimple {

    public static final NodeType<EnumExchangePart> TYPE_EXCHANGE_PART;

    public static final FunctionContext FUNCTION_CONTEXT;

    public static final NodeVariableBoolean VAR_CONNECTED_LEFT;
    public static final NodeVariableBoolean VAR_CONNECTED_RIGHT;
    public static final NodeVariableBoolean VAR_CONNECTED_UP;
    public static final NodeVariableBoolean VAR_CONNECTED_DOWN;
    public static final NodeVariableObject<EnumExchangePart> VAR_PART;
    public static final NodeVariableObject<EnumFacing> VAR_DIRECTION;

    static {
        TYPE_EXCHANGE_PART = new NodeType<>("HeatExchangePart", EnumExchangePart.MIDDLE);
        NodeTypes.addType(TYPE_EXCHANGE_PART);
        for (EnumExchangePart part : EnumExchangePart.values()) {
            TYPE_EXCHANGE_PART.putConstant(part.getName(), part);
        }
        FUNCTION_CONTEXT = new FunctionContext("heat_exchange", DefaultContexts.createWithAll(), TYPE_EXCHANGE_PART);
        VAR_CONNECTED_LEFT = FUNCTION_CONTEXT.putVariableBoolean("connected_left");
        VAR_CONNECTED_RIGHT = FUNCTION_CONTEXT.putVariableBoolean("connected_right");
        VAR_CONNECTED_UP = FUNCTION_CONTEXT.putVariableBoolean("connected_up");
        VAR_CONNECTED_DOWN = FUNCTION_CONTEXT.putVariableBoolean("connected_down");
        VAR_PART = FUNCTION_CONTEXT.putVariableObject("part", EnumExchangePart.class);
        VAR_DIRECTION = FUNCTION_CONTEXT.putVariableObject("direction", EnumFacing.class);
    }

    public final List<BakedQuad> itemQuads = new ArrayList<>();
    private final List<List<BakedQuad>> cache = new ArrayList<>();

    public ModelHeatExchange() {
        super(ImmutableList.of(), TRANSFORM_BLOCK, false);

        VAR_CONNECTED_DOWN.value = false;
        VAR_CONNECTED_UP.value = false;
        VAR_CONNECTED_LEFT.value = false;
        VAR_CONNECTED_RIGHT.value = false;
        VAR_PART.value = EnumExchangePart.MIDDLE;
        VAR_DIRECTION.value = EnumFacing.NORTH;
        for (MutableQuad quad : BCFactoryModels.HEAT_EXCHANGE_STATIC.getCutoutQuads()) {
            quad.multShade();
            itemQuads.add(quad.toBakedItem());
        }

        for (int i = 0; i < 4 * 8 * 3; i++) {
            boolean connectedUpDown = (i & 4) == 4;
            EnumExchangePart part = EnumExchangePart.values()[i / (8 * 4)];
            VAR_CONNECTED_LEFT.value = (i & 1) == 1;
            VAR_CONNECTED_RIGHT.value = (i & 2) == 2;
            VAR_CONNECTED_UP.value = connectedUpDown && part == EnumExchangePart.END;
            VAR_CONNECTED_DOWN.value = connectedUpDown && part == EnumExchangePart.START;
            VAR_PART.value = part;
            VAR_DIRECTION.value = EnumFacing.getHorizontal((i / 8) & 3);
            List<BakedQuad> quads = new ArrayList<>();

            for (MutableQuad quad : BCFactoryModels.HEAT_EXCHANGE_STATIC.getCutoutQuads()) {
                quad.multShade();
                quads.add(quad.toBakedBlock());
            }

            cache.add(quads);
        }
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) {
            return ImmutableList.of();
        }
        return cache.get(getIndexOf(state));
    }

    private static int getIndexOf(IBlockState state) {
        return (state.getValue(BlockHeatExchange.PROP_CONNECTED_LEFT) ? 1 : 0)//
            | (state.getValue(BlockHeatExchange.PROP_CONNECTED_RIGHT) ? 2 : 0)//
            | (state.getValue(BlockHeatExchange.PROP_CONNECTED_Y) ? 4 : 0)//
            | (state.getValue(BlockBCBase_Neptune.PROP_FACING).getHorizontalIndex() * 8)//
            | (state.getValue(BlockHeatExchange.PROP_PART).ordinal() * 8 * 4)//
        ;
    }
}
