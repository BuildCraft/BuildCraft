package buildcraft.lib.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.gui.slot.IPhantomSlot;
import buildcraft.lib.net.MessageContainer;
import buildcraft.lib.net.MessageWidget;
import buildcraft.lib.net.command.IPayloadWriter;

public abstract class ContainerBC_Neptune extends Container {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.container");

    public final EntityPlayer player;
    private final List<Widget_Neptune<?>> widgets = new ArrayList<>();

    public ContainerBC_Neptune(EntityPlayer player) {
        this.player = player;
    }

    protected void addFullPlayerInventory(int startX, int startY) {
        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlotToContainer(new Slot(player.inventory, sx + sy * 9 + 9, startX + sx * 18, startY + sy * 18));
            }
        }

        for (int sx = 0; sx < 9; sx++) {
            addSlotToContainer(new Slot(player.inventory, sx, startX + sx * 18, startY + 58));
        }
    }

    protected void addFullPlayerInventory(int startY) {
        addFullPlayerInventory(8, startY);
    }

    protected <W extends Widget_Neptune<?>> W addWidget(W widget) {
        if (widget == null) throw new NullPointerException("widget");
        widgets.add(widget);
        return widget;
    }

    public ImmutableList<Widget_Neptune<?>> getWidgets() {
        return ImmutableList.copyOf(widgets);
    }

    // Package-private so that the widget itself can send this
    void sendWidgetData(Widget_Neptune<?> widget, IPayloadWriter writer) {
        int widgetId = widgets.indexOf(widget);
        if (widgetId == -1) {
            if (DEBUG) {
                throw new IllegalArgumentException("Invalid Widget Request! (" + (widget == null ? "null" : widget.getClass()) + ")");
            } else {
                BCLog.logger.warn("[lib.container] Received an invalid widget sending request!");
                BCLog.logger.warn("[lib.container]   Widget {id = " + widgetId + ", class = " + widget.getClass() + "}");
                BCLog.logger.warn("[lib.container]   Container {class = " + getClass() + "}");
                BCLog.logger.warn("[lib.container]   Player {class = " + player.getClass() + ", name = " + player.getName() + "}");
            }
        } else {
            MessageWidget message = new MessageWidget(windowId, widgetId, writer);
            if (player.worldObj.isRemote) {
                BCMessageHandler.netWrapper.sendToServer(message);
            } else {
                BCMessageHandler.netWrapper.sendTo(message, (EntityPlayerMP) player);
            }
        }
    }

    @Nullable
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        Slot slot = slotId < 0 ? null : this.inventorySlots.get(slotId);
        if (slot == null) {
            return super.slotClick(slotId, dragType, clickType, player);
        }

        ItemStack playerStack = player.inventory.getItemStack();
        if (slot instanceof IPhantomSlot) {
            ItemStack itemStack;
            if (playerStack != null && (slot.getStack() == null || ((IPhantomSlot) slot).canAdjust())) {
                ItemStack copy = playerStack.copy();
                copy.stackSize = 1;
                if (ItemStack.areItemsEqual(copy, slot.getStack()) && ItemStack.areItemStackTagsEqual(copy, slot.getStack())) {
                    copy.stackSize += slot.getStack().stackSize;
                }
                slot.putStack(copy);
            } else {
                slot.putStack(null);
            }
            itemStack = playerStack;
            return itemStack;
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }

    public static ItemStack safeCopy(ItemStack in) {
        return in == null ? null : in.copy();
    }

    public final void sendMessage(IPayloadWriter writer) {
        MessageContainer message = new MessageContainer(windowId, writer);
        if (player.worldObj.isRemote) {
            BCMessageHandler.netWrapper.sendToServer(message);
        } else {
            BCMessageHandler.netWrapper.sendTo(message, (EntityPlayerMP) player);
        }
    }

    public void handleMessage(MessageContext ctx, PacketBuffer payload, Side side) {}

    public final void handleWidgetMessage(MessageContext ctx, int widgetId, PacketBuffer payload, Side side) {
        if (widgetId < 0 || widgetId >= widgets.size()) {
            if (DEBUG) {
                String string = "Received unknown or invalid widget ID " + widgetId + " on side " + side;
                if (side == Side.SERVER) {
                    string += " (for player " + player.getName() + ")";
                }
                BCLog.logger.warn(string);
            }
            return;
        }
        Widget_Neptune<?> widget = widgets.get(widgetId);
        try {
            if (side == Side.SERVER) {
                widget.handleWidgetDataServer(ctx, payload);
            } else if (side == Side.CLIENT) {
                widget.handleWidgetDataClient(ctx, payload);
            }
        } catch (IOException io) {
            if (DEBUG) {
                // ALL THE DATA
                BCLog.logger.warn("[lib.container] Failed to handle some widget data!");
                BCLog.logger.warn("[lib.container]   On the " + side);
                BCLog.logger.warn("[lib.container]   Widget {id = " + widgetId + ", class = " + widget.getClass() + "}");
                BCLog.logger.warn("[lib.container]   Container {class = " + getClass() + "}");
                BCLog.logger.warn("[lib.container]   Exception: ", io);
            }
        }
    }
}
