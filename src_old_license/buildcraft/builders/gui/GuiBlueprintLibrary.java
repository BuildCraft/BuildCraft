/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.library.LibraryAPI;
import buildcraft.builders.tile.TileLibrary_Neptune;
import buildcraft.core.DefaultProps;
import buildcraft.core.blueprints.LibraryId;
import buildcraft.core.lib.gui.widgets.ScrollbarElement;
import buildcraft.core.lib.utils.BCStringUtils;
import buildcraft.lib.BCLibDatabase;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IPositionedElement;
import buildcraft.lib.library.LibraryEntryHeader;

public class GuiBlueprintLibrary extends GuiBC8<ContainerBlueprintLibrary> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftbuilders:textures/gui/library_rw.png");
    private static final GuiIcon SCROLLBAR_BACKGROUND = new GuiIcon(TEXTURE, 244, 0, 6, 110);
    private static final GuiIcon SCROLLBAR_ITSELF = new GuiIcon(TEXTURE, 250, 0, 6, 12);
    private static List<LibraryEntryHeader> entries = new ArrayList<>();

    private GuiButton deleteButton;
    private final ScrollbarElement<GuiBlueprintLibrary, ContainerBlueprintLibrary> scrollbar;
    private int selected = -1;

    public GuiBlueprintLibrary(EntityPlayer player, TileLibrary_Neptune library) {
        super(new ContainerBlueprintLibrary(player, library));
        xSize = 244;
        ySize = 220;

        IPositionedElement parent = rootElement.offset(163, 21);
        scrollbar = new ScrollbarElement<>(this, parent, height, SCROLLBAR_BACKGROUND, SCROLLBAR_ITSELF);

        fillEntries();
    }

    private static void fillEntries() {
        entries.clear();
        entries.addAll(BCLibDatabase.LOCAL_DB.getAllHeaders());
        if (BCLibDatabase.remoteDB != null) {
            entries.addAll(BCLibDatabase.remoteDB.getAllHeaders());
        }
        entries.sort(Comparator.naturalOrder());
    }

    @Override
    public void initGui() {
        super.initGui();

        deleteButton = new GuiButton(2, guiLeft + 174, guiTop + 109, 25, 20, BCStringUtils.localize("gui.del"));
        buttonList.add(deleteButton);

        guiElements.add(scrollbar);

        // container.tile.refresh();

        checkDelete();
    }

    @Override
    protected void drawForegroundLayer() {
        String title = BCStringUtils.localize("tile.libraryBlock.name");
        fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);

        int off = scrollbar.getPosition();
        for (int i = off; i < (off + 12); i++) {
            if (i >= entries.size()) {
                break;
            }
            LibraryEntryHeader header = entries.get(i);
            String name = header.name;

            if (name.length() > DefaultProps.MAX_NAME_SIZE) {
                name = name.substring(0, DefaultProps.MAX_NAME_SIZE);
            }

            if (i == selected) {
                int l1 = 8;
                int i2 = 22;

                drawGradientRect(l1, i2 + 9 * (i - off), l1 + 146, i2 + 9 * (i - off + 1), 0x80ffffff, 0x80ffffff);
            }

            while (fontRendererObj.getStringWidth(name) > (160 - 9)) {
                name = name.substring(0, name.length() - 1);
            }

            fontRendererObj.drawString(name, 9, 23 + 9 * (i - off), LibraryAPI.getHandlerFor(header.kind).getTextColor());
        }
    }

    public int getCenteredOffset(String title) {
        return 0;
    }
    
    @Override
    protected void drawBackgroundLayer(float particlTicks) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(TEXTURE);

        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        scrollbar.setLength(Math.max(0, entries.size() - 12));

//        drawWidgets(x, y);

//        int inP = library.progressIn * 22 / 100;
//        int outP = library.progressOut * 22 / 100;

//        drawTexturedModalRect(guiLeft + 194 + 22 - inP, guiTop + 57, 234 + 22 - inP, 240, inP, 16);
//        drawTexturedModalRect(guiLeft + 194, guiTop + 79, 234, 224, outP, 16);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (deleteButton != null && button == deleteButton) {
//            library.deleteSelectedBpt();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;

        if (x >= 8 && x <= 161) {
            int ySlot = (y - 22) / 9 + scrollbar.getPosition();

            if (ySlot > -1 && ySlot < entries.size()) {
                selected = ySlot;
            }
        }

        checkDelete();
    }

    protected void checkDelete() {
        if (selected != -1) {
            deleteButton.enabled = true;
        } else {
            deleteButton.enabled = false;
        }
    }
}
