// STUB(R.Chen): Minecraft 1.12 GuiScreen → 1.20.1 Screen.
package net.minecraft.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.text.Text;

public abstract class GuiScreen extends net.minecraft.client.gui.screen.Screen {
    /** Forge/1.12: reference to MinecraftClient. */
    protected MinecraftClient mc = MinecraftClient.getInstance();
    /** Forge/1.12: texture manager. */
    protected TextureManager renderEngine = TextureManager.INSTANCE;
    /** Forge/1.12: font renderer. */
    protected TextRenderer fontRenderer = MinecraftClient.getInstance().textRenderer;

    protected GuiScreen() { super(Text.empty()); }
    protected GuiScreen(Text title) { super(title); }

    /** Forge compat: called when screen opens. */
    public void initGui() { super.init(); }

    @Override
    protected void init() {
        mc = MinecraftClient.getInstance();
        renderEngine = TextureManager.INSTANCE;
        fontRenderer = mc.textRenderer;
        initGui();
    }
}
