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

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.gui.slot.IPhantomSlot;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.MessageContainer;
import buildcraft.lib.net.PacketBufferBC;

public abstract class ContainerBC_Neptune extends Container {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.container");

    protected static final IdAllocator IDS = new IdAllocator("container");
    /** Generic "data" id. Used by all containers which only have 1 id to write out (no point in making EVERY container
     * have an {@link IdAllocator} if they only allocate one. */
    public static final int NET_DATA = IDS.allocId("DATA");
    public static final int NET_WDIGET = IDS.allocId("WIDGET");

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

    public String getIdName(int id) {
        return IDS.getNameFor(id);
    }

    protected <W extends Widget_Neptune<?>> W addWidget(W widget) {
        if (widget == null) throw new NullPointerException("widget");
        widgets.add(widget);
        return widget;
    }

    public ImmutableList<Widget_Neptune<?>> getWidgets() {
        return ImmutableList.copyOf(widgets);
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
                copy.setCount(1);
                if (ItemStack.areItemsEqual(copy, slot.getStack()) && ItemStack.areItemStackTagsEqual(copy, slot.getStack())) {
                    copy.setCount(copy.getCount() + slot.getStack().getCount());
                }
                slot.putStack(copy);
            } else {
                slot.putStack(StackUtil.EMPTY);
            }
            itemStack = playerStack;
            return itemStack;
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }

    public static ItemStack safeCopy(ItemStack in) {
        return in == null ? null : in.copy();
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
            sendMessage(NET_WDIGET, (buffer) -> {
                buffer.writeShort(widgetId);
                writer.write(buffer);
            });
        }
    }

    public final void sendMessage(int id) {
        Side side = player.world.isRemote ? Side.CLIENT : Side.SERVER;
        sendMessage(id, (buffer) -> writeMessage(id, buffer, side));
    }

    public final void sendMessage(int id, IPayloadWriter writer) {
        PacketBufferBC payload = PacketBufferBC.write(writer);
        MessageContainer message = new MessageContainer(windowId, id, payload);
        if (player.world.isRemote) {
            BCMessageHandler.netWrapper.sendToServer(message);
        } else {
            BCMessageHandler.netWrapper.sendTo(message, (EntityPlayerMP) player);
        }
    }

    public void writeMessage(int id, PacketBufferBC buffer, Side side) {}

    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        if (id == NET_WDIGET) {
            int widgetId = buffer.readUnsignedShort();
            if (widgetId < 0 || widgetId >= widgets.size()) {
                if (DEBUG) {
                    String string = "Received unknown or invalid widget ID " + widgetId + " on side " + side;
                    if (side == Side.SERVER) {
                        string += " (for player " + player.getName() + ")";
                    }
                    BCLog.logger.warn(string);
                }
            } else {
                Widget_Neptune<?> widget = widgets.get(widgetId);
                if (side == Side.SERVER) {
                    widget.handleWidgetDataServer(ctx, buffer);
                } else if (side == Side.CLIENT) {
                    widget.handleWidgetDataClient(ctx, buffer);
                }
            }
        }
    }
}
