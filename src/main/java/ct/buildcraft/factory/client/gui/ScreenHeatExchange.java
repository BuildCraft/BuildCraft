package ct.buildcraft.factory.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.factory.BCFactorySprites;
import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.ContainerScreenBase;
import ct.buildcraft.lib.gui.component.TankComponent;
import ct.buildcraft.lib.gui.help.GuiHelpUtil;
import ct.buildcraft.lib.gui.ledger.LedgerHelp;
import ct.buildcraft.lib.gui.ledger.LedgerOwnership;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenHeatExchange extends ContainerScreenBase<MenuHeatExchange> {

    private static final ResourceLocation TEXTURE_BASE = BCFactorySprites.HEAT_EXCHANGE;

    protected static final TankComponent[] tanks = {
        new TankComponent(44, 12, 16, 38, 2000, -1, -1),
        new TankComponent(44, 64, 33, 16, 2000, -1, -1),
        new TankComponent(98, 12, 33, 16, 2000, -1, -1),
        new TankComponent(116, 43, 16, 38, 2000, -1, -1),
    };

    private final BuildCraftGui mainGui;

    public ScreenHeatExchange(MenuHeatExchange menu, Inventory inventory, Component name) {
        super(menu, inventory, name, 4, TEXTURE_BASE);
        this.mainGui = new BuildCraftGui(this, BuildCraftGui.createWindowedArea(this));
        titleLabelY -= 3;
        inventoryLabelY += 8;
        for (int i = 0; i < 4; i++) {
            add(tanks[i], true);
        }
        setup(menu.data);

        if (menu.tile != null) {
            mainGui.shownElements.add(new LedgerOwnership(mainGui, menu.tile, true));
        }
        GuiHelpUtil.addRoot(mainGui, 44, 12, 16, 38, "buildcraft.help.heat_exchange.input_a.title", 0xFF_55_99_FF,
            "buildcraft.help.heat_exchange.input_a.desc");
        GuiHelpUtil.addRoot(mainGui, 44, 64, 33, 16, "buildcraft.help.heat_exchange.output_a.title", 0xFF_55_CC_FF,
            "buildcraft.help.heat_exchange.output_a.desc");
        GuiHelpUtil.addRoot(mainGui, 98, 12, 33, 16, "buildcraft.help.heat_exchange.input_b.title", 0xFF_FF_99_55,
            "buildcraft.help.heat_exchange.input_b.desc");
        GuiHelpUtil.addRoot(mainGui, 116, 43, 16, 38, "buildcraft.help.heat_exchange.output_b.title", 0xFF_FF_CC_55,
            "buildcraft.help.heat_exchange.output_b.desc");
        mainGui.shownElements.add(new LedgerHelp(mainGui, false));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.mainGui.tick();
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        super.render(pose, mouseX, mouseY, partialTick);
        this.renderTooltip(pose, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        super.renderBg(pose, partialTick, mouseX, mouseY);
        this.mainGui.drawBackgroundLayer(pose, partialTick, mouseX, mouseY, () -> {});
        this.mainGui.drawElementBackgrounds(pose);
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        super.renderLabels(pose, mouseX, mouseY);
        this.mainGui.preDrawForeground(pose);
        this.mainGui.drawElementForegrounds(pose, () -> {});
        this.mainGui.postDrawForeground(pose);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.mainGui.onMouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.mainGui.onMouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.mainGui.onMouseDragged(mouseX, mouseY, button, dragX, dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.mainGui.onKeyTyped(modifiers, InputConstants.getKey(keyCode, scanCode))) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
