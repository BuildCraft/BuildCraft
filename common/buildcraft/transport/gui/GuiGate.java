package buildcraft.transport.gui;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.client.sprite.RawSprite;
import buildcraft.lib.client.sprite.SpriteSplit;
import buildcraft.lib.gui.*;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IPositionedElement;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.transport.container.ContainerGate;
import buildcraft.transport.gate.*;

public class GuiGate extends GuiBC8<ContainerGate> implements ITooltipElement {

    public static final ResourceLocation TEXTURE_GATE = new ResourceLocation("buildcrafttransport:textures/gui/gate_interface.png");

    public static final int GUI_WIDTH = 176;
    public static final int GUI_HEIGHT_NORM = 117;

    public static final GuiIcon ICON_BACK_TOP = new GuiIcon(TEXTURE_GATE, 0, 0, GUI_WIDTH, 16);
    public static final GuiIcon ICON_BACK_MID = new GuiIcon(TEXTURE_GATE, 0, 23, GUI_WIDTH, 18);
    public static final GuiIcon ICON_BACK_BOTTOM = new GuiIcon(TEXTURE_GATE, 0, 48, GUI_WIDTH, 101);

    public static final GuiIcon ICON_SLOT_BLOCKED = new GuiIcon(TEXTURE_GATE, 176, 0, 18, 18);
    public static final GuiIcon ICON_SLOT_NOT_SET = ICON_SLOT_BLOCKED.offset(18, 0);

    public static final GuiIcon CONNECT_HORIZ_OFF = new GuiIcon(TEXTURE_GATE, 176, 18, 18, 18);
    public static final GuiIcon CONNECT_HORIZ_ON_TRIGGER = new GuiIcon(TEXTURE_GATE, 194, 18, 7, 18);
    public static final GuiIcon CONNECT_HORIZ_ON_ACTION = new GuiIcon(TEXTURE_GATE, 201, 18, 11, 18);
    public static final GuiIcon CONNECT_HORIZ_ON_FULL = CONNECT_HORIZ_OFF.offset(18, 0);

    public static final GuiIcon CONNECT_VERT_TOP_OFF = new GuiIcon(TEXTURE_GATE, 176, 36, 18, 9);
    public static final GuiIcon CONNECT_VERT_TOP_ON = CONNECT_VERT_TOP_OFF.offset(18, 0);

    public static final GuiIcon CONNECT_VERT_BOTTOM_OFF = CONNECT_VERT_TOP_OFF.offset(0, 9);
    public static final GuiIcon CONNECT_VERT_BOTTOM_ON = CONNECT_VERT_TOP_ON.offset(0, 9);

    public static final GuiIcon CONNECT_VERT_OFF = new GuiIcon(TEXTURE_GATE, 176, 54, 18, 18);
    public static final GuiIcon CONNECT_VERT_ON = CONNECT_VERT_OFF.offset(18, 0);

    public static final GuiIcon SLOT_0_PARAM = new GuiIcon(TEXTURE_GATE, 176, 72, 18 * 1, 18);
    public static final GuiIcon SLOT_1_PARAM = new GuiIcon(TEXTURE_GATE, 176, 72, 18 * 2, 18);
    public static final GuiIcon SLOT_2_PARAM = new GuiIcon(TEXTURE_GATE, 176, 72, 18 * 3, 18);
    public static final GuiIcon SLOT_3_PARAM = new GuiIcon(TEXTURE_GATE, 176, 72, 18 * 4, 18);
    public static final GuiIcon[] SLOT = { SLOT_0_PARAM, SLOT_1_PARAM, SLOT_2_PARAM, SLOT_3_PARAM };

    public static final RawSprite ICON_SELECT_HOVER = new RawSprite(TEXTURE_GATE, 212, 0, 16, 16, 256);
    public static final SpriteSplit SELECTION_HOVER = new SpriteSplit(ICON_SELECT_HOVER, 3, 3, 13, 13, 16);

    public static final GuiIcon SLOT_COLOUR = new GuiIcon(TEXTURE_GATE, 176, 72, 18, 18);

    public ElementStatement<?> currentHover = null;

    private final IPositionedElement[] positionSlotPair, positionConnect;

    public boolean isDraggingBig;
    public TriggerWrapper draggingTrigger;
    public ActionWrapper draggingAction;

    public GuiGate(ContainerGate container) {
        super(container);

        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT_NORM + 18 * container.slotHeight;

        boolean split = container.gate.isSplitInTwo();
        GateVariant variant = container.gate.variant;
        int triggerWidth = 18 * (variant.numTriggerArgs + 1);
        int columWidth = 18 * 2 + triggerWidth + 18 * variant.numActionArgs;

        int columnStartFirst = split ? (GUI_WIDTH - 18) / 2 - columWidth : (GUI_WIDTH - columWidth) / 2;
        int columnStartSecond = columnStartFirst + columWidth + 18;

        int numSlots = variant.numSlots;
        positionSlotPair = new IPositionedElement[numSlots];
        positionConnect = new IPositionedElement[numSlots];

        for (int i = 0; i < numSlots; i++) {
            boolean otherColumn = split && i >= container.slotHeight;
            int x = otherColumn ? columnStartSecond : columnStartFirst;
            int y = (otherColumn ? i - container.slotHeight : i) * 18 + 16;
            positionSlotPair[i] = new GuiRectangle(x, y, columWidth, 18).offset(rootElement);

            boolean nextOtherColum = split && i + 1 >= container.slotHeight;
            if (otherColumn == nextOtherColum && i < numSlots - 1) {
                positionConnect[i] = new GuiRectangle(x + triggerWidth, y + 9, 18, 18).offset(rootElement);
            }
        }

        // Ask the server for all the valid statements
        MessageUtil.doDelayed(() -> container.sendMessage((buffer) -> {
            buffer.writeByte(ContainerGate.ID_VALID_STATEMENTS);
        }));
    }

    @Override
    public void initGui() {
        super.initGui();

        GateVariant variant = container.gate.variant;
        int numSlots = variant.numSlots;

        for (int i = 0; i < numSlots; i++) {
            IPositionedElement actionPos = positionSlotPair[i].offset(18 * (2 + variant.numTriggerArgs), 0);
            ElementTrigger trigger = new ElementTrigger(this, positionSlotPair[i].resize(18, 18), container.pairs[i].trigger);
            ElementAction action = new ElementAction(this, actionPos.resize(18, 18), container.pairs[i].action);
            guiElements.add(trigger);
            guiElements.add(action);
            for (int p = 0; p < variant.numTriggerArgs; p++) {
                IPositionedElement pos = positionSlotPair[i].offset(18 * (p + 1), 0).resize(18, 18);
                guiElements.add(new ElementStatementParam(this, pos, container.pairs[i].triggerParams[p], p, trigger));
            }
            for (int p = 0; p < variant.numActionArgs; p++) {
                IPositionedElement pos = actionPos.offset(18 * (p + 1), 0).resize(18, 18);
                guiElements.add(new ElementStatementParam(this, pos, container.pairs[i].actionParams[p], p, action));
            }
        }
    }

    // Drawing

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_BACK_TOP.drawAt(rootElement);
        int y = 16;
        for (int i = 0; i < container.slotHeight; i++) {
            ICON_BACK_MID.drawAt(rootElement.offset(0, y));
            y += 18;
        }
        ICON_BACK_BOTTOM.drawAt(rootElement.offset(0, y));

        GateLogic gate = container.gate;
        int triggerArgs = gate.variant.numTriggerArgs;

        for (int i = 0; i < positionSlotPair.length; i++) {
            int xOff = 18 * (triggerArgs + 1);
            IPositionedElement elemSlots = positionSlotPair[i];

            IPositionedElement offsetConnect = elemSlots.offset(xOff, 0);

            boolean triggerOn = gate.triggerOn[i];
            boolean actionOn = gate.actionOn[i];

            if (triggerOn) {
                if (actionOn) {
                    CONNECT_HORIZ_ON_FULL.drawAt(offsetConnect);
                } else {
                    CONNECT_HORIZ_OFF.drawAt(offsetConnect);
                    CONNECT_HORIZ_ON_TRIGGER.drawAt(offsetConnect);
                }
            } else if (actionOn) {
                CONNECT_HORIZ_OFF.drawAt(offsetConnect);
                CONNECT_HORIZ_ON_ACTION.drawAt(offsetConnect.getX() + 7, offsetConnect.getY());
            } else {
                CONNECT_HORIZ_OFF.drawAt(offsetConnect);
            }

            IPositionedElement elemConnect = positionConnect[i];
            if (elemConnect != null) {
                boolean twoConnected = gate.connections[i];
                boolean bottomOn = gate.actionOn[i + 1];
                if (twoConnected) {
                    GuiIcon icon = actionOn ? CONNECT_VERT_ON : CONNECT_VERT_OFF;
                    icon.drawAt(elemConnect);
                } else {
                    GuiIcon icon = actionOn ? CONNECT_VERT_TOP_ON : CONNECT_VERT_TOP_OFF;
                    icon.drawAt(elemConnect);
                    icon = bottomOn ? CONNECT_VERT_BOTTOM_ON : CONNECT_VERT_BOTTOM_OFF;
                    icon.drawAt(elemConnect.offset(0, 9));
                }
            }
        }
        iteratePossible((wrapper, pos) -> {
            ElementStatement.draw(this, wrapper, pos);
        });
    }

    @Override
    protected void drawForegroundLayer() {
        int x = rootElement.getX();
        int y = rootElement.getY();
        String localizedName = container.gate.variant.getLocalizedName();
        int cX = x + (GUI_WIDTH - fontRendererObj.getStringWidth(localizedName)) / 2;
        fontRendererObj.drawString(localizedName, cX, y + 5, 0x404040);
        fontRendererObj.drawString(StringUtilBC.localize("gui.inventory"), x + 8, y + ySize - 97, 0x404040);
        GlStateManager.color(1, 1, 1);

        if (currentHover != null) {
            drawGradientRect(rootElement.getX(), rootElement.getY(), rootElement.getX() + GUI_WIDTH, rootElement.getY() + ySize, 0x55_00_00_00, 0x55_00_00_00);
        }

        if (isDraggingBig) {
            StatementWrapper wrapper = draggingTrigger == null ? draggingAction : draggingTrigger;
            ElementStatement.draw(this, wrapper, mouse.offset(-9, -9));
        }
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        iteratePossible((wrapper, pos) -> {
            if (pos.contains(mouse) && wrapper != null) {
                String[] arr = { wrapper.getDescription() };
                EnumFacing face = wrapper.sourcePart.face;
                if (face != null) {
                    String translated = ColourUtil.getTextFullTooltip(face);
                    translated = StringUtilBC.localize("gate.side", translated);
                    arr = new String[] { arr[0], translated };
                }
                tooltips.add(new ToolTip(arr));
            }
        });
    }

    private interface OnStatement {
        void iterate(StatementWrapper wrapper, IPositionedElement pos);
    }

    private void iteratePossible(OnStatement onStatemenet) {
        int tx = 0;
        int ty = 0;
        EnumPipePart last = null;
        onStatemenet.iterate(null, rootElement.offset(-18, 8).resize(18, 18));
        for (TriggerWrapper wrapper : container.possibleTriggers) {
            tx++;
            if (tx > 5 || (last != null && last != wrapper.sourcePart)) {
                tx = 0;
                ty++;
            }
            onStatemenet.iterate(wrapper, rootElement.offset(18 * (-1 - tx), ty * 18 + 8).resize(18, 18));
            last = wrapper.sourcePart;
        }

        tx = 0;
        ty = 0;
        last = null;
        onStatemenet.iterate(null, rootElement.offset(GUI_WIDTH, 8).resize(18, 18));
        for (ActionWrapper wrapper : container.possibleActions) {
            tx++;
            if (tx > 5 || (last != null && last != wrapper.sourcePart)) {
                tx = 0;
                ty++;
            }
            onStatemenet.iterate(wrapper, rootElement.offset(GUI_WIDTH + 18 * tx, ty * 18 + 8).resize(18, 18));
            last = wrapper.sourcePart;
        }
    }

    // Mouse logic

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Test for connection changes
        for (int i = 0; i < positionConnect.length; i++) {
            IPositionedElement pos = positionConnect[i];
            if (pos != null && pos.contains(mouse)) {
                container.setConnected(i, !container.gate.connections[i]);
                return;
            }
        }

        // Test for dragging statements from the side contexts
        iteratePossible((wrapper, pos) -> {
            if (pos.contains(mouse)) {
                isDraggingBig = true;
                if (wrapper instanceof TriggerWrapper) {
                    draggingTrigger = (TriggerWrapper) wrapper;
                } else if (wrapper instanceof ActionWrapper) {
                    draggingAction = (ActionWrapper) wrapper;
                }
            }
        });
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        // Test for dragging statements from the side contexts

        if (isDraggingBig) {

            // Is our location valid?
            for (IGuiElement elem : guiElements) {
                if (elem instanceof ElementStatement<?>) {
                    ElementStatement<?> element = (ElementStatement<?>) elem;
                    if (element.contains(mouse)) {
                        if (element instanceof ElementTrigger && draggingAction == null) {
                            ((ElementTrigger) element).reference.set(draggingTrigger);
                        } else if (element instanceof ElementAction && draggingTrigger == null) {
                            ((ElementAction) element).reference.set(draggingAction);
                        }
                        break;
                    }
                }
            }
            isDraggingBig = false;
            draggingTrigger = null;
            draggingAction = null;
        }
    }
}
