/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import buildcraft.builders.container.ContainerBlueprintLibrary;
import buildcraft.builders.tile.TileLibrary_Neptune;
import buildcraft.lib.BCLibDatabase;
import buildcraft.lib.BCLibDatabase.EntryStatus;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.ScrollbarElement;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.library.ILibraryEntryData;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.library.RemoteLibraryDatabase;
import buildcraft.lib.library.network.MessageLibraryTransferEntry;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.StringUtilBC;

public class GuiBlueprintLibrary extends GuiBC8<ContainerBlueprintLibrary> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftbuilders:textures/gui/library_rw.png");
    private static final GuiIcon SCROLLBAR_BACKGROUND = new GuiIcon(TEXTURE, 244, 0, 6, 110);
    private static final GuiIcon SCROLLBAR_ITSELF = new GuiIcon(TEXTURE, 250, 0, 6, 12);
    private static final GuiIcon ICON_UPLOAD = new GuiIcon(TEXTURE, 0, 220, 7, 8);
    private static final GuiIcon ICON_DOWNLOAD = ICON_UPLOAD.offset(7, 0);

    private GuiButton deleteButton;
    private final ScrollbarElement<GuiBlueprintLibrary> scrollbar;
    public LibraryEntryHeader selected = null;

    public GuiBlueprintLibrary(EntityPlayer player, TileLibrary_Neptune library) {
        super(new ContainerBlueprintLibrary(player, library));

        // Always re-request the index, just to refresh it
        RemoteLibraryDatabase.requestIndex();

        xSize = 244;
        ySize = 220;

        IGuiPosition parent = rootElement.offset(163, 21);
        scrollbar = new ScrollbarElement<>(this, parent, height, SCROLLBAR_BACKGROUND, SCROLLBAR_ITSELF);

    }

    public static GuiIcon getIcon(EntryStatus status) {
        if (status == EntryStatus.REMOTE) return ICON_DOWNLOAD;
        if (status == EntryStatus.LOCAL) return ICON_UPLOAD;
        return null;
    }

    @Override
    public void initGui() {
        super.initGui();

        deleteButton = new GuiButton(2, guiLeft + 174, guiTop + 109, 25, 20, StringUtilBC.localize("gui.del"));
        buttonList.add(deleteButton);

        guiElements.add(scrollbar);

        checkDelete();
    }

    @Override
    protected void drawForegroundLayer() {
        String title = StringUtilBC.localize("tile.libraryBlock.name");
        int x = guiLeft;
        int y = guiTop;
        fontRendererObj.drawString(title, x + getCenteredOffset(title), y + 6, 0x404040);

        int off = scrollbar.getPosition();
        for (int i = off; i < (off + 12); i++) {
            if (i >= BCLibDatabase.allEntries.size()) {
                break;
            }

            LibraryEntryHeader header = BCLibDatabase.allEntries.get(i);
            String name = header.name;

            int l1 = x + 8;
            int i2 = y + 22;
            int yOff = i2 + 9 * (i - off);

            if (header.equals(selected)) {
                drawGradientRect(l1, yOff, l1 + 154, yOff + 9, 0x80ffffff, 0x80ffffff);
            }
            EntryStatus status = BCLibDatabase.getStatus(header);
            GuiIcon icon = getIcon(status);
            if (icon != null) {
                RenderUtil.setGLColorFromInt(-1);
                icon.drawAt(x + 8 + 146 + 1, yOff);
            }

            while (fontRendererObj.getStringWidth(name) > (160 - 9)) {
                name = name.substring(0, name.length() - 1);
            }

            int colour = /* LibraryAPI.getHandlerFor(header.kind).getTextColor() */0;
            fontRendererObj.drawString(name, x + 9, y + 23 + 9 * (i - off), colour);
        }
    }

    public int getCenteredOffset(String title) {
        int width = fontRendererObj.getStringWidth(title);
        return (xSize - width) / 2;
    }

    @Override
    protected void drawBackgroundLayer(float particlTicks) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(TEXTURE);

        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        scrollbar.setLength(Math.max(0, BCLibDatabase.allEntries.size() - 12));

        // drawWidgets(x, y);

        // int inP = library.progressIn * 22 / 100;
        // int outP = library.progressOut * 22 / 100;

        // drawTexturedModalRect(guiLeft + 194 + 22 - inP, guiTop + 57, 234 + 22 - inP, 240, inP, 16);
        // drawTexturedModalRect(guiLeft + 194, guiTop + 79, 234, 224, outP, 16);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (deleteButton != null && button == deleteButton) {
            // library.deleteSelectedBpt();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;

        if (x >= 8 && x <= 161) {
            int ySlot = (y - 22) / 9 + scrollbar.getPosition();

            if (ySlot > -1 && ySlot < BCLibDatabase.allEntries.size()) {
                LibraryEntryHeader header = BCLibDatabase.allEntries.get(ySlot);
                selected = header;
                sendSelected();
                if (x > 154) {
                    // The "upload/download" button
                    EntryStatus status = BCLibDatabase.getStatus(header);
                    if (status == EntryStatus.LOCAL) {
                        uploadEntry(header);
                    } else if (status == EntryStatus.REMOTE) {
                        downloadEntry(header);
                    }
                }
            }
        }
    }

    private void sendSelected() {
        container.tile.selected = selected;
        IMessage message = container.tile.createNetworkUpdate(TileLibrary_Neptune.NET_SELECTED);
        MessageUtil.getWrapper().sendToServer(message);
    }

    private static void uploadEntry(LibraryEntryHeader header) {
        ILibraryEntryData data = BCLibDatabase.LOCAL_DB.getEntry(header);
        MessageLibraryTransferEntry transfer = new MessageLibraryTransferEntry(header, data);
        MessageUtil.getWrapper().sendToServer(transfer);
    }

    private static void downloadEntry(LibraryEntryHeader header) {
        RemoteLibraryDatabase.requestEntry(header);
    }

    protected void checkDelete() {
        if (selected != null) {
            deleteButton.enabled = true;
        } else {
            deleteButton.enabled = false;
        }
    }
}
