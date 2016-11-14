package buildcraft.transport.gui;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.core.BCCoreStatements;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.ITooltipElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.IPositionedElement;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.transport.container.ContainerGate;
import buildcraft.transport.gate.GateLogic;
import buildcraft.transport.gate.GateVariant;
import buildcraft.transport.gate.StatementWrapper;
import buildcraft.transport.gate.TriggerWrapper;

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

    public static final GuiIcon SLOT_COLOUR = new GuiIcon(TEXTURE_GATE, 176, 72, 18, 18);

    private final IPositionedElement[] positionSlotPair, positionConnect;

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
            positionSlotPair[i] = new GuiRectangle(x, y, columWidth, 18);

            boolean nextOtherColum = split && i + 1 >= container.slotHeight;
            if (otherColumn == nextOtherColum && i < numSlots - 1) {
                positionConnect[i] = new GuiRectangle(x + triggerWidth, y + 9, 18, 18);
            }
        }

        for (int i = 0; i < numSlots; i++) {
            positionSlotPair[i] = positionSlotPair[i].offset(rootElement);
            if (positionConnect[i] != null) {
                positionConnect[i] = positionConnect[i].offset(rootElement);
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
        int actionArgs = gate.variant.numActionArgs;

        for (int i = 0; i < positionSlotPair.length; i++) {
            int xOff = 18 * (triggerArgs + 1);
            IPositionedElement elemSlots = positionSlotPair[i];
            IPositionedElement elemActionSlots = elemSlots.offset(xOff + 18, 0);

            SLOT[triggerArgs].drawAt(elemSlots);
            SLOT[actionArgs].drawAt(elemActionSlots);

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
            drawStatementSlots(gate.triggers[i], gate.triggerParameters[i], elemSlots);
            drawStatementSlots(gate.actions[i], gate.actionParameters[i], elemActionSlots);
        }
    }

    private void drawStatementSlots(StatementWrapper statement, IStatementParameter[] params, IPositionedElement offset) {
        int allowedParams = 0;
        if (statement != null) {
            allowedParams = statement.maxParameters();

            EnumPipePart part = statement.sourcePart;
            int yOffset = (part.getIndex() + 1) % 7;
            SLOT_COLOUR.offset(0, yOffset * 18).drawAt(offset);

            TextureAtlasSprite sprite = statement.getGuiSprite();
            if (sprite != null) {
                SpriteUtil.bindBlockTextureMap();
                drawTexturedModalRect(offset.getX() + 1, offset.getY() + 1, sprite, 16, 16);
            }
        }
        for (int p = 0; p < params.length; p++) {
            IGuiPosition pos = offset.offset(18 * (p + 1), 0);
            if (p >= allowedParams) {
                ICON_SLOT_BLOCKED.drawAt(pos);
            } else {
                IStatementParameter param = params[p];
                if (param == null) {
                    ICON_SLOT_NOT_SET.drawAt(pos);
                } else {
                    int x = pos.getX();
                    int y = pos.getY();

                    TextureAtlasSprite sprite = param.getGuiSprite();
                    if (sprite != null) {
                        SpriteUtil.bindBlockTextureMap();
                        drawTexturedModalRect(x + 1, y + 1, sprite, 16, 16);
                    }

                    ItemStack stack = param.getItemStack();
                    if (StackUtil.isValid(stack)) {
                        this.drawItemStackAt(stack, x + 1, y + 1);
                    }

                    if (sprite == null && StackUtil.isInvalid(stack)) {
                        ICON_SLOT_NOT_SET.drawAt(pos);
                    }
                }
            }
        }
    }

    @Override
    protected void drawForegroundLayer() {
        fontRendererObj.drawString(StringUtilBC.localize(this.container.gate.variant.getVariantName()), 8, 10, 0x404040);
        fontRendererObj.drawString(StringUtilBC.localize("gui.inventory"), 8, ySize - 97, 0x404040);
    }

    // Tooltips

    @Override
    public void addToolTips(List<ToolTip> tooltips) {

        // Test for triggers, actions or params

        for (int i = 0; i < positionSlotPair.length; i++) {
            IPositionedElement pos = positionSlotPair[i];
            if (pos.contains(mouse)) {
                GuiRectangle rect = new GuiRectangle(pos.getX() + 1, pos.getY() + 1, 16, 16);
                if (rect.contains(mouse)) {
                    TriggerWrapper trigger = container.gate.triggers[i];
                    if (trigger != null) {
                        tooltips.add(new ToolTip(trigger.getDescription()));
                    }
                    return;
                }
                int params = container.gate.triggerParameters[i].length;
                for (int p = 0; p < params; p++) {
                    IStatementParameter param = container.gate.triggerParameters[i][p];
                    if (param != null && rect.offset(18 * (p + 1), 0).contains(mouse)) {
                        tooltips.add(new ToolTip(param.getDescription()));
                        return;
                    }
                }
            }
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
            }
        }

        // Test for changing statements from the small popup

        for (int i = 0; i < positionSlotPair.length; i++) {
            IPositionedElement pos = positionSlotPair[i];
            if (pos.contains(mouse)) {
                container.gate.triggers[i] = new TriggerWrapper.TriggerWrapperExternal(BCCoreStatements.TRIGGER_MACHINE_ACTIVE, EnumFacing.DOWN);
            }
        }

        // Test for dragging statements from the big contexts
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        // Test for changing statements from the small popup

        // Test for dragging statements from the big contexts
    }
}
