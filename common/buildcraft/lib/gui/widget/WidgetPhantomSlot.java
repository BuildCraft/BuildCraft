/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.widget;

import buildcraft.api.core.BCLog;
import buildcraft.api.net.IMessage;
import buildcraft.lib.gui.*;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/** Defines a widget that represents a phantom slot. */
public class WidgetPhantomSlot extends Widget_Neptune<ContainerBC_Neptune> {
    private static final byte NET_CLIENT_TO_SERVER_CLICK = 0;
    private static final byte NET_CLIENT_TO_SERVER_STACK = 1; // Calen 1.20.1
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
    public IMessage handleWidgetDataServer(NetworkEvent.Context ctx, PacketBufferBC buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_CLIENT_TO_SERVER_CLICK) {
            byte flags = buffer.readByte();
            tryMouseClick(flags);
        } else if (id == NET_CLIENT_TO_SERVER_STACK) {
            ItemStack stack = buffer.readItem();
            setStack(stack, true);
        }
        return null;
    }

    void tryMouseClick(int flags) {
        boolean shift = (flags & CLICK_FLAG_SHIFT) == CLICK_FLAG_SHIFT;
        boolean single = (flags & CLICK_FLAG_SINGLE) == CLICK_FLAG_SINGLE;
        boolean clone = (flags & CLICK_FLAG_CLONE) == CLICK_FLAG_CLONE;
        if (clone) {
            if (container.player.isCreative()) {
                ItemStack get = getStack();
//                if (!get.isEmpty() && container.player.inventory.getItemStack().isEmpty())
                if (!get.isEmpty() && container.player.containerMenu.getCarried().isEmpty()) {
//                    container.player.inventory.setItemStack(get.copy());
                    container.player.containerMenu.setCarried(get.copy());
                }
            }
        } else if (shift) {
            setStack(StackUtil.EMPTY, true);
        } else {
//            ItemStack toSet = container.player.inventory.getItemStack();
            ItemStack toSet = container.player.containerMenu.getCarried();
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
    public IMessage handleWidgetDataClient(NetworkEvent.Context ctx, PacketBufferBC buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_SERVER_TO_CLIENT_ITEM) {
            stack = StackUtil.asNonNull(buffer.readItem());
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
        if (tellClient && !container.player.level().isClientSide) {
            sendWidgetData(buffer ->
            {
                buffer.writeByte(NET_SERVER_TO_CLIENT_ITEM);
                buffer.writeItemStack(stack, false);
            });
        }
        onSetStack();
    }

    protected void onSetStack() {
    }

    // Calen 1.20.1
    public void clientSetStackToServer(ItemStack stack) {
        this.sendWidgetData(buffer ->
        {
            buffer.writeByte(NET_CLIENT_TO_SERVER_STACK);
            buffer.writeItem(stack);
        });
    }

    @OnlyIn(Dist.CLIENT)
    public class GuiElementPhantomSlot extends GuiElementSimple implements IInteractionElement {
        private final ToolTip tooltip = GuiUtil.createToolTip(this::getStack);

        public GuiElementPhantomSlot(BuildCraftGui gui, IGuiArea area) {
            super(gui, area);
        }

        @Override
        public void drawForeground(GuiGraphics guiGraphics, float partialTicks) {
//            RenderHelper.enableGUIStandardItemLighting();
            RenderUtil.enableGUIStandardItemLighting();
//            gui.mc.getRenderItem().renderItemAndEffectIntoGUI(getStack(), (int) getX(), (int) getY());
            guiGraphics.renderFakeItem(getStack(), (int) getX(), (int) getY());
//            RenderHelper.disableStandardItemLighting();
            RenderUtil.disableStandardItemLighting();
            if (contains(gui.mouse) && shouldDrawHighlight()) {
                GuiUtil.drawRect(guiGraphics, this, 0x70_FF_FF_FF);
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
//                if (GuiScreen.isShiftKeyDown()) flags |= CLICK_FLAG_SHIFT;
                if (Screen.hasShiftDown()) {
                    flags |= CLICK_FLAG_SHIFT;
                }
                // Calen: should not -100 in 1.18.2
//                if (gui.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(button - 100))
                if (gui.mc.options.keyPickItem.isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(button))) {
                    flags |= CLICK_FLAG_CLONE;
                    BCLog.logger.info("clone");
                }
                final byte writtenFlags = flags;
                // Pretend what we did was right
                WidgetPhantomSlot.this.tryMouseClick(flags);
                // Tell the server what we just did so we can get confirmation that it was right
                WidgetPhantomSlot.this.sendWidgetData(buffer ->
                {
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
