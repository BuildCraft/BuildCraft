package buildcraft.builders.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

import buildcraft.lib.client.render.HudRenderer;

public class HudSingleSchematic extends HudRenderer {
    @Override
    protected void renderImpl(Minecraft mc, EntityPlayerSP player) {

    }

    @Override
    protected boolean shouldRender(Minecraft mc, EntityPlayerSP player) {
        ItemStack stack = player.getHeldItemMainhand();
        return false;
    }
}
