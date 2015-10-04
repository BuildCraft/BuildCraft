package buildcraft.builders.gui;

import java.io.IOException;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.Template;
import buildcraft.core.lib.world.FakeWorld;
import buildcraft.core.lib.world.FakeWorldManager;

public abstract class GuiBlueprintBase extends GuiScreen {
    protected final BlueprintBase blueprint;
    private final FakeWorldManager fakeWorld;
    private float scroll = 16;
    private BlockPos offset = new BlockPos(0, 0, 0);
    private double mouseX = 0, mouseY = 0;
    private boolean hasRendered = false;

    public GuiBlueprintBase(BlueprintBase blueprint) {
        this.blueprint = blueprint;
        FakeWorld world;
        if (blueprint instanceof Blueprint) {
            world = new FakeWorld((Blueprint) blueprint);
        } else {
            world = new FakeWorld((Template) blueprint, Blocks.brick_block.getDefaultState());
        }
        fakeWorld = new FakeWorldManager(world);
        // This is set later, but just set it now to be able to use it earlier
        mc = Minecraft.getMinecraft();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void openGui(GuiOpenEvent event) {
        if (event.gui != this) {
            mc.mouseHelper.ungrabMouseCursor();
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!hasRendered) {
            mc.mouseHelper.grabMouseCursor();
            hasRendered = true;
        }
        fakeWorld.renderWorld(this.mouseX, this.mouseY, scroll, offset);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        double deltaWheel = Mouse.getEventDWheel() / 64;
        scroll -= deltaWheel;
        if (scroll < 2) {
            scroll += deltaWheel;
        }

        // Same formula as used by minecraft everywhere. No idea if its a special one for general mouse sensitivity or
        // minecraft specific though.
        double sensitivity = mc.gameSettings.mouseSensitivity * 0.6 + 0.2;
        sensitivity *= sensitivity * sensitivity * 0.8;

        mouseX += Mouse.getEventDX() * sensitivity;
        mouseY += Mouse.getEventDY() * sensitivity;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        EnumFacing currentLookFace = EnumFacing.fromAngle(mouseX);
        if (keyCode == mc.gameSettings.keyBindForward.getKeyCode()) {
            offset = offset.offset(currentLookFace.getOpposite());
        } else if (keyCode == mc.gameSettings.keyBindBack.getKeyCode()) {
            offset = offset.offset(currentLookFace);
        } else if (keyCode == mc.gameSettings.keyBindLeft.getKeyCode()) {
            offset = offset.offset(currentLookFace.rotateY());
        } else if (keyCode == mc.gameSettings.keyBindRight.getKeyCode()) {
            offset = offset.offset(currentLookFace.rotateY().getOpposite());
        } else if (isCtrlKeyDown() && !isShiftKeyDown()) {
            offset = offset.down();
        } else if (isShiftKeyDown() && !isCtrlKeyDown()) {
            offset = offset.up();
        }
    }
}
