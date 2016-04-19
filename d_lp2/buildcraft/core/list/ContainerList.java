/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.list;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.relauncher.Side;

import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.net.MessageCommand;
import buildcraft.lib.net.command.ICommandReceiver;

public class ContainerList extends BuildCraftContainer implements ICommandReceiver {
    public ListHandler.Line[] lines;

    public ContainerList(EntityPlayer iPlayer) {
        super(iPlayer, iPlayer.inventory.getSizeInventory());

        lines = ListHandler.getLines(getListItemStack());

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlotToContainer(new Slot(player.inventory, sx + sy * 9 + 9, 8 + sx * 18, 103 + sy * 18));
            }
        }

        for (int sx = 0; sx < 9; sx++) {
            addSlotToContainer(new Slot(player.inventory, sx, 8 + sx * 18, 161));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    public ItemStack getListItemStack() {
        ItemStack toTry = player.getHeldItemMainhand();
        if (toTry != null && toTry.getItem() instanceof ItemList_BC8) {
            return toTry;
        }

        toTry = player.getHeldItemOffhand();
        if (toTry != null && toTry.getItem() instanceof ItemList_BC8) {
            return toTry;
        }
        return null;
    }

    public void setStack(final int lineIndex, final int slotIndex, final ItemStack stack) {
        lines[lineIndex].setStack(slotIndex, stack);
        ListHandler.saveLines(getListItemStack(), lines);

        if (player.worldObj.isRemote) {
            BCMessageHandler.netWrapper.sendToServer(new MessageCommand(this, "setStack", (buffer) -> {
                buffer.writeByte(lineIndex);
                buffer.writeByte(slotIndex);
                buffer.writeItemStackToBuffer(stack);
            }));
        }
    }

    public void switchButton(final int lineIndex, final int button) {
        lines[lineIndex].toggleOption(button);
        ListHandler.saveLines(getListItemStack(), lines);

        if (player.worldObj.isRemote) {
            BCMessageHandler.netWrapper.sendToServer(new MessageCommand(this, "switchButton", (buffer) -> {
                buffer.writeByte(lineIndex);
                buffer.writeByte(button);
            }));
        }
    }

    public void setLabel(final String text) {
        BCCoreItems.list.setName(getListItemStack(), text);

        if (player.worldObj.isRemote) {
            BCMessageHandler.netWrapper.sendToServer(new MessageCommand(this, "setLabel", (buffer) -> {
                buffer.writeString(text);
            }));
        }
    }

    @Override
    public MessageCommand receiveCommand(String command, Side side, PacketBuffer buffer) throws IOException {
        if (side.isServer()) {
            if ("setLabel".equals(command)) {
                setLabel(buffer.readStringFromBuffer(1024));
            } else if ("switchButton".equals(command)) {
                switchButton(buffer.readUnsignedByte(), buffer.readUnsignedByte());
            } else if ("setStack".equals(command)) {
                setStack(buffer.readUnsignedByte(), buffer.readUnsignedByte(), buffer.readItemStackFromBuffer());
            }
        }
        return null;
    }
}
