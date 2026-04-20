/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.widget;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.GuiElementSimple;
import ct.buildcraft.lib.gui.IInteractionElement;
import ct.buildcraft.lib.gui.MenuBC_Neptune;
import ct.buildcraft.lib.gui.Widget_Neptune;
import ct.buildcraft.lib.gui.elem.ToolTip;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.misc.GuiUtil;
import ct.buildcraft.lib.misc.StackUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

/** Defines a widget that represents a phantom slot. */
public class WidgetPhantomSlot extends Widget_Neptune<MenuBC_Neptune> {
    private static final byte NET_CLIENT_TO_SERVER_CLICK = 0;
    private static final byte NET_SERVER_TO_CLIENT_ITEM = 0;

    private static final byte CLICK_FLAG_SHIFT = 1;
    private static final byte CLICK_FLAG_SINGLE = 2;
    private static final byte CLICK_FLAG_CLONE = 4;

    @Nonnull
    private ItemStack stack = ItemStack.EMPTY;

    public WidgetPhantomSlot(MenuBC_Neptune container) {
        super(container);
    }

    @Override
    public void handleWidgetDataServer(NetworkEvent.Context ctx, FriendlyByteBuf buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_CLIENT_TO_SERVER_CLICK) {
            byte flags = buffer.readByte();
            tryMouseClick(flags);
        }
    }

    void tryMouseClick(int flags) {
        boolean shift = (flags & CLICK_FLAG_SHIFT) == CLICK_FLAG_SHIFT;
        boolean single = (flags & CLICK_FLAG_SINGLE) == CLICK_FLAG_SINGLE;
        boolean clone = (flags & CLICK_FLAG_CLONE) == CLICK_FLAG_CLONE;
        if (clone) {
            if (container.playerInventory.player.isCreative()) {
                ItemStack get = getStack();
                if (!get.isEmpty() && container.getCarried().isEmpty()) {
                    container.setCarried(get.copy());
                }
            }
        } else if (shift) {
            setStack(StackUtil.EMPTY, true);
        } else {
            ItemStack toSet = container.getCarried();
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
    public void handleWidgetDataClient(NetworkEvent.Context ctx, FriendlyByteBuf buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_SERVER_TO_CLIENT_ITEM) {
            stack = buffer.readItem();
            onSetStack();
        }
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
        if (tellClient && !container.playerInventory.player.level.isClientSide) {
            sendWidgetData(buffer -> {
                buffer.writeByte(NET_SERVER_TO_CLIENT_ITEM);
                buffer.writeItem(stack);
            });
        }
        onSetStack();
    }

    protected void onSetStack() {}

    @OnlyIn(Dist.CLIENT)
    public class GuiElementPhantomSlot extends GuiElementSimple implements IInteractionElement {
        private final ToolTip tooltip = GuiUtil.createToolTip(this::getStack);

        public GuiElementPhantomSlot(BuildCraftGui gui, IGuiArea area) {
            super(gui, area);
        }

        @Override
        public void drawForeground(PoseStack pose, float partialTicks) {
           // RenderHelper.enableGUIStandardItemLighting();
            gui.mc.getItemRenderer().renderGuiItem(getStack(), (int) (getX() - gui.rootElement.getX()), (int) (getY() - gui.rootElement.getY()));
            //RenderHelper.disableStandardItemLighting();
            if (contains(gui.mouse) && shouldDrawHighlight()) {
                GuiUtil.drawRect(pose, this, 0x70_FF_FF_FF);
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
                if (Screen.hasShiftDown()) flags |= CLICK_FLAG_SHIFT;
                if (gui.mc.options.keyPickItem.isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(button))) {//TODO check
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
