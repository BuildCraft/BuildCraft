package buildcraft.lib.gui.statement;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.statements.IStatement;

import buildcraft.lib.BCLibSprites;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.json.GuiJson;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.statement.FullStatement;

public class GuiElementStatement<S extends IStatement> extends GuiElementSimple<GuiJson<?>>
    implements IInteractionElement, IReference<S> {

    public static final ResourceLocation TEXTURE_SELECTOR;

    public static final GuiIcon SLOT_COLOUR;
    public static final GuiIcon ICON_SLOT_BLOCKED;
    public static final GuiIcon ICON_SLOT_NOT_SET;

    public static final SpriteRaw ICON_SELECT_HOVER;
    public static final SpriteNineSliced SELECTION_HOVER;

    static {
        TEXTURE_SELECTOR = new ResourceLocation("buildcraftlib:textures/gui/statement_selector.png");
        SLOT_COLOUR = new GuiIcon(TEXTURE_SELECTOR, 0, 0, 18, 18);
        ICON_SLOT_BLOCKED = SLOT_COLOUR.offset(18, 0);
        ICON_SLOT_NOT_SET = ICON_SLOT_BLOCKED.offset(18, 0);
        ICON_SELECT_HOVER = new SpriteRaw(TEXTURE_SELECTOR, 18, 18, 36, 36, 256);
        SELECTION_HOVER = new SpriteNineSliced(ICON_SELECT_HOVER, 8, 8, 28, 28, 36);
    }

    private final FullStatement<S> ref;
    private final boolean draw;

    public GuiElementStatement(GuiJson<?> gui, IGuiArea element, FullStatement<S> ref, boolean draw) {
        super(gui, element);
        this.ref = ref;
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
    public boolean canSet(Object value) {
        return ref.canSet(value);
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
    public void drawBackground(float partialTicks) {
        if (draw) {
            S stmnt = ref.get();
            double x = getX();
            double y = getY();
            GuiElementStatementSource.drawGuiSlot(stmnt, x, y);
            if (!ref.canInteract) {
                GuiIcon.drawAt(BCLibSprites.LOCK, x + 1, y + 1, 16);
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
            if (GuiScreen.isShiftKeyDown()) {
                set(null);
                return;
            }
            S s = get();
            if (s == null) {
                return;
            }
            IStatement[] possible = s.getPossible();
            if (!s.isPossibleOrdered()) {
                List<IStatement> list = new ArrayList<>();
                list.add(null);
                for (IStatement p2 : possible) {
                    if (p2 != null) {
                        list.add(p2);
                    }
                }
                possible = list.toArray(new IStatement[0]);
            }
            gui.currentMenu = GuiElementStatementVariant.create(this, this, possible);
        }
    }
}
