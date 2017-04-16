package buildcraft.builders.gui;

import buildcraft.builders.snapshot.Snapshot;
import buildcraft.lib.gui.pos.GuiRectangle;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.builders.container.ContainerBuilder;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;

public class GuiBuilder extends GuiBC8<ContainerBuilder> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftbuilders:textures/gui/builder.png");
    private static final ResourceLocation TEXTURE_BLUEPRINT =
            new ResourceLocation("buildcraftbuilders:textures/gui/builder_blueprint.png");
    private static final int SIZE_X = 176, SIZE_BLUEPRINT_X = 256, SIZE_Y = 222, BLUEPRINT_WIDTH = 87;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_BLUEPRINT_GUI = new GuiIcon(
            TEXTURE_BLUEPRINT,
            SIZE_BLUEPRINT_X - BLUEPRINT_WIDTH,
            0,
            BLUEPRINT_WIDTH,
            SIZE_Y
    );
    private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE_BLUEPRINT, 0, 54, 16, 47);

    public GuiBuilder(ContainerBuilder container) {
        super(container);
        xSize = SIZE_BLUEPRINT_X;
        ySize = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();

        for (int i = 0; i < container.widgetTanks.size(); i++) {
            guiElements.add(
                    container.widgetTanks
                            .get(i)
                            .createGuiElement(
                                    this,
                                    rootElement,
                                    new GuiRectangle(179 + i * 18, 145, 16, 47),
                                    ICON_TANK_OVERLAY
                            )
            );
        }
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
        ICON_BLUEPRINT_GUI.drawAt(rootElement.offset(SIZE_BLUEPRINT_X - BLUEPRINT_WIDTH, 0));
        if (container.tile.snapshotType == Snapshot.EnumSnapshotType.BLUEPRINT) {
            int x = 0;
            int y = 0;
            for (ItemStack stack : container.tile.blueprintBuilder.remainingDisplayRequired) {
                int posX = rootElement.getX() + 179 + x * 18;
                int posY = rootElement.getY() + 18 + y * 18;
                RenderHelper.enableGUIStandardItemLighting();
                this.itemRender.renderItemAndEffectIntoGUI(this.mc.player, stack, posX, posY);
                this.itemRender.renderItemOverlayIntoGUI(this.mc.fontRenderer, stack, posX, posY, null);
                RenderHelper.disableStandardItemLighting();
                x++;
                if (x == 4) {
                    x = 0;
                    y++;
                }
            }
        }
    }
}
