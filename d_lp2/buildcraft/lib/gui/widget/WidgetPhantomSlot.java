package buildcraft.lib.gui.widget;

import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.gui.tooltips.ToolTip;
import buildcraft.lib.gui.*;
import buildcraft.lib.misc.GuiUtil;

/** Defines a widget that represents a phantom slot. */
public class WidgetPhantomSlot extends Widget_BC8<ContainerBC8> {
    private static final byte NET_CLIENT_TO_SERVER_CLICK = 0;
    private static final byte NET_SERVER_TO_CLIENT_ITEM = 0;

    private static final byte CLICK_FLAG_SHIFT = 1;
    private static final byte CLICK_FLAG_SINGLE = 2;
    private static final byte CLICK_FLAG_CLONE = 4;

    private ItemStack stack;

    public WidgetPhantomSlot(ContainerBC8 container) {
        super(container);
    }

    @Override
    public IMessage handleWidgetDataServer(PacketBuffer buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_CLIENT_TO_SERVER_CLICK) {
            byte flags = buffer.readByte();
            tryMouseClick(flags);
        }
        return null;
    }

    private void tryMouseClick(int flags) {
        boolean shift = (flags & CLICK_FLAG_SHIFT) == CLICK_FLAG_SHIFT;
        boolean single = (flags & CLICK_FLAG_SINGLE) == CLICK_FLAG_SINGLE;
        boolean clone = (flags & CLICK_FLAG_CLONE) == CLICK_FLAG_CLONE;
        if (clone) {
            if (container.player.capabilities.isCreativeMode) {
                ItemStack get = getStack();
                if (get != null && container.player.inventory.getItemStack() == null) {
                    container.player.inventory.setItemStack(get.copy());
                }
            }
        } else {
            ItemStack toSet = null;
            if (!shift) {
                toSet = container.player.inventory.getItemStack();
                if (toSet != null) {
                    toSet = toSet.copy();
                    if (single) {
                        toSet.stackSize = 1;
                    }
                }
            }
            setStack(toSet, true);
        }
    }

    @Override
    public IMessage handleWidgetDataClient(PacketBuffer buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_SERVER_TO_CLIENT_ITEM) {
            stack = buffer.readItemStackFromBuffer();
            onSetStack();
        }
        return null;
    }

    protected int getMaxStackSize(ItemStack stack) {
        return stack.getMaxStackSize();
    }

    public ItemStack getStack() {
        return stack;
    }

    public final void setStack(ItemStack stack, boolean tellClient) {
        if (stack == null) {
            this.stack = stack;
        } else {
            this.stack = stack;
            int max = getMaxStackSize(stack);
            if (stack.stackSize > max) {
                this.stack.stackSize = max;
            }
        }
        if (tellClient && !container.player.worldObj.isRemote) {
            sendWidgetData(buffer -> {
                buffer.writeByte(NET_SERVER_TO_CLIENT_ITEM);
                buffer.writeItemStackToBuffer(stack);
            });
        }
        onSetStack();
    }

    protected void onSetStack() {}

    @SideOnly(Side.CLIENT)
    public class GuiElementPhantomSlot<G extends GuiBC8<C>, C extends ContainerBC8> extends GuiElementSimple<G, C> {
        private final ToolTip tooltip = GuiUtil.createToolTip(gui, this::getStack);

        public GuiElementPhantomSlot(G gui, IPositionedElement parent, GuiRectangle position) {
            super(gui, parent, position);
        }

        @Override
        public void drawForeground() {
            RenderHelper.enableGUIStandardItemLighting();
            gui.mc.getRenderItem().renderItemAndEffectIntoGUI(getStack(), getX(), getY());
            RenderHelper.disableStandardItemLighting();
            if (contains(gui.mouse) && shouldDrawHighlight()) {
                gui.drawGradientRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x70_FF_FF_FF, 0x70_FF_FF_FF);
            }
        }

        protected boolean shouldDrawHighlight() {
            return true;
        }

        public ItemStack getStack() {
            return WidgetPhantomSlot.this.getStack();
        }

        @Override
        public void onMouseClicked(int button) {
            if (contains(gui.mouse)) {
                byte flags = 0;
                if (button == 1) flags |= CLICK_FLAG_SINGLE;
                if (GuiScreen.isShiftKeyDown()) flags |= CLICK_FLAG_SHIFT;
                if (gui.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(button - 100)) {
                    flags |= CLICK_FLAG_CLONE;
                    BCLog.logger.info("clone");
                }
                final byte writtenFlags = flags;
                // Pretend what we did was right
                WidgetPhantomSlot.this.tryMouseClick(flags);
                // Tell the server what we just did so we can get confirmation that it was right
                WidgetPhantomSlot.this.sendWidgetData(buffer -> {
                    buffer.writeByte(NET_CLIENT_TO_SERVER_CLICK);
                    buffer.writeByte(writtenFlags);
                });
            }
        }

        @Override
        public ToolTip getToolTip() {
            if (getStack() != null && contains(gui.mouse)) {
                return tooltip;
            }
            return null;
        }
    }
}
