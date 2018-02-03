/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.widget;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.Widget_Neptune;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;

/** Defines a widget that represents a phantom slot. */
public class WidgetPhantomSlot extends Widget_Neptune<ContainerBC_Neptune> {
    private static final byte NET_CLIENT_TO_SERVER_CLICK = 0;
    private static final byte NET_SERVER_TO_CLIENT_ITEM = 0;

    private static final byte CLICK_FLAG_SHIFT = 1;
    private static final byte CLICK_FLAG_SINGLE = 2;
    private static final byte CLICK_FLAG_CLONE = 4;

    @Nonnull
    private ItemStack stack = StackUtil.EMPTY;

    public WidgetPhantomSlot(ContainerBC_Neptune container) {
        super(container);
    }

    @Override
    public IMessage handleWidgetDataServer(MessageContext ctx, PacketBufferBC buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_CLIENT_TO_SERVER_CLICK) {
            byte flags = buffer.readByte();
            tryMouseClick(flags);
        }
        return null;
    }

    void tryMouseClick(int flags) {
        boolean shift = (flags & CLICK_FLAG_SHIFT) == CLICK_FLAG_SHIFT;
        boolean single = (flags & CLICK_FLAG_SINGLE) == CLICK_FLAG_SINGLE;
        boolean clone = (flags & CLICK_FLAG_CLONE) == CLICK_FLAG_CLONE;
        if (clone) {
            if (container.player.capabilities.isCreativeMode) {
                ItemStack get = getStack();
                if (!get.isEmpty() && container.player.inventory.getItemStack().isEmpty()) {
                    container.player.inventory.setItemStack(get.copy());
                }
            }
        } else if (shift) {
            setStack(StackUtil.EMPTY, true);
        } else {
            ItemStack toSet = container.player.inventory.getItemStack();
            if (toSet.isEmpty()) {
                setStack(StackUtil.EMPTY, true);
            } else {
                toSet = toSet.copy();
                if (single) {
                    toSet.setCount(1);
                }
                setStack(toSet, true);
            }
        }
    }

    @Override
    public IMessage handleWidgetDataClient(MessageContext ctx, PacketBufferBC buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_SERVER_TO_CLIENT_ITEM) {
            stack = StackUtil.asNonNull(buffer.readItemStack());
            onSetStack();
        }
        return null;
    }

    protected int getMaxStackSize(ItemStack stack) {
        return stack.getMaxStackSize();
    }

    @Nonnull
    public ItemStack getStack() {
        return stack;
    }

    public final void setStack(@Nonnull ItemStack stack, boolean tellClient) {
        this.stack = StackUtil.asNonNull(stack);
        int max = getMaxStackSize(stack);
        if (stack.getCount() > max) {
            this.stack.setCount(max);
        }
        if (tellClient && !container.player.world.isRemote) {
            sendWidgetData(buffer -> {
                buffer.writeByte(NET_SERVER_TO_CLIENT_ITEM);
                buffer.writeItemStack(stack);
            });
        }
        onSetStack();
    }

    protected void onSetStack() {}

    @SideOnly(Side.CLIENT)
    public class GuiElementPhantomSlot extends GuiElementSimple implements IInteractionElement {
        private final ToolTip tooltip = GuiUtil.createToolTip(this::getStack);

        public GuiElementPhantomSlot(BuildCraftGui gui, IGuiArea area) {
            super(gui, area);
        }

        @Override
        public void drawForeground(float partialTicks) {
            RenderHelper.enableGUIStandardItemLighting();
            gui.mc.getRenderItem().renderItemAndEffectIntoGUI(getStack(), (int) getX(), (int) getY());
            RenderHelper.disableStandardItemLighting();
            if (contains(gui.mouse) && shouldDrawHighlight()) {
                GuiUtil.drawRect(this, 0x70_FF_FF_FF);
            }
        }

        protected boolean shouldDrawHighlight() {
            return true;
        }

        @Nonnull
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
        public void addToolTips(List<ToolTip> tooltips) {
            if (contains(gui.mouse) && !getStack().isEmpty()) {
                tooltips.add(tooltip);
                tooltip.refresh();
            }
        }
    }
}
