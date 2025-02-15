package buildcraft.lib.gui.statement;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.IStatementParameter.DrawType;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.GuiUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/** Specialised class for rendering {@link IStatementParameter}. */
public class ParameterRenderer {

    private static final ISimpleDrawable BACKGROUND_DRAWABLE = (p, x, y) ->
    {
        GuiElementStatement.SLOT_COLOUR.drawAt(p, x, y);
    };
    private static final Map<DrawType, Function<IStatementParameter, ISimpleDrawable>> drawTypes;

    static {
        drawTypes = new EnumMap<>(DrawType.class);
        drawTypes.put(DrawType.SPRITE_ONLY, ParameterRenderer::getSpriteDrawable);
        drawTypes.put(DrawType.STACK_ONLY, (p) -> getStackDrawable(p, false));
        drawTypes.put(DrawType.STACK_ONLY_OR_QUESTION_MARK, (p) -> getStackDrawable(p, true));
        drawTypes.put(DrawType.SPRITE_STACK, (p) -> getSpriteDrawable(p).andThen(getStackDrawable(p, false)));
        drawTypes.put(DrawType.STACK_SPRITE, (p) -> getStackDrawable(p, false).andThen(getSpriteDrawable(p)));
        drawTypes.put(DrawType.SPRITE_STACK_OR_QUESTION_MARK, (p) ->
        {
            return getSpriteDrawable(p).andThen(getStackDrawable(p, true));
        });
        drawTypes.put(DrawType.STACK_OR_QUESTION_MARK_THEN_SPRITE, (p) ->
        {
            return getStackDrawable(p, true).andThen(getSpriteDrawable(p));
        });
    }

    public static ISimpleDrawable getSpriteDrawable(IStatementParameter param) {
        return (p, x, y) ->
        {
            ISprite sprite = param.getSprite();
            if (sprite != null) {
                GuiIcon.drawAt(sprite, p, x + 1, y + 1, 16);
            }
        };
    }

    public static ISimpleDrawable getStackDrawable(IStatementParameter param, boolean orQuestionMark) {
        return (p, x, y) ->
        {
            ItemStack stack = param.getItemStack();
            if (!stack.isEmpty()) {
                GuiUtil.drawItemStackAt(stack, p, (int) x + 1, (int) y + 1);
            } else if (orQuestionMark) {
                GuiElementStatement.ICON_SLOT_NOT_SET.drawAt(p, x + 1, y + 1);
            }
        };
    }

    public static ISimpleDrawable getDrawable(IStatementParameter param) {
        if (param instanceof IDrawingParameter) {
            return BACKGROUND_DRAWABLE.andThen(((IDrawingParameter) param).getDrawable());
        }
        DrawType type = param.getDrawType();
        return BACKGROUND_DRAWABLE.andThen(drawTypes.get(type).apply(param));
    }

    public static void draw(IStatementParameter param, GuiGraphics guiGraphics, double x, double y) {
        getDrawable(param).drawAt(guiGraphics, x, y);
    }
}
