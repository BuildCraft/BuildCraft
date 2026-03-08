package buildcraft.lib.gui.statement;

import buildcraft.api.statements.IStatement;
import buildcraft.lib.BCLibSprites;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.statement.FullStatement;
import buildcraft.lib.statement.StatementContext;
import buildcraft.lib.statement.StatementContext.StatementGroup;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiElementStatement<S extends IStatement> extends GuiElementSimple
        implements IInteractionElement, IReference<S> {

    public static final ResourceLocation TEXTURE_SELECTOR;

    public static final GuiIcon SLOT_COLOUR;
    public static final GuiIcon ICON_SLOT_BLOCKED;
    public static final GuiIcon ICON_SLOT_NOT_SET;

    public static final SpriteRaw ICON_SELECT_HOVER;
    public static final SpriteNineSliced SELECTION_HOVER;

    static {
        TEXTURE_SELECTOR = new ResourceLocation("buildcraftlib:textures/gui/misc_slots.png");
        SLOT_COLOUR = new GuiIcon(TEXTURE_SELECTOR, 0, 0, 18, 18);
        ICON_SLOT_BLOCKED = SLOT_COLOUR.offset(18, 0);
        ICON_SLOT_NOT_SET = ICON_SLOT_BLOCKED.offset(18, 0);
        ICON_SELECT_HOVER = new SpriteRaw(TEXTURE_SELECTOR, 18, 18, 36, 36, 256);
        SELECTION_HOVER = new SpriteNineSliced(ICON_SELECT_HOVER, 8, 8, 28, 28, 36);
    }

    private final FullStatement<S> ref;
    private final StatementContext<?> ctx;
    private final boolean draw;

    public GuiElementStatement(BuildCraftGui gui, IGuiArea element, FullStatement<S> ref, StatementContext<?> ctx,
                               boolean draw) {
        super(gui, element);
        this.ref = ref;
        this.ctx = ctx;
        this.draw = draw;
    }

    // IReference

    @Override
    public S get() {
        return ref.get();
    }

    @Override
    public void set(S to) {
        ref.set(to);
        ref.postSetFromGui(-1);
    }

    @Override
    public boolean canSet(S value) {
        return ref.canSet(value);
    }

    @Override
    public S convertToType(Object value) {
        return ref.convertToType(value);
    }

    @Override
    public Class<S> getHeldType() {
        return ref.getHeldType();
    }

    // ITooltipElement

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (contains(gui.mouse)) {
            S s = get();
            if (s != null) {
                tooltips.add(new ToolTip(s.getTooltip()));
            }
        }
    }

    // IGuiElement

    @Override
    public void drawBackground(float partialTicks, MatrixStack poseStack) {
        if (draw) {
            S statement = ref.get();
            double x = getX();
            double y = getY();
            GuiElementStatementSource.drawGuiSlot(statement, poseStack, x, y);
            if (!ref.canInteract) {
                GuiIcon.drawAt(BCLibSprites.LOCK, poseStack, x + 1, y + 1, 16);
            }
        }
    }

    // IInteractionElement

    @Override
    public void onMouseClicked(int button) {
        if (!contains(gui.mouse)) {
            return;
        }
        if (ref.canInteract && button == 0) {
            if (Screen.hasShiftDown()) {
                set(null);
                return;
            }
            S s = get();
            if (s == null) {
                return;
            }
            List<IStatement> possible = new ArrayList<>();
            Collections.addAll(possible, s.getPossible());
            if (!s.isPossibleOrdered()) {
                List<IStatement> list = new ArrayList<>();
                list.add(null);
                for (IStatement p2 : possible) {
                    if (p2 != null) {
                        list.add(p2);
                    }
                }
                possible = list;
            }
            if (ctx != null) {
                possible.removeIf(f ->
                {
                    for (StatementGroup<?> group : ctx.getAllPossible()) {
                        if (group.getValues().contains(f)) {
                            return false;
                        }
                    }
                    return true;
                });
            }
            gui.currentMenu = GuiElementStatementVariant.create(gui, this, this, possible.toArray(new IStatement[0]));
        }
    }
}
