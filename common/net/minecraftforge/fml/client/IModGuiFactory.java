// STUB(R.Chen): Forge IModGuiFactory — compile shim.
package net.minecraftforge.fml.client;

import net.minecraft.client.MinecraftClient;

public interface IModGuiFactory {
    void initialize(MinecraftClient minecraft);
    boolean hasConfigGui(MinecraftClient parentMcScreen);
    Object createConfigGui(Object parentScreen);
}
