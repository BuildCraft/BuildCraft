// STUB(R.Chen): Forge GuiConfig — compile shim.
package net.minecraftforge.fml.client.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class GuiConfig extends Screen {
    public GuiConfig(Screen parent, Object configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title) {
        super(Text.literal(title != null ? title : "Config"));
    }
}
