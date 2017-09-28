package buildcraft.lib.gui.statement;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.IStatementParameter.DrawType;

import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.GuiUtil;

/** Specialised class for rendering {@link IStatementParameter}. */
public class ParameterRenderer {

    private static final ISimpleDrawable BACKGROUND_DRAWABLE = (x, y) -> {
        GuiElementStatement.SLOT_COLOUR.drawAt(x, y);
    };
    private static final Map<DrawType, Function<IStatementParameter, ISimpleDrawable>> drawTypes;

    static {
        drawTypes = new EnumMap<>(DrawType.class);
        drawTypes.put(DrawType.SPRITE_ONLY, ParameterRenderer::getSpriteDrawable);
        drawTypes.put(DrawType.STACK_ONLY, p -> getStackDrawable(p, false));
        drawTypes.put(DrawType.STACK_ONLY_OR_QUESTION_MARK, p -> getStackDrawable(p, true));
        drawTypes.put(DrawType.SPRITE_STACK, p -> getSpriteDrawable(p).andThen(getStackDrawable(p, false)));
        drawTypes.put(DrawType.STACK_SPRITE, p -> getStackDrawable(p, false).andThen(getSpriteDrawable(p)));
        drawTypes.put(DrawType.SPRITE_STACK_OR_QUESTION_MARK, p -> {
            return getSpriteDrawable(p).andThen(getStackDrawable(p, true));
        });
        drawTypes.put(DrawType.STACK_OR_QUESTION_MARK_THEN_SPRITE, p -> {
            return getStackDrawable(p, true).andThen(getSpriteDrawable(p));
        });
    }

    public static ISimpleDrawable getSpriteDrawable(IStatementParameter param) {
        return (x, y) -> {
            ISprite sprite = param.getSprite();
            if (sprite != null) {
                GuiIcon.drawAt(sprite, x + 1, y + 1, 16);
            }
        };
    }

    public static ISimpleDrawable getStackDrawable(IStatementParameter param, boolean orQuestionMark) {
        return (x, y) -> {
            ItemStack stack = param.getItemStack();
            if (!stack.isEmpty()) {
                GuiUtil.drawItemStackAt(stack, (int) x + 1, (int) y + 1);
            } else if (orQuestionMark) {
                GuiElementStatement.ICON_SLOT_NOT_SET.drawAt(x + 1, y + 1);
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

    public static void draw(IStatementParameter param, double x, double y) {
        getDrawable(param).drawAt(x, y);
    }
}
